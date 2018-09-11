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

package org.protocol.ezmqx;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.edgexfoundry.ezmq.EZMQCallback;
import org.edgexfoundry.ezmq.EZMQErrorCode;
import org.edgexfoundry.ezmq.EZMQPublisher;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;
import org.protocol.ezmqx.internal.Context;
import org.protocol.ezmqx.internal.RestResponse;
import org.protocol.ezmqx.internal.RestFactory;
import org.protocol.ezmqx.internal.RestUtils;
import org.protocol.ezmqx.internal.TopicHandler;
import org.protocol.ezmqx.internal.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class is base class of EZMQX publisher.
 */
public class EZMQXPublisher {
  protected Context mContext;
  protected TopicHandler mHandler;
  protected EZMQPublisher mPublisher;
  protected EZMQCallback mCallback;
  protected EZMQXTopic mTopic;
  protected AtomicBoolean mTerminated;
  protected int mLocalPort;
  protected boolean mSecured;

  private final static EdgeXLogger logger = EdgeXLoggerFactory.getEdgeXLogger(EZMQXPublisher.class);

  protected EZMQXPublisher() {
    mContext = Context.getInstance();
  }

  // finalize method to be called by Java Garbage collector
  // before destroying
  // this object.
  @Override
  protected void finalize() throws EZMQXException {
    terminate();
  }

  protected void initialize(int optionalPort, String serverPrivateKey) throws EZMQXException {
    if (!mContext.isInitialized()) {
      throw new EZMQXException("Could not create publisher context not initialized",
          EZMQXErrorCode.NotInitialized);
    }
    if (mContext.isStandAlone()) {
      mLocalPort = optionalPort;
    } else {
      mLocalPort = mContext.assignDynamicPort();
    }
    mCallback = new EZMQCallback() {
      public void onStopCB(EZMQErrorCode code) {}

      public void onStartCB(EZMQErrorCode code) {}

      public void onErrorCB(EZMQErrorCode code) {}
    };

    // create ezmq publisher
    mPublisher = new EZMQPublisher(mLocalPort, mCallback);
    if (null == mPublisher) {
      throw new EZMQXException("Could not create ezmq publisher", EZMQXErrorCode.UnKnownState);
    }

    //Set server key
    if (serverPrivateKey.length() == Utils.KEY_LENGTH) {
      try {
        EZMQErrorCode result = mPublisher.setServerPrivateKey(serverPrivateKey);
        if (result != EZMQErrorCode.EZMQ_OK) {
          logger.error("setServerPrivateKey [Result]: " + result);
          throw new EZMQXException("Could not set key", EZMQXErrorCode.InvalidParam);
        }
      } catch (Exception e) {
        logger.error("setServerPrivateKey [Exception occured]: " + e.getMessage());
        throw new EZMQXException(e.getMessage(), EZMQXErrorCode.UnKnownState);
      }
      mSecured = true;
    }

    if (EZMQErrorCode.EZMQ_OK != mPublisher.start()) {
      throw new EZMQXException("Could not start ezmq publisher", EZMQXErrorCode.UnKnownState);
    }

    // Init topic handler
    if (mContext.isTnsEnabled()) {
      mHandler = TopicHandler.getInstance();
      mHandler.initHandler();
      logger.debug("Initialized topic handler");
    }
    mTerminated = new AtomicBoolean(false);
  }

  private void parseTopicResponse(RestResponse response) throws EZMQXException {
    int statusCode = response.getStatusCode();
    logger.debug("[TNS register topic] Status code: " + statusCode);
    if (statusCode != RestUtils.HTTP_CREATED) {
      mPublisher.stop(); // free the EZMQ publisher instance
      throw new EZMQXException("Could not register topic", EZMQXErrorCode.RestError);
    }
    String jsonString = response.getResponse();
    logger.debug("[TNS register topic] Response: " + jsonString);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = null;
    try {
      root = mapper.readTree(jsonString);
    } catch (IOException e) {
      throw new EZMQXException("Could not parse register response", EZMQXErrorCode.RestError);
    }
    if (root.has(RestUtils.PAYLOAD_KEEPALIVE_INTERVAL)) {
      int interval = root.path(RestUtils.PAYLOAD_KEEPALIVE_INTERVAL).asInt();
      logger.debug("[[Register topic] Keep Alive interval: " + interval);
      if (interval < 1) {
        mPublisher.stop(); // free the EZMQ publisher instance
        throw new EZMQXException("Invalid keepAlive interval", EZMQXErrorCode.RestError);
      }
      logger.debug(
          "[[Register topic] Current Keep Alive interval: " + mHandler.getKeepAliveInterval());
      if (mHandler.getKeepAliveInterval() < 1) {
        mHandler.updateKeepAliveInterval(interval);
      }
      if (!mHandler.isKeepAliveStarted()) {
        TopicHandler.getInstance().send(RestUtils.KEEPALIVE, "");
      }
    }
  }

