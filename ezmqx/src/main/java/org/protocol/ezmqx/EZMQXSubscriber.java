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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.datamodel.aml.Representation;
import org.edgexfoundry.ezmq.EZMQContentType;
import org.edgexfoundry.ezmq.EZMQErrorCode;
import org.edgexfoundry.ezmq.EZMQMessage;
import org.edgexfoundry.ezmq.EZMQSubscriber;
import org.edgexfoundry.ezmq.EZMQSubscriber.EZMQSubCallback;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;
import org.protocol.ezmqx.internal.Context;
import org.protocol.ezmqx.internal.RestResponse;
import org.protocol.ezmqx.internal.RestFactory;
import org.protocol.ezmqx.internal.RestUtils;
import org.protocol.ezmqx.internal.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is base class of EZMQX subscribers.
 */
public class EZMQXSubscriber {
  protected Context mContext;
  protected EZMQSubscriber mSubscriber;
  protected AtomicBoolean mTerminated;
  protected List<EZMQXTopic> mStoredTopics;
  protected Map<String, Representation> mAMLRepDic;
  private EZMQXSubCallback mCallback;

  private final static EdgeXLogger logger =
      EdgeXLoggerFactory.getEdgeXLogger(EZMQXSubscriber.class);

  protected EZMQXSubscriber() {
    mTerminated = new AtomicBoolean(false);
    mContext = Context.getInstance();
    mStoredTopics = new ArrayList<EZMQXTopic>();
    mAMLRepDic = new HashMap<String, Representation>();
  }

  // finalize method to be called by Java Garbage collector
  // before destroying
  // this object.
  @Override
  protected void finalize() throws EZMQXException {
    terminate();
  }

  protected interface EZMQXSubCallback {
    public void onMessage(String topic, EZMQMessage data);
  }

  protected void setSubCallback(EZMQXSubCallback callback) {
    mCallback = callback;
  }

  protected void initialize(String topic, boolean isHierarchical) throws EZMQXException {
    if (!mContext.isInitialized()) {
      throw new EZMQXException("Could not create Subscriber context not initialized",
          EZMQXErrorCode.NotInitialized);
    }
    boolean result = Utils.validateTopic(topic);
    if (false == result) {
      throw new EZMQXException("Invalid topic", EZMQXErrorCode.InvalidTopic);
    }
    List<EZMQXTopic> verified = new ArrayList<EZMQXTopic>();
    if (mContext.isTnsEnabled()) {
      verified = verifyTopics(topic, isHierarchical);
      if (verified.isEmpty()) {
        throw new EZMQXException("Could not find matched topic", EZMQXErrorCode.NoTopicMatched);
      }
    } else {
      throw new EZMQXException("TNS not available", EZMQXErrorCode.TnsNotAvailable);
    }
    initialize(verified);
  }

  protected void initialize(List<EZMQXTopic> topics) throws EZMQXException {
    if (!mContext.isInitialized()) {
      throw new EZMQXException("Could not create Subscriber context not initialized",
          EZMQXErrorCode.NotInitialized);
    }
    for (EZMQXTopic topic : topics) {
      mAMLRepDic.put(topic.getName(), mContext.getAmlRep(topic.getDatamodel()));
      subscribe(topic);
      mStoredTopics.add(topic);
    }
  }

  private void createSubscriber(EZMQXEndPoint endPoint) throws EZMQXException {
    mSubscriber = new EZMQSubscriber(endPoint.getAddr(), endPoint.getPort(), new EZMQSubCallback() {
      public void onMessageCB(String topic, EZMQMessage ezmqMessage) {
        if (EZMQContentType.EZMQ_CONTENT_TYPE_BYTEDATA == ezmqMessage.getContentType()) {
          mCallback.onMessage(topic, ezmqMessage);
        } else {
        }
      }

      public void onMessageCB(EZMQMessage ezmqMessage) {}
    });

    if (EZMQErrorCode.EZMQ_OK != mSubscriber.start()) {
      throw new EZMQXException("Could not connect endpoint: " + endPoint.toString(),
          EZMQXErrorCode.SessionUnavailable);
    }
  }

