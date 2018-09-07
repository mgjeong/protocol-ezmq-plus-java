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
import org.junit.Test;
import org.protocol.ezmqx.EZMQXEndPoint;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.EZMQXTopic;

public class EZMQXTopicTest {
  private final String DATA_MODEL = "Robot_1.1";
  private final EZMQXEndPoint END_POINT = new EZMQXEndPoint(TestUtils.ADDRESS, TestUtils.PORT);

  @Test
  public void getNameTest() throws EZMQXException {
    EZMQXTopic instance = new EZMQXTopic(TestUtils.TOPIC, DATA_MODEL, false, END_POINT);
    assertNotNull(instance);
    assertEquals(instance.getName(), TestUtils.TOPIC);
  }

  @Test
  public void getDataModelTest() throws EZMQXException {
    EZMQXTopic instance = new EZMQXTopic(TestUtils.TOPIC, DATA_MODEL, false, END_POINT);
    assertNotNull(instance);
    assertEquals(instance.getDatamodel(), DATA_MODEL);
  }

  @Test
  public void getEndPointTest() throws EZMQXException {
    EZMQXTopic instance = new EZMQXTopic(TestUtils.TOPIC, DATA_MODEL, false, END_POINT);
    assertNotNull(instance);
    EZMQXEndPoint endPoint = instance.getEndPoint();
    assertEquals(endPoint.getAddr(), TestUtils.ADDRESS);
    assertEquals(endPoint.getPort(), TestUtils.PORT);
  }

  @Test
  public void isSecuredTest() throws EZMQXException {
    EZMQXTopic instance = new EZMQXTopic(TestUtils.TOPIC, DATA_MODEL, true, END_POINT);
    assertNotNull(instance);
    assertEquals(instance.isSecured(), true);
  }
}
