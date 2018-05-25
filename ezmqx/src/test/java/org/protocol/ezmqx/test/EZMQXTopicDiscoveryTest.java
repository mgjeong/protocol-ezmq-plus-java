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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.protocol.ezmqx.EZMQXConfig;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.EZMQXTopicDiscovery;

public class EZMQXTopicDiscoveryTest {

    private EZMQXConfig mConfig;

    @Before
    public void setup() throws EZMQXException {
        mConfig = EZMQXConfig.getInstance();
        assertNotNull(mConfig);
    }

    @After
    public void after() throws Exception {
        mConfig.reset();
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void constrctorTest() throws EZMQXException {
        mConfig.startStandAloneMode(false, "");
        EZMQXTopicDiscovery instance = new EZMQXTopicDiscovery();
        assertNotNull(instance);
    }

    @Test
    public void queryTest() throws EZMQXException {
        mConfig.startStandAloneMode(true, "127.0.0.1");
        EZMQXTopicDiscovery instance = new EZMQXTopicDiscovery();
        assertNotNull(instance);
        thrown.expect(EZMQXException.class);
        instance.query("/topic");
    }

    @Test
    public void hierarchicalQueryTest() throws EZMQXException {
        mConfig.startStandAloneMode(true, "127.0.0.1");
        EZMQXTopicDiscovery instance = new EZMQXTopicDiscovery();
        assertNotNull(instance);
        thrown.expect(EZMQXException.class);
        instance.hierarchicalQuery("/topic");
    }
}
