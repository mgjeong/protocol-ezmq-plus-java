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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.protocol.ezmqx.EZMQXConfig;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.internal.RestClientFactoryInterface;
import org.protocol.ezmqx.internal.RestFactory;
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

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void startStandAloneModeTest() throws EZMQXException {
    mConfig.startStandAloneMode(false, "");
  }

  @Test
  public void startStandAloneModeWithTNSTest() throws EZMQXException {
    mConfig.startStandAloneMode(true, "127.0.0.1");
  }

  @Test(expected = EZMQXException.class)
  public void startDockerModeTest() throws EZMQXException {
    mConfig.startDockerMode();
  }

  @Test(expected = EZMQXException.class)
  public void startStandAloneModeNagativeTest() throws EZMQXException {
    mConfig.startStandAloneMode(false, "");
    mConfig.startStandAloneMode(false, "");
    mConfig.startDockerMode();
  }

  @Test(expected = EZMQXException.class)
  public void startStandAloneModeNagativeTest1() throws EZMQXException {
    mConfig.startStandAloneMode(false, "");
    mConfig.startDockerMode();
  }

  @Test
  public void addAmlModelTest() throws EZMQXException {
    mConfig.startStandAloneMode(false, "");
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
