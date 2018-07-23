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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.protocol.ezmqx.EZMQXAmlModelInfo;
import org.protocol.ezmqx.EZMQXAmlPublisher;
import org.protocol.ezmqx.EZMQXConfig;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.internal.RestClientFactoryInterface;
import org.protocol.ezmqx.internal.RestFactory;
import org.protocol.ezmqx.test.internal.FakeRestClientFactory;

public class EZMQXAmlPublisherTest {
  private EZMQXConfig mConfig;

  @Before
  public void setup() throws EZMQXException {
    mConfig = EZMQXConfig.getInstance();
    RestClientFactoryInterface restFactory = new FakeRestClientFactory();
    RestFactory.getInstance().setFactory(restFactory);
    mConfig.startStandAloneMode(false, "");
    assertNotNull(mConfig);
  }

  @After
  public void after() throws Exception {
    try {
      mConfig.reset();
    } catch (EZMQXException e) {

    }
  }

  @Test
  public void getPublisherTest() throws EZMQXException {
    List<String> amlFilePath = new ArrayList<String>();
    amlFilePath.add(TestUtils.FILE_PATH);
    List<String> IdList = mConfig.addAmlModel(amlFilePath);
    EZMQXAmlPublisher publisher = EZMQXAmlPublisher.getPublisher(TestUtils.TOPIC,
        EZMQXAmlModelInfo.AML_MODEL_ID, IdList.get(0), 5563);
    assertNotNull(publisher);
    publisher.terminate();
  }

  @Test
  public void getPublisherTest1() throws EZMQXException {
    EZMQXAmlPublisher publisher = EZMQXAmlPublisher.getPublisher(TestUtils.TOPIC,
        EZMQXAmlModelInfo.AML_FILE_PATH, TestUtils.FILE_PATH, 5563);
    assertNotNull(publisher);
    publisher.terminate();
  }

  @Test(expected = EZMQXException.class)
  public void getPublisherTest2() throws EZMQXException, AMLException {
    mConfig.reset();
    try {
      mConfig.startDockerMode();
    } catch (Exception e) {
    }

    EZMQXAmlPublisher publisher = EZMQXAmlPublisher.getPublisher(TestUtils.TOPIC,
        EZMQXAmlModelInfo.AML_FILE_PATH, TestUtils.FILE_PATH, 5563);
    assertNotNull(publisher);
    publisher.terminate();
  }

  @Test(expected = EZMQXException.class)
  public void getPublisherTest3() throws EZMQXException, AMLException {
    mConfig.reset();
    try {
      mConfig.startStandAloneMode(true, "0.0.0.0");
    } catch (Exception e) {
    }

    EZMQXAmlPublisher publisher = EZMQXAmlPublisher.getPublisher(TestUtils.TOPIC,
        EZMQXAmlModelInfo.AML_FILE_PATH, TestUtils.FILE_PATH, 5563);
    assertNotNull(publisher);
    publisher.terminate();
  }

  @Test(expected = EZMQXException.class)
  public void getPublisherTest4() throws EZMQXException {
    EZMQXAmlPublisher publisher = EZMQXAmlPublisher.getPublisher(TestUtils.INVALID_TOPIC,
        EZMQXAmlModelInfo.AML_FILE_PATH, TestUtils.FILE_PATH, 5563);
    assertNotNull(publisher);
  }

  @Test
  public void publishTest() throws EZMQXException, AMLException {
    EZMQXAmlPublisher publisher = EZMQXAmlPublisher.getPublisher(TestUtils.TOPIC,
        EZMQXAmlModelInfo.AML_FILE_PATH, TestUtils.FILE_PATH, 5563);
    assertNotNull(publisher);
    publisher.publish(TestUtils.getAMLObject());
    publisher.terminate();
  }

  @Test
  public void getTopicTest() throws EZMQXException, AMLException {
    EZMQXAmlPublisher publisher = EZMQXAmlPublisher.getPublisher(TestUtils.TOPIC,
        EZMQXAmlModelInfo.AML_FILE_PATH, TestUtils.FILE_PATH, 5563);
    assertNotNull(publisher);
    assertEquals(publisher.getTopic().getName(), TestUtils.TOPIC);
    publisher.terminate();
  }

  @Test
  public void terminateTest() throws EZMQXException, AMLException {
    EZMQXAmlPublisher publisher = EZMQXAmlPublisher.getPublisher(TestUtils.TOPIC,
        EZMQXAmlModelInfo.AML_FILE_PATH, TestUtils.FILE_PATH, 5563);
    assertNotNull(publisher);
    assertEquals(publisher.isTerminated(), false);
    publisher.terminate();
    assertEquals(publisher.isTerminated(), true);
  }
}
