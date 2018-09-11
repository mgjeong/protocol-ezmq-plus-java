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
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.protocol.ezmqx.EZMQXConfig;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.internal.RestClientFactoryInterface;
import org.protocol.ezmqx.internal.RestFactory;
import org.protocol.ezmqx.test.internal.FakeRestClient;
import org.protocol.ezmqx.test.internal.FakeRestClientFactory;

public class EZMQXConfigTest {

  private EZMQXConfig mConfig;

  @Before
  public void setup() throws EZMQXException {
    mConfig = EZMQXConfig.getInstance();
    assertNotNull(mConfig);
    RestClientFactoryInterface restFactory = new FakeRestClientFactory();
    RestFactory.getInstance().setFactory(restFactory);
  }

  @After
  public void after() {
    try {
      mConfig.reset();
    } catch (EZMQXException e) {
    }
  }

  @Test
  public void startStandAloneModeTest() throws EZMQXException {
    mConfig.startStandAloneMode(TestUtils.LOCAL_HOST, false, "");
  }

  @Test
  public void startStandAloneModeWithTNSTest() throws EZMQXException {
    mConfig.startStandAloneMode(TestUtils.ADDRESS, true, TestUtils.TNS_ADDRESS);
  }

  @Test(expected = EZMQXException.class)
  public void startStandAloneModeTwiceTest() throws EZMQXException {
    mConfig.startStandAloneMode(TestUtils.LOCAL_HOST, false, "");
    mConfig.startStandAloneMode(TestUtils.LOCAL_HOST, false, "");
  }

  @Test
  public void startDockerModeTest() throws EZMQXException {
    FakeRestClient.setResponse(TestUtils.CONFIG_URL, TestUtils.VALID_CONFIG_RESPONSE);
    FakeRestClient.setResponse(TestUtils.TNS_INFO_URL, TestUtils.VALID_TNS_INFO_RESPONSE);
    FakeRestClient.setResponse(TestUtils.RUNNING_APPS_URL, TestUtils.VALID_RUNNING_APPS_RESPONSE);
    FakeRestClient.setResponse(TestUtils.RUNNING_APP_INFO_URL, TestUtils.RUNNING_APP_INFO_RESPONSE);
    mConfig.startDockerMode(TestUtils.TNS_CONFIG_FILE_PATH);
  }

  @Test(expected = EZMQXException.class)
  public void startStandAloneModeNagativeTest1() throws EZMQXException {
    mConfig.startStandAloneMode(TestUtils.LOCAL_HOST, false, "");
    mConfig.startDockerMode(TestUtils.TNS_CONFIG_FILE_PATH);
  }

  @Test
  public void addAmlModelTest() throws EZMQXException {
    mConfig.startStandAloneMode(TestUtils.LOCAL_HOST, false, "");
    List<String> amlFilePath = new ArrayList<String>();
    amlFilePath.add(TestUtils.FILE_PATH);
    List<String> IdList = mConfig.addAmlModel(amlFilePath);
    assertNotNull(IdList);
  }

  @Test(expected = EZMQXException.class)
  public void addAmlModelNegativeTest() throws EZMQXException {
    List<String> amlFilePath = new ArrayList<String>();
    amlFilePath.add(TestUtils.FILE_PATH);
    mConfig.addAmlModel(amlFilePath);
  }

  @Test(expected = EZMQXException.class)
  public void resetNTest() throws EZMQXException {
    EZMQXConfig.getInstance().reset();
  }
}
