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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.edgexfoundry.ezmq.EZMQCallback;
import org.edgexfoundry.ezmq.EZMQErrorCode;
import org.edgexfoundry.ezmq.EZMQPublisher;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.protocol.ezmqx.internal.EZMQXContext;
import org.protocol.ezmqx.internal.EZMQXRestUtils;
import org.protocol.ezmqx.internal.EZMQXTopicHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class is base class of EZMQX publisher.
 */
public class EZMQXPublisher {
    protected EZMQXContext mContext;
    protected EZMQXTopicHandler mHandler;
    protected EZMQPublisher mPublisher;
    protected EZMQCallback mCallback;
    protected EZMQXTopic mTopic;
    protected AtomicBoolean mTerminated;
    protected int mLocalPort;

    private final static EdgeXLogger logger =
            EdgeXLoggerFactory.getEdgeXLogger(EZMQXPublisher.class);

    protected EZMQXPublisher() {
        mContext = EZMQXContext.getInstance();
    }

    // finalize method to be called by Java Garbage collector
    // before destroying
    // this object.
    @Override
    protected void finalize() throws EZMQXException {
        terminate();
    }

    protected void initialize(int optionalPort) throws EZMQXException {
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
            throw new EZMQXException("Could not create ezmq publisher",
                    EZMQXErrorCode.UnKnownState);
        }
        if (EZMQErrorCode.EZMQ_OK != mPublisher.start()) {
            throw new EZMQXException("Could not start ezmq publisher", EZMQXErrorCode.UnKnownState);
        }

        // Init topic handler
        if (!mContext.isStandAlone()) {
            mHandler = EZMQXTopicHandler.getInstance();
            mHandler.initHandler();
            logger.debug("Initialized topic handler");
        }
        mTerminated = new AtomicBoolean(false);
    }

    private boolean parseTopicResponse(Response response) throws EZMQXException {
        logger.debug("[TNS register topic] Status code: " + response.getStatus());
        if (response.getStatus() != EZMQXRestUtils.HTTP_CREATED) {
            logger.debug("[TNS register topic] Status code : " + response.getStatus());
            mPublisher.stop(); // free the EZMQ publisher instance
            throw new EZMQXException("Could not register topic", EZMQXErrorCode.RestError);
        }

        String jsonString = response.readEntity(String.class);
        logger.debug("[TNS register topic] Response: " + jsonString);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (root.has(EZMQXRestUtils.PAYLOAD_KEEPALIVE_INTERVAL)) {
            int interval = root.path(EZMQXRestUtils.PAYLOAD_KEEPALIVE_INTERVAL).asInt();
            logger.debug("[[Register topic] Keep Alive interval: " + interval);
            if (interval < 1) {
                mPublisher.stop(); // free the EZMQ publisher instance
                throw new EZMQXException("Invalid keepAlive interval", EZMQXErrorCode.RestError);
            }
            logger.debug("[[Register topic] Current Keep Alive interval: "
                    + mHandler.getKeepAliveInterval());
            if (mHandler.getKeepAliveInterval() < 1) {
                mHandler.updateKeepAliveInterval(interval);
            }
            if (!mHandler.isKeepAliveStarted()) {
                EZMQXTopicHandler.getInstance().send(EZMQXRestUtils.KEEPALIVE, "");
            }
        }
        return true;
    }

    protected void registerTopic(EZMQXTopic topic) throws EZMQXException {
        mTopic = topic;
        if (!(mContext.isTnsEnabled())) {
            return;
        }

        // Send post request to TNS server
        String topicURL = EZMQXRestUtils.HTTP_PREFIX + mContext.getTnsAddr() + EZMQXRestUtils.COLON
                + EZMQXRestUtils.TNS_KNOWN_PORT + EZMQXRestUtils.PREFIX + EZMQXRestUtils.TOPIC;
        logger.debug("[TNS register topic] Rest URL: " + topicURL);
        // Form post payload
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        JsonNode nodeTopic = mapper.createObjectNode();
        ((ObjectNode) nodeTopic).put(EZMQXRestUtils.PAYLOAD_NAME, topic.getName());
        ((ObjectNode) nodeTopic).put(EZMQXRestUtils.PAYLOAD_ENDPOINT,
                topic.getEndPoint().toString());
        ((ObjectNode) nodeTopic).put(EZMQXRestUtils.PAYLOAD_DATAMODEL, topic.getDatamodel());
        ((ObjectNode) rootNode).set(EZMQXRestUtils.PAYLOAD_TOPIC, nodeTopic);
        String payload = rootNode.toString();
        logger.debug("[TNS register topic] payload : " + payload);

        ResteasyClient restClient = new ResteasyClientBuilder()
                .establishConnectionTimeout(EZMQXRestUtils.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .build();
        ResteasyWebTarget target = (ResteasyWebTarget) restClient.target(topicURL);
        Response response;
        try {
            response = target.request().post(Entity.entity(payload, MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            logger.debug("Caught exeption : " + e.getMessage());
            return;
        }
        if (parseTopicResponse(response)) {
            // send request to add topic to list
            EZMQXTopicHandler.getInstance().send(EZMQXRestUtils.REGISTER, topic.getName());
        }
    }

    protected void unRegisterTopic(EZMQXTopic topic) throws EZMQXException {
        if (!(mContext.isTnsEnabled())) {
            return;
        }
        String topicURL = EZMQXRestUtils.HTTP_PREFIX + mContext.getTnsAddr() + EZMQXRestUtils.COLON
                + EZMQXRestUtils.TNS_KNOWN_PORT + EZMQXRestUtils.PREFIX + EZMQXRestUtils.TOPIC;
        String query = EZMQXRestUtils.QUERY_NAME + topic.getName();
        logger.debug("[TNS unregister topic] Rest URL: " + topicURL);
        logger.debug("[TNS unregister topic] Query: " + query);
        ResteasyClient restClient = new ResteasyClientBuilder()
                .establishConnectionTimeout(EZMQXRestUtils.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .build();
        ResteasyWebTarget target = (ResteasyWebTarget) restClient.target(topicURL + query);
        try {
            Response response = target.request().delete();
            logger.debug("[TNS unregister topic] Response code: " + response.getStatus());
        } catch (Exception e) {
            logger.debug("Caught exeption : " + e.getMessage());
            return;
        }
        // send request to topicHandler for removing topic from list
        EZMQXTopicHandler.getInstance().send(EZMQXRestUtils.UNREGISTER, topic.getName());
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
            terminate();
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
}
