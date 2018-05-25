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

public class EZMQXEndPointTest {
    public static final String IP_PORT = TestUtils.ADDRESS + ":" + TestUtils.PORT;

    @Test
    public void constructorTest() throws EZMQXException {
        EZMQXEndPoint instance = new EZMQXEndPoint(IP_PORT);
        assertNotNull(instance);
        assertEquals(instance.getAddr(), TestUtils.ADDRESS);
        assertEquals(instance.getPort(), TestUtils.PORT);
        assertEquals(instance.toString(), IP_PORT);
        instance = new EZMQXEndPoint(TestUtils.ADDRESS);
        assertNotNull(instance);
        assertEquals(instance.getAddr(), TestUtils.ADDRESS);
        assertEquals(instance.getPort(), -1);
    }

    @Test
    public void constructorTest1() throws EZMQXException {
        EZMQXEndPoint instance = new EZMQXEndPoint(TestUtils.ADDRESS, TestUtils.PORT);
        assertNotNull(instance);
        assertEquals(instance.getAddr(), TestUtils.ADDRESS);
        assertEquals(instance.getPort(), TestUtils.PORT);
        assertEquals(instance.toString(), IP_PORT);
    }
}
