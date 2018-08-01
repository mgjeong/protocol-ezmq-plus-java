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

import static org.junit.Assert.assertNotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.protocol.ezmqx.EZMQXConfig;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.EZMQXTopicDiscovery;
import org.protocol.ezmqx.internal.RestClientFactoryInterface;
import org.protocol.ezmqx.internal.RestFactory;
import org.protocol.ezmqx.test.internal.FakeRestClient;
import org.protocol.ezmqx.test.internal.FakeRestClientFactory;

public class EZMQXTopicDiscoveryTest {

  private EZMQXConfig mConfig;

  @Before
  public void setup() throws EZMQXException {
    mConfig = EZMQXConfig.getInstance();
    assertNotNull(mConfig);
    RestClientFactoryInterface restFactory = new FakeRestClientFactory();
    RestFactory.getInstance().setFactory(restFactory);
  }

  @After
  public void after() throws Exception {
    try {
      mConfig.reset();
    } catch (EZMQXException e) {
    }
  }

  @Test
  public void constructorTest() throws EZMQXException {
    mConfig.startStandAloneMode(TestUtils.LOCAL_HOST, false, "");
    EZMQXTopicDiscovery instance = new EZMQXTopicDiscovery();
    assertNotNull(instance);
  }

  @Test
  public void queryTest() throws EZMQXException {
    mConfig.startStandAloneMode(TestUtils.ADDRESS, true, TestUtils.TNS_ADDRESS);
    EZMQXTopicDiscovery instance = new EZMQXTopicDiscovery();
    assertNotNull(instance);
    FakeRestClient.setResponse(TestUtils.TOPIC_DISCOVERY_URL,
        TestUtils.VALID_TOPIC_DISCOVERY_RESPONSE);
    instance.query(TestUtils.TOPIC);
  }

  @Test
  public void hierarchicalQueryTest() throws EZMQXException {
    mConfig.startStandAloneMode(TestUtils.ADDRESS, true, TestUtils.TNS_ADDRESS);
    EZMQXTopicDiscovery instance = new EZMQXTopicDiscovery();
    assertNotNull(instance);
    FakeRestClient.setResponse(TestUtils.TOPIC_DISCOVERY_H_URL,
        TestUtils.VALID_TOPIC_DISCOVERY_RESPONSE);
    instance.hierarchicalQuery(TestUtils.TOPIC);
  }

  @Test(expected = EZMQXException.class)
  public void queryNegativeTest() throws EZMQXException {
    EZMQXTopicDiscovery instance = new EZMQXTopicDiscovery();
    assertNotNull(instance);
    instance.query(TestUtils.TOPIC);
  }

  @Test(expected = EZMQXException.class)
  public void hierarchicalQueryNegativeTest() throws EZMQXException {
    EZMQXTopicDiscovery instance = new EZMQXTopicDiscovery();
    assertNotNull(instance);
    instance.hierarchicalQuery(TestUtils.TOPIC);
  }
}
