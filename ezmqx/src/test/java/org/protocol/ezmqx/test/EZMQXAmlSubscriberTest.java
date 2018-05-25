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

package org.protocol.ezmqx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.datamodel.aml.AMLException;
import org.datamodel.aml.AMLObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.protocol.ezmqx.EZMQXAmlSubscriber;
import org.protocol.ezmqx.EZMQXConfig;
import org.protocol.ezmqx.EZMQXEndPoint;
import org.protocol.ezmqx.EZMQXErrorCode;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.EZMQXTopic;
import org.protocol.ezmqx.EZMQXAmlSubscriber.EZMQXAmlSubCallback;

public class EZMQXAmlSubscriberTest {
    private EZMQXConfig mConfig;
    private EZMQXAmlSubCallback mCallback;

    @Before
    public void setup() throws EZMQXException {
        mConfig = EZMQXConfig.getInstance();
        mConfig.startStandAloneMode(false, "");
        mCallback = new EZMQXAmlSubCallback() {
            @Override
            public void onMessage(String topic, AMLObject data) {}

            @Override
            public void onError(String topic, EZMQXErrorCode errorCode) {}
        };
        assertNotNull(mConfig);
    }

    @After
    public void after() throws Exception {
        mConfig.reset();
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void getSubscriberTest() throws EZMQXException {
        List<String> amlFilePath = new ArrayList<String>();
        amlFilePath.add(TestUtils.FILE_PATH);
        List<String> IdList = mConfig.addAmlModel(amlFilePath);
        EZMQXEndPoint endPoint = new EZMQXEndPoint(TestUtils.ADDRESS, TestUtils.PORT);
        EZMQXTopic topic = new EZMQXTopic(TestUtils.TOPIC, IdList.get(0), endPoint);
        EZMQXAmlSubscriber subscriber = EZMQXAmlSubscriber.getSubscriber(topic, mCallback);
        assertNotNull(subscriber);
        subscriber.terminate();
    }

    @Test
    public void getSubscriberTest1() throws EZMQXException {
        List<String> amlFilePath = new ArrayList<String>();
        amlFilePath.add(TestUtils.FILE_PATH);
        List<String> IdList = mConfig.addAmlModel(amlFilePath);
        EZMQXEndPoint endPoint = new EZMQXEndPoint(TestUtils.ADDRESS, TestUtils.PORT);
        EZMQXTopic topic = new EZMQXTopic(TestUtils.TOPIC, IdList.get(0), endPoint);
        List<EZMQXTopic> topicList = new ArrayList<EZMQXTopic>();
        topicList.add(topic);
        EZMQXAmlSubscriber subscriber = EZMQXAmlSubscriber.getSubscriber(topicList, mCallback);
        assertNotNull(subscriber);
        subscriber.terminate();
    }

    @Test
    public void getSubscriberTest2() throws EZMQXException {
        List<String> amlFilePath = new ArrayList<String>();
        amlFilePath.add(TestUtils.FILE_PATH);
        thrown.expect(EZMQXException.class);
        EZMQXAmlSubscriber subscriber =
                EZMQXAmlSubscriber.getSubscriber(TestUtils.TOPIC, true, mCallback);
        assertNotNull(subscriber);
        subscriber.terminate();
    }

    @Test
    public void terminateTest() throws EZMQXException, AMLException {
        List<String> amlFilePath = new ArrayList<String>();
        amlFilePath.add(TestUtils.FILE_PATH);
        List<String> IdList = mConfig.addAmlModel(amlFilePath);
        EZMQXEndPoint endPoint = new EZMQXEndPoint(TestUtils.ADDRESS, TestUtils.PORT);
        EZMQXTopic topic = new EZMQXTopic(TestUtils.TOPIC, IdList.get(0), endPoint);
        EZMQXAmlSubscriber subscriber = EZMQXAmlSubscriber.getSubscriber(topic, mCallback);
        assertNotNull(subscriber);
        assertEquals(subscriber.isTerminated(), false);
        subscriber.terminate();
        assertEquals(subscriber.isTerminated(), true);
    }
}
