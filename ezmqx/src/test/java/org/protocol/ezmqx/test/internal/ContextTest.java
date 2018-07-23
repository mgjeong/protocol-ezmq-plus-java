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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.internal.Context;

public class ContextTest {
  private Context mContext;

  @Before
  public void setup() throws EZMQXException {
    mContext = Context.getInstance();
    assertNotNull(mContext);
  }

  @After
  public void after() {
    mContext.terminate();
  }

  @Test
  public void isInitializedTest() {
    assertEquals(false, mContext.isInitialized());
  }

  @Test
  public void isTerminatedTest() {
    assertEquals(true, mContext.isTerminated());
  }

  @Test
  public void isStandAloneTest() {
    assertEquals(false, mContext.isStandAlone());
  }

  @Test
  public void isTnsEnabledTest() {
    assertEquals(false, mContext.isTnsEnabled());
  }

  @Test
  public void assignDynamicPortTest() throws EZMQXException {
    assertEquals(4000, mContext.assignDynamicPort());
  }

  @Test
  public void assignReleasePortTest() throws EZMQXException {
    assertEquals(4000, mContext.assignDynamicPort());
    mContext.releaseDynamicPort(4000);
  }

  @Test(expected = EZMQXException.class)
  public void initializeDockerModeTest() throws EZMQXException {
    mContext.initializeDockerMode();
  }
}