  protected void registerTopic(EZMQXTopic topic) throws EZMQXException {
    mTopic = topic;
    if (!(mContext.isTnsEnabled())) {
      return;
    }

    // Send post request to TNS server
    String topicURL = mContext.getTnsAddr() + RestUtils.PREFIX + RestUtils.TOPIC;
    logger.debug("[TNS register topic] Rest URL: " + topicURL);
    // Form post payload
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode = mapper.createObjectNode();
    JsonNode nodeTopic = mapper.createObjectNode();
    ((ObjectNode) nodeTopic).put(RestUtils.PAYLOAD_NAME, topic.getName());
    ((ObjectNode) nodeTopic).put(RestUtils.PAYLOAD_ENDPOINT, topic.getEndPoint().toString());
    ((ObjectNode) nodeTopic).put(RestUtils.PAYLOAD_DATAMODEL, topic.getDatamodel());
    ((ObjectNode) nodeTopic).put(RestUtils.PAYLOAD_SECURED, topic.isSecured());
    ((ObjectNode) rootNode).set(RestUtils.PAYLOAD_TOPIC, nodeTopic);
    String payload = rootNode.toString();
    logger.debug("[TNS register topic] payload : " + payload);

    RestFactory restClient = RestFactory.getInstance();
    RestResponse response;
    try {
      response = restClient.post(topicURL, payload);
    } catch (Exception e) {
      throw new EZMQXException("Could not send register request to TNS", EZMQXErrorCode.RestError);
    }
    parseTopicResponse(response);
    // send request to add topic to list
    TopicHandler.getInstance().send(RestUtils.REGISTER, topic.getName());
  }

  protected void unRegisterTopic(EZMQXTopic topic) throws EZMQXException {
    if (!(mContext.isTnsEnabled())) {
      return;
    }
    String topicURL = mContext.getTnsAddr() + RestUtils.PREFIX + RestUtils.TOPIC;
    String query = RestUtils.QUERY_NAME + topic.getName();
    logger.debug("[TNS unregister topic] Rest URL: " + topicURL);
    logger.debug("[TNS unregister topic] Query: " + query);

    RestFactory restClient = RestFactory.getInstance();
    RestResponse response;
    try {
      response = restClient.delete(topicURL, query);
      logger.debug("[TNS unregister topic] Response code: " + response.getStatusCode());
    } catch (Exception e) {
      logger.debug("Caught exeption : " + e.getMessage());
      return;
    }
    // send request to topicHandler for removing topic from list
    TopicHandler.getInstance().send(RestUtils.UNREGISTER, topic.getName());
    logger.debug("Sent request to topic handler to remove topic from list: " + topic.getName());
  }

  /**
   * Get EZMQX topic.
   *
   * @return {@link EZMQXTopic}
   *
   */
  public EZMQXTopic getTopic() throws EZMQXException {
    if (mContext.isTerminated()) {
      throw new EZMQXException("Publisher terminated", EZMQXErrorCode.Terminated);
    }
    return mTopic;
  }

  /**
   * Terminate EZMQX publisher.
   *
   */
  public synchronized void terminate() throws EZMQXException {
    if (mTerminated.get()) {
      throw new EZMQXException("Publisher already terminated", EZMQXErrorCode.Terminated);
    }
    if (!mContext.isStandAlone()) {
      mContext.releaseDynamicPort(mLocalPort);
      logger.debug("Released local port");
    }
    if (mContext.isTnsEnabled()) {
      unRegisterTopic(mTopic);
      logger.debug("Unregistered topic on TNS");
    }
    if (mPublisher != null) {
      mPublisher.stop();
      logger.debug("Stopped EZMQ publisher");
    }
    mTerminated.set(true);
  }

  /**
   * Check whether EZMQX publisher is terminated or not.
   *
   * @return true if terminated otherwise false.
   */
  public boolean isTerminated() {
    return mTerminated.get();
  }

  /**
   * Check if publisher is secured or not.
   *
   * @return Returns true if publisher is secured otherwise false.
   */
  public boolean isSecured() {
    return mSecured;
  }
}
