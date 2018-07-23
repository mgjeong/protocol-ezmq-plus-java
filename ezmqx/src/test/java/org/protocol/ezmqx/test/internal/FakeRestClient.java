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

import java.util.HashMap;
import java.util.Map;
import org.protocol.ezmqx.internal.RestClientInterface;
import org.protocol.ezmqx.internal.RestResponse;

public class FakeRestClient implements RestClientInterface {
  private static Map<String, String> mRestResponse = new HashMap<String, String>();

  public static String getResponse(String url) {
    return mRestResponse.get(url);
  }

  public static void setResponse(String url, String payload) {
    mRestResponse.put(url, payload);
  }

  @Override
  public RestResponse get(String url) throws Exception {
    RestResponse restResponse = new RestResponse(200, mRestResponse.get(url));
    return restResponse;
  }

  @Override
  public RestResponse get(String url, String query) throws Exception {
    RestResponse restResponse = new RestResponse(200, mRestResponse.get(url));
    return restResponse;
  }

  @Override
  public RestResponse put(String url, String payload) throws Exception {
    RestResponse restResponse = new RestResponse(200, mRestResponse.get(url));
    return restResponse;
  }

  @Override
  public RestResponse post(String url, String payload) throws Exception {
    RestResponse restResponse = new RestResponse(200, mRestResponse.get(url));
    return restResponse;
  }

  @Override
  public RestResponse delete(String url, String query) throws Exception {
    RestResponse restResponse = new RestResponse(200, mRestResponse.get(url));
    return restResponse;
  }
}
