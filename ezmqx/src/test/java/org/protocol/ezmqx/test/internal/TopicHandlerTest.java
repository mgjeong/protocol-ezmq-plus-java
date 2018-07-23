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

package org.protocol.ezmqx.test.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.internal.RestUtils;
import org.protocol.ezmqx.internal.TopicHandler;

public class TopicHandlerTest {

  @Test
  public void getInstanceTest() throws EZMQXException {
    TopicHandler instance = TopicHandler.getInstance();
    assertNotNull(instance);
  }

  @Test
  public void initHandlerTest() throws EZMQXException {
    TopicHandler instance = TopicHandler.getInstance();
    assertNotNull(instance);
    instance.initHandler();
    instance.initHandler();
    instance.terminateHandler();
  }

  @Test
  public void keepAliveTest() throws EZMQXException {
    TopicHandler instance = TopicHandler.getInstance();
    assertNotNull(instance);
    instance.initHandler();
    assertEquals(instance.getKeepAliveInterval(), -1);
    assertEquals(instance.isKeepAliveStarted(), false);
    instance.updateKeepAliveInterval(1);
    assertEquals(instance.getKeepAliveInterval(), 1000);
    instance.terminateHandler();
  }

  @Test
  public void sendTest() throws EZMQXException {
    TopicHandler instance = TopicHandler.getInstance();
    assertNotNull(instance);
    instance.initHandler();
    instance.send(null, null);
    instance.send(RestUtils.REGISTER, "/topic");
    instance.send(RestUtils.KEEPALIVE, "");
    instance.send(RestUtils.UNREGISTER, "/topic");
    instance.send(RestUtils.SHUTDOWN, "");
    instance.send("UNKNOWN", "");
    instance.terminateHandler();
  }
}
