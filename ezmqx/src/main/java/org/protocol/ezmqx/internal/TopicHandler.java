/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/

package org.protocol.ezmqx.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.edgexfoundry.ezmq.EZMQAPI;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;
import org.protocol.ezmqx.EZMQXException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TopicHandler {
  private static TopicHandler mInstance;
  private RestFactory mRestClient;
  private ZMQ.Context mContext;
  private Context mEZMQXContext;
  private ZMQ.Socket mTopicServer;
  private ZMQ.Socket mTopicClient;
  private Poller mPoller;
  private Thread mThread;
  private String mHandlerAddress;
  private AtomicBoolean mInitialized;
  private AtomicInteger mKeepAliveInterval;
  private AtomicBoolean mIsKeepAliveStarted;
  private String mTnsAddress;
  private final String INPROC_PREFIX = "inproc://topicHandler";
  private List<String> mTopicList;

  private final static EdgeXLogger logger = EdgeXLoggerFactory.getEdgeXLogger(TopicHandler.class);

  private TopicHandler() throws EZMQXException {
    mContext = EZMQAPI.getInstance().getContext();
    mEZMQXContext = Context.getInstance();
    mTnsAddress = mEZMQXContext.getTnsAddr();
    mKeepAliveInterval = new AtomicInteger(-1);
    mInitialized = new AtomicBoolean(false);
    mIsKeepAliveStarted = new AtomicBoolean(false);
    mTopicList = new ArrayList<String>();
  }

  public static synchronized TopicHandler getInstance() throws EZMQXException {
    if (null == mInstance) {
      mInstance = new TopicHandler();
    }
    return mInstance;
  }

  public synchronized void initHandler() {
    if (mInitialized.get()) {
      return;
    }
    String address;
    try {
      // Topic server
      if (null == mTopicServer) {
        address = getInProcUniqueAddress();
        logger.debug("Server address: " + address);
        mTopicServer = mContext.socket(ZMQ.PAIR);
        mTopicServer.bind(address);
        mHandlerAddress = address;
      }

      // Topic client
      if (null == mTopicClient) {
        address = getHandlerAddress();
        logger.debug("Client address: " + address);
        mTopicClient = mContext.socket(ZMQ.PAIR);
        mTopicClient.connect(address);
      }

      // Poller
      if (null == mPoller) {
        mPoller = mContext.poller(1);
        mPoller.register(mTopicServer, Poller.POLLIN | Poller.POLLERR);
      }

      // Handler thread
      if (null == mThread) {
        mThread = new Thread(new Runnable() {
          public void run() {
            handleEvents();
          }
        });
        mThread.start();
        logger.debug("Topic Handler thread started");
      }

    } catch (Exception e) {
      logger.debug("Caught exception: " + e.getMessage());
      return;
    }
    mInitialized.set(true);
  }

  public synchronized void send(String requestType, String payload) {
    if (null == requestType || null == payload) {
      logger.debug("Invalid parameter");
      return;
    }
    try {
      boolean result = mTopicClient.sendMore(requestType);
      if (false == result) {
        logger.error("SendMore failed [requestType]");
        return;
      }
      result = mTopicClient.send(payload);
      if (false == result) {
        logger.error("Send failed [payload]");
        return;
      }
    } catch (Exception e) {
      e.getStackTrace();
    }
  }

  public void updateKeepAliveInterval(int keepAliveInterval) {
    mRestClient = RestFactory.getInstance();
    mKeepAliveInterval.set(keepAliveInterval * 1000);
  }

  public int getKeepAliveInterval() {
    return mKeepAliveInterval.get();
  }

  public boolean isKeepAliveStarted() {
    return mIsKeepAliveStarted.get();
  }

  public synchronized void terminateHandler() {
    if (!mInitialized.get()) {
      return;
    }
    logger.debug("Sending shutdown request..");
    send(RestUtils.SHUTDOWN, "");
    // wait for topic handler thread to stop
    try {
      if (null != mThread) {
        mThread.join();
      }
    } catch (InterruptedException e) {
      logger.error("Thread join exception" + e.getMessage());
    }

    try {
      if (null != mPoller) {
        mPoller.unregister(mTopicServer);
      }

      if (null != mTopicServer) {
        mTopicServer.close();
      }

      if (null != mTopicClient) {
        mTopicClient.close();
      }

      mPoller = null;
      mTopicServer = null;
      mTopicClient = null;
      mThread = null;
      mKeepAliveInterval.set(-1);
      mIsKeepAliveStarted.set(false);
    } catch (Exception e) {
      logger.error("Exception while stopping topic handler: " + e.getMessage());
      return;
    }
    mInitialized.set(false);
    logger.debug("Terminated topic handler");
  }

  private String getHandlerAddress() {
    return mHandlerAddress;
  }

  private void AddTopic(String topic) {
    logger.debug("[TNS register topic] add topic to list: " + topic);
    mTopicList.add(topic);
  }

  private void removeTopic(String topic) {
    logger.debug("[TNS register topic] Remove topic from list: " + topic);
    mTopicList.remove(topic);
  }

  private void sendKeepAlive() {
    String keepAliveURL = mTnsAddress + RestUtils.PREFIX + RestUtils.TNS_KEEP_ALIVE;
    logger.debug("[TNS Keep alive ] Rest URL: " + keepAliveURL);

    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootN = mapper.createObjectNode();
    ArrayNode childNode = mapper.createArrayNode();
    for (String topic : mTopicList) {
      childNode.add(topic);
    }
    ((ObjectNode) rootN).set("topic_names", childNode);
    String payload = rootN.toString();
    logger.debug("[TNS Keep alive ] payload: " + payload);

    try {
      RestResponse response = mRestClient.post(keepAliveURL, payload, mKeepAliveInterval.get() * 2);
      logger.debug("[TNS Keep alive topic] Response code: " + response.getStatusCode());
    } catch (Exception e) {
      logger.debug("Caught exeption : " + e.getMessage());
      return;
    }
  }

  private boolean parseSocketData() {
    String requestType = null;
    String data = null;
    try {
      if (null != mTopicServer) {
        requestType = mTopicServer.recvStr();
        if (mTopicServer.hasReceiveMore()) {
          data = mTopicServer.recvStr();
        }
      }
    } catch (Exception e) {
      logger.error("Exception while parsing socket data: " + e.getMessage());
      return false;
    }

    if (requestType.equals(RestUtils.SHUTDOWN)) {
      logger.debug("requestType: " + requestType);
      return true;
    } else if (requestType.equals(RestUtils.REGISTER)) {
      AddTopic(data);
    } else if (requestType.equals(RestUtils.UNREGISTER)) {
      removeTopic(data);
    } else if (requestType.equals(RestUtils.KEEPALIVE)) {
      mIsKeepAliveStarted.set(true);
    } else {
      logger.error("Unknown request type");
    }
    return false;
  }

  private void handleEvents() {
    long lastKeepAlive = 0;
    boolean isShutDown = false;
    while (null != mThread && !mThread.isInterrupted()) {
      if (null == mPoller) {
        logger.error("poller is null");
        return;
      }
      mPoller.poll((mKeepAliveInterval.get()));
      if (mPoller.pollin(0)) {
        logger.debug("Received register/unregister/keepalive/shutdown request");
        isShutDown = parseSocketData();
        if (true == isShutDown) {
          logger.debug("Received shut down request");
          break;
        }
      } else {
        logger.debug("Poller timeout occured");
      }
      if (mIsKeepAliveStarted.get()) {
        logger.debug("Starting keep alive request");
        logger.debug("Diff : " + (System.currentTimeMillis() - lastKeepAlive));
        if ((System.currentTimeMillis() - lastKeepAlive) >= mKeepAliveInterval.get()) {
          logger.debug("Sending rest request for keep alive request");
          sendKeepAlive();
          lastKeepAlive = System.currentTimeMillis();
          logger.debug("New lastKeepAlive time is : " + lastKeepAlive);
        }
      }
    }
    logger.debug("handleEvents thread stopped");
  }

  private String getInProcUniqueAddress() {
    String address = INPROC_PREFIX + UUID.randomUUID().toString();
    return address;
  }
}
