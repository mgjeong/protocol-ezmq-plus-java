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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.datamodel.aml.AMLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.protocol.ezmqx.EZMQXAmlModelInfo;
import org.protocol.ezmqx.EZMQXAmlPublisher;
import org.protocol.ezmqx.EZMQXConfig;
import org.protocol.ezmqx.EZMQXEndPoint;
import org.protocol.ezmqx.EZMQXErrorCode;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.EZMQXTopic;
import org.protocol.ezmqx.EZMQXXmlSubscriber;
import org.protocol.ezmqx.EZMQXXmlSubscriber.EZMQXXmlSubCallback;
import org.protocol.ezmqx.internal.RestClientFactoryInterface;
import org.protocol.ezmqx.internal.RestFactory;
import org.protocol.ezmqx.test.internal.FakeRestClientFactory;

public class EZMQXXmlSubscriberTest {
  private EZMQXConfig mConfig;
  private EZMQXXmlSubCallback mCallback;
  private Lock mTerminateLock;
  private java.util.concurrent.locks.Condition mCondVar;
  private int mEventCount;
  private final int TOTAL_EVENTS = 5;

  @Before
  public void setup() throws EZMQXException {
    mConfig = EZMQXConfig.getInstance();
    mConfig.startStandAloneMode(false, "");
    mTerminateLock = new ReentrantLock();
    mCondVar = mTerminateLock.newCondition();
    mEventCount = 0;
    RestClientFactoryInterface restFactory = new FakeRestClientFactory();
    RestFactory.getInstance().setFactory(restFactory);
    mCallback = new EZMQXXmlSubCallback() {
      @Override
      public void onMessage(String topic, String data) {
        mEventCount++;
      }

      @Override
      public void onError(String topic, EZMQXErrorCode errorCode) {
        mEventCount++;
      }
    };
    assertNotNull(mConfig);
  }

  @After
  public void after() throws Exception {
    try {
      mConfig.reset();
    } catch (Exception e) {

    }
  }

  void publish() throws EZMQXException, AMLException {
    EZMQXAmlPublisher publisher = EZMQXAmlPublisher.getPublisher(TestUtils.TOPIC,
        EZMQXAmlModelInfo.AML_FILE_PATH, TestUtils.FILE_PATH, 5562);
    assertNotNull(publisher);

    for (int i = 0; i <= 5; i++) {
      publisher.publish(TestUtils.getAMLObject());
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    publisher.terminate();
    try {
      mTerminateLock.lock();
      mCondVar.signalAll();
    } catch (Exception e) {
    } finally {
      mTerminateLock.unlock();
    }
  }

  @Test
  public void subscriberStandAloneTest() throws EZMQXException, AMLException {
    List<String> amlFilePath = new ArrayList<String>();
    amlFilePath.add(TestUtils.FILE_PATH);
    List<String> IdList = mConfig.addAmlModel(amlFilePath);
    EZMQXEndPoint endPoint = new EZMQXEndPoint(TestUtils.LOCAL_HOST, TestUtils.PORT);
    EZMQXTopic topic = new EZMQXTopic(TestUtils.TOPIC, IdList.get(0), endPoint);
    EZMQXXmlSubscriber subscriber = EZMQXXmlSubscriber.getSubscriber(topic, mCallback);
    assertNotNull(subscriber);

    // Thread to publish data on socket
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          publish();
        } catch (Exception e) {
        }
      }
    });
    thread.start();

    // Prevent thread from exit till publisher stopped
    try {
      mTerminateLock.lock();
      mCondVar.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      mTerminateLock.unlock();
    }
    assertEquals(TOTAL_EVENTS, mEventCount);
    subscriber.terminate();
  }

  @Test(expected = EZMQXException.class)
  public void subscriberDockerTest() throws EZMQXException, AMLException {
    mConfig.reset();
    try {
      mConfig.startDockerMode();
    } catch (EZMQXException e) {

    }
    EZMQXXmlSubscriber subscriber =
        EZMQXXmlSubscriber.getSubscriber(TestUtils.TOPIC, true, mCallback);
    assertNotNull(subscriber);
  }

  @Test(expected = EZMQXException.class)
  public void getSubscriberNegativeTest() throws EZMQXException, AMLException {
    mConfig.reset();
    EZMQXEndPoint endPoint = new EZMQXEndPoint("127.0.0.1", 5562);
    EZMQXTopic topic = new EZMQXTopic(TestUtils.TOPIC, "robot_1.0", endPoint);
    List<EZMQXTopic> topicList = new ArrayList<EZMQXTopic>();
    topicList.add(topic);
    EZMQXXmlSubscriber subscriber = EZMQXXmlSubscriber.getSubscriber(topicList, mCallback);
    assertNotNull(subscriber);
  }
}
