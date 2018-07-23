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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.internal.Utils;

public class UtilsTest {
  public Utils utils = new Utils();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void validateTopicPositiveTest() throws EZMQXException {
    String topic = "/sdf/sdf/sdfgfg/12";
    assertEquals(true, Utils.validateTopic(topic));

    topic = "/sdfsdf/123sdfs/t312xsdf/213lkj_";
    assertEquals(true, Utils.validateTopic(topic));

    topic = "/-0/ssd............fdfadsf/fdsg-/-0-";
    assertEquals(true, Utils.validateTopic(topic));

    topic = "/sdf/sdf/sdfgfg";
    assertEquals(true, Utils.validateTopic(topic));

    // topic = "/*/-/*/-/-/-______/-/-/-3223434223424323421"; //Enable once
    // TNS
    // support wild card
    // assertEquals(true, EZMQXUtils.validateTopic(topic));

    topic = "/123/sdafdsaf/44___kk/2232/abicls";
    assertEquals(true, Utils.validateTopic(topic));
  }

  @Test
  public void validateTopicNegativeTest() throws EZMQXException {
    String topic = "/";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "//////)///(///////////";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "//3/1/2/1/2/3";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "312123/";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "/fds+dsfg-23-/";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "/312?_!_12--3/";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "/312?_!_12--3/";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "/312_!_123/sda";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "/        /312123";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "-=0/ssdfdfadsf/fdsg-/-0-";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "/sdfsdf*/";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "/sdf/sdf/sdfgfg/";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "sdf/sdf/sdf/sdfgfg/";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "/sdf/sdf/sdf gfg/12";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "/sdfsdf/123sdfs/t312xsdf*/213lkj_+/";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "sdfsdf/123sdfs/t312xsdf*/213lkj_+/";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "/sdfsdf/123sdfs//t312xsdf*/213lkj_+";
    assertEquals(false, Utils.validateTopic(topic));

    topic = null;
    assertEquals(false, Utils.validateTopic(topic));

    topic = "";
    assertEquals(false, Utils.validateTopic(topic));

    topic = "/#topic";
    assertEquals(false, Utils.validateTopic(topic));
  }
}
