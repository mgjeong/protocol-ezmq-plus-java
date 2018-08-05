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
import org.protocol.ezmqx.EZMQXErrorCode;
import org.protocol.ezmqx.EZMQXException;

public class EZMQXExceptionTest {
  private final String MSG = "This is testing exception";
  private final EZMQXErrorCode ERROR_CODE = EZMQXErrorCode.NotInitialized;

  @Test
  public void getMsgTest() {
    EZMQXException instance = new EZMQXException(MSG, ERROR_CODE);
    assertNotNull(instance);
    assertEquals(instance.getMsg(), MSG);
  }

  @Test
  public void getCodeTest() throws EZMQXException {
    EZMQXException instance = new EZMQXException(MSG, ERROR_CODE);
    assertNotNull(instance);
    assertEquals(instance.getCode(), ERROR_CODE);
  }
}
