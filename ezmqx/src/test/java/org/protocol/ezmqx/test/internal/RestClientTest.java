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

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.protocol.ezmqx.internal.RestClient;
import org.protocol.ezmqx.internal.RestUtils;

public class RestClientTest {

  @Test
  public void constructorTest() {
    RestClient instance = new RestClient(5);
    assertNotNull(instance);
  }

  @Test(expected = Exception.class)
  public void getTest() throws Exception {
    RestClient instance = new RestClient(5);
    assertNotNull(instance);
    instance.get(RestUtils.NODE + RestUtils.PREFIX + RestUtils.API_CONFIG);
  }

  @Test(expected = Exception.class)
  public void putTest() throws Exception {
    RestClient instance = new RestClient(5);
    assertNotNull(instance);
    instance.put(RestUtils.NODE + RestUtils.PREFIX + RestUtils.API_CONFIG, "testData");
  }

  @Test(expected = Exception.class)
  public void postTest() throws Exception {
    RestClient instance = new RestClient(5);
    assertNotNull(instance);
    instance.post(RestUtils.NODE + RestUtils.PREFIX + RestUtils.API_CONFIG, "testData");
  }

  @Test(expected = Exception.class)
  public void deleteTest() throws Exception {
    RestClient instance = new RestClient(5);
    assertNotNull(instance);
    instance.delete(RestUtils.NODE + RestUtils.PREFIX + RestUtils.API_CONFIG, "testQuery");
  }

  @Test
  public void restUtilsTest() {
    RestUtils instance = new RestUtils();
    assertNotNull(instance);
  }
}
