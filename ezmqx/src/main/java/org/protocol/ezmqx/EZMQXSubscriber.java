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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.core.Response;

import org.datamodel.aml.Representation;
import org.edgexfoundry.ezmq.EZMQContentType;
import org.edgexfoundry.ezmq.EZMQErrorCode;
import org.edgexfoundry.ezmq.EZMQMessage;
import org.edgexfoundry.ezmq.EZMQSubscriber;
import org.edgexfoundry.ezmq.EZMQSubscriber.EZMQSubCallback;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.protocol.ezmqx.internal.EZMQXContext;
import org.protocol.ezmqx.internal.EZMQXRestUtils;
import org.protocol.ezmqx.internal.EZMQXUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is base class of EZMQX subscribers.
 */
public class EZMQXSubscriber {
    protected EZMQXContext mContext;
    protected List<EZMQSubscriber> mSubscribers;
    protected AtomicBoolean mTerminated;
    protected List<EZMQXTopic> mStoredTopics;
    protected Map<String, Representation> mAMLRepDic;
    private EZMQXSubCallback mCallback;

    private final static EdgeXLogger logger =
            EdgeXLoggerFactory.getEdgeXLogger(EZMQXSubscriber.class);

    protected EZMQXSubscriber() throws EZMQXException {
        mTerminated = new AtomicBoolean(false);
        mContext = EZMQXContext.getInstance();
        mSubscribers = new ArrayList<EZMQSubscriber>();
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
        boolean result = EZMQXUtils.validateTopic(topic);
        if (false == result) {
            throw new EZMQXException("Invalid topic", EZMQXErrorCode.InvalidTopic);
        }
        List<EZMQXTopic> verified = new ArrayList<EZMQXTopic>();
        if (mContext.isTnsEnabled()) {
            verified = verifyTopics(topic, isHierarchical);
            if (verified.isEmpty()) {
                throw new EZMQXException("Could not find matched topic",
                        EZMQXErrorCode.NoTopicMatched);
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
            getSession(topic);
            mStoredTopics.add(topic);
        }
    }

    protected void getSession(EZMQXTopic topic) throws EZMQXException {
        EZMQXEndPoint endPoint = topic.getEndPoint();
        EZMQSubscriber subscriber =
                new EZMQSubscriber(endPoint.getAddr(), endPoint.getPort(), new EZMQSubCallback() {
                    public void onMessageCB(String topic, EZMQMessage ezmqMessage) {
                        if (EZMQContentType.EZMQ_CONTENT_TYPE_BYTEDATA == ezmqMessage
                                .getContentType()) {
                            mCallback.onMessage(topic, ezmqMessage);
                        } else {
                        }
                    }

                    public void onMessageCB(EZMQMessage ezmqMessage) {}
                });

        if (EZMQErrorCode.EZMQ_OK != subscriber.start()) {
            throw new EZMQXException("Could not connect endpoint: " + endPoint.toString(),
                    EZMQXErrorCode.SessionUnavailable);
        }
        if (EZMQErrorCode.EZMQ_OK != subscriber.subscribe(topic.getName())) {
            throw new EZMQXException("Could not Subscribe to endpoint: " + endPoint.toString(),
                    EZMQXErrorCode.SessionUnavailable);
        }
        mSubscribers.add(subscriber);
    }

    private List<EZMQXTopic> parseTNSResponse(Response response) throws EZMQXException {
        if (null == response) {
            throw new EZMQXException("Could not get topic", EZMQXErrorCode.RestError);
        }
        logger.debug("[TNS get topic] Status code: " + response.getStatus());
        if (response.getStatus() != EZMQXRestUtils.HTTP_OK) {
            throw new EZMQXException("Could not discover topic", EZMQXErrorCode.RestError);
        }

        String jsonString = response.readEntity(String.class);
        logger.debug("[TNS get topic] Response: " + jsonString);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<EZMQXTopic> topics = new ArrayList<EZMQXTopic>();
        JsonNode propertiesNode = root.path(EZMQXRestUtils.PAYLOAD_TOPICS);
        for (JsonNode node : propertiesNode) {
            if (node.has(EZMQXRestUtils.PAYLOAD_NAME) && node.has(EZMQXRestUtils.PAYLOAD_DATAMODEL)
                    && node.has(EZMQXRestUtils.PAYLOAD_ENDPOINT)) {
                String name = node.path(EZMQXRestUtils.PAYLOAD_NAME).asText();
                String dataModel = node.path(EZMQXRestUtils.PAYLOAD_DATAMODEL).asText();
                String ep = node.path(EZMQXRestUtils.PAYLOAD_ENDPOINT).asText();
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
        String topicURL = EZMQXRestUtils.HTTP_PREFIX + mContext.getTnsAddr() + EZMQXRestUtils.COLON
                + EZMQXRestUtils.TNS_KNOWN_PORT + EZMQXRestUtils.PREFIX + EZMQXRestUtils.TOPIC;
        String query = EZMQXRestUtils.QUERY_NAME + topic + EZMQXRestUtils.QUERY_HIERARCHICAL
                + (isHierarchical == true ? EZMQXRestUtils.QUERY_TRUE : EZMQXRestUtils.QUERY_FALSE);
        logger.debug("[TNS get topic] Rest URL: " + topicURL);
        logger.debug("[TNS get topic] Query: " + query);

        ResteasyClient mRestClient = new ResteasyClientBuilder()
                .establishConnectionTimeout(EZMQXRestUtils.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .build();
        ResteasyWebTarget target = (ResteasyWebTarget) mRestClient
                .target(topicURL + EZMQXRestUtils.QUESTION_MARK + query);
        Response response = null;
        try {
            response = target.request().get();
        } catch (Exception e) {
            logger.debug("Caught exeption : " + e.getMessage());
            return parseTNSResponse(response);
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
        for (EZMQSubscriber subscriber : mSubscribers) {
            subscriber.stop();
        }
        mSubscribers.clear();
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
