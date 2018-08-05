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
import java.util.List;
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
 * This class represents EZMQX topic discovery. It provides APIs for
 * Topic discovery on TNS server.
 */
public class EZMQXTopicDiscovery {
  private Context mContext;
  private final static EdgeXLogger logger =
      EdgeXLoggerFactory.getEdgeXLogger(EZMQXTopicDiscovery.class);

  /**
   * Constructor for EZMQX topic discovery.
   */
  public EZMQXTopicDiscovery() throws EZMQXException {
    mContext = Context.getInstance();
    if (!(mContext.isInitialized())) {
      mContext = null;
      throw new EZMQXException("Context not initialized", EZMQXErrorCode.NotInitialized);
    }
  }

  /**
   * Query the given topic to TNS [Topic name server] server.
   *
   * @param topic Topic to be search on TNS server.
   * @return {@link EZMQXTopic}
   */
  public EZMQXTopic query(String topic) throws EZMQXException {
    return queryInternal(topic, false).get(0);
  }

  /**
   * Query the given topic to TNS [Topic name server] server. It will
   * send query request with hierarchical option.
   *
   * For example: If topic name is /Topic then in success case TNS will
   * return /Topic/A, /Topic/A/B etc.
   *
   * @param topic Topic to be search on TNS server.
   * @return List of {@link EZMQXTopic}
   */
  public List<EZMQXTopic> hierarchicalQuery(String topic) throws EZMQXException {
    return queryInternal(topic, true);
  }

  private List<EZMQXTopic> queryInternal(String topic, boolean isHierarchical)
      throws EZMQXException {
    if (null == mContext) {
      throw new EZMQXException("Context not created", EZMQXErrorCode.UnKnownState);
    }
    if (mContext.isTerminated()) {
      throw new EZMQXException("Context terminated", EZMQXErrorCode.Terminated);
    }
    if (!mContext.isTnsEnabled()) {
      throw new EZMQXException("Could not use discovery without tns server",
          EZMQXErrorCode.TnsNotAvailable);
    }
    boolean result = Utils.validateTopic(topic);
    if (false == result) {
      throw new EZMQXException("Invalid topic", EZMQXErrorCode.InvalidTopic);
    }
    return verifyTopic(topic, isHierarchical);
  }

  private List<EZMQXTopic> verifyTopic(String topic, boolean isHierarchical) throws EZMQXException {
    String tnsURL = mContext.getTnsAddr() + RestUtils.PREFIX + RestUtils.TOPIC;
    logger.debug("[Topic discovery] Rest URL: " + tnsURL);
    String query = RestUtils.QUERY_NAME + topic + RestUtils.QUERY_HIERARCHICAL
        + (isHierarchical == true ? RestUtils.QUERY_TRUE : RestUtils.QUERY_FALSE);
    logger.debug("[Topic discovery] Query: " + query);
    RestResponse response = null;
    RestFactory restClient = RestFactory.getInstance();
    try {
      response = restClient.get(tnsURL, query);
    } catch (Exception e) {
      logger.debug("Caught exeption : " + e.getMessage());
      throw new EZMQXException("Could not send request to TNS", EZMQXErrorCode.RestError);
    }
    return parseTNSResponse(response);
  }

  private List<EZMQXTopic> parseTNSResponse(RestResponse response) throws EZMQXException {
    if (null == response) {
      throw new EZMQXException("Could not discover topic", EZMQXErrorCode.RestError);
    }
    logger.debug("[TNS discover topic] Status code: " + response.getStatusCode());
    if (response.getStatusCode() != RestUtils.HTTP_OK) {
      throw new EZMQXException("Could not discover topic", EZMQXErrorCode.RestError);
    }

    String jsonString = response.getResponse();
    logger.debug("[TNS discover topic] Response: " + jsonString);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = null;
    try {
      root = mapper.readTree(jsonString);
    } catch (IOException e) {
      throw new EZMQXException("Could not parse response", EZMQXErrorCode.RestError);
    }

    List<EZMQXTopic> topics = new ArrayList<EZMQXTopic>();
    JsonNode topicNode = root.path(RestUtils.PAYLOAD_TOPICS);
    for (JsonNode node : topicNode) {
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
}