  protected void subscribe(EZMQXTopic topic) throws EZMQXException {
    EZMQXEndPoint endPoint = topic.getEndPoint();
    if (null == mSubscriber) {
      createSubscriber(endPoint);
    }
    EZMQErrorCode errorCode =
        mSubscriber.subscribe(endPoint.getAddr(), endPoint.getPort(), topic.getName());

    if (EZMQErrorCode.EZMQ_OK != errorCode) {
      throw new EZMQXException("Could not Subscribe to endpoint: " + endPoint.toString(),
          EZMQXErrorCode.SessionUnavailable);
    }
    logger.debug("Subscribed for topic: " + topic.getName());
  }

  private List<EZMQXTopic> parseTNSResponse(RestResponse response) throws EZMQXException {
    if (null == response) {
      throw new EZMQXException("Could not get topic", EZMQXErrorCode.RestError);
    }
    logger.debug("[TNS get topic] Status code: " + response.getStatusCode());
    if (response.getStatusCode() != RestUtils.HTTP_OK) {
      throw new EZMQXException("Could not discover topic", EZMQXErrorCode.RestError);
    }

    String jsonString = response.getResponse();
    logger.debug("[TNS get topic] Response: " + jsonString);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = null;
    try {
      root = mapper.readTree(jsonString);
    } catch (IOException e) {
      throw new EZMQXException("Could not parse response", EZMQXErrorCode.RestError);
    }

    List<EZMQXTopic> topics = new ArrayList<EZMQXTopic>();
    JsonNode propertiesNode = root.path(RestUtils.PAYLOAD_TOPICS);
    for (JsonNode node : propertiesNode) {
      if (node.has(RestUtils.PAYLOAD_NAME) && node.has(RestUtils.PAYLOAD_DATAMODEL)
          && node.has(RestUtils.PAYLOAD_ENDPOINT)) {
        String name = node.path(RestUtils.PAYLOAD_NAME).asText();
        String dataModel = node.path(RestUtils.PAYLOAD_DATAMODEL).asText();
        String ep = node.path(RestUtils.PAYLOAD_ENDPOINT).asText();
        EZMQXEndPoint endPoint = new EZMQXEndPoint(ep);
        EZMQXTopic topic = new EZMQXTopic(name, dataModel, endPoint);
        topics.add(topic);
      }
    }
    return topics;
  }

  protected List<EZMQXTopic> verifyTopics(String topic, boolean isHierarchical)
      throws EZMQXException {
    // Send post request to TNS server
    String topicURL = mContext.getTnsAddr() + RestUtils.PREFIX + RestUtils.TOPIC;
    String query = RestUtils.QUERY_NAME + topic + RestUtils.QUERY_HIERARCHICAL
        + (isHierarchical == true ? RestUtils.QUERY_TRUE : RestUtils.QUERY_FALSE);
    logger.debug("[TNS get topic] Rest URL: " + topicURL);
    logger.debug("[TNS get topic] Query: " + query);

    RestFactory restClient = RestFactory.getInstance();
    RestResponse response = null;
    try {
      response = restClient.get(topicURL, query);
    } catch (Exception e) {
      throw new EZMQXException("Could not send request to TNS", EZMQXErrorCode.RestError);
    }
    return parseTNSResponse(response);
  }

  /**
   * Terminate EZMQX subscriber.
   *
   */
  public synchronized void terminate() throws EZMQXException {
    if (mTerminated.get()) {
      throw new EZMQXException("Subscriber already terminated", EZMQXErrorCode.Terminated);
    }
    if (null != mSubscriber) {
      mSubscriber.stop();
    }
    mTerminated.set(true);
  }

  /**
   * Check whether EZMQX subscriber is terminated or not.
   *
   * @return true if terminated otherwise false.
   */
  public boolean isTerminated() {
    return mTerminated.get();
  }

  /**
   * Get EZMQX topic list.
   *
   * @return list of {@link EZMQXTopic}
   *
   */
  public List<EZMQXTopic> getTopics() {
    return mStoredTopics;
  }
}
