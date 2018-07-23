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

package org.protocol.ezmqx.internal;

public class RestFactory {
  private static RestFactory mInstance;
  private RestClientFactoryInterface mRestInterface;
  private int mTimeOut;

  private RestFactory() {
    mRestInterface = new RestClientFactory();
    mTimeOut = RestUtils.CONNECTION_TIMEOUT;
  }

  public static RestFactory getInstance() {
    if (null == mInstance) {
      mInstance = new RestFactory();
    }
    return mInstance;
  }

  public void setFactory(RestClientFactoryInterface factory) {
    mRestInterface = factory;
  }

  public RestResponse get(String url) throws Exception {
    RestClientInterface client = mRestInterface.getRestClient(mTimeOut);
    return client.get(url);
  }

  public RestResponse get(String url, String query) throws Exception {
    RestClientInterface client = mRestInterface.getRestClient(mTimeOut);
    return client.get(url, query);
  }

  public RestResponse put(String url, String payload) throws Exception {
    RestClientInterface client = mRestInterface.getRestClient(mTimeOut);
    return client.put(url, payload);
  }

  public RestResponse post(String url, String payload) throws Exception {
    RestClientInterface client = mRestInterface.getRestClient(mTimeOut);
    return client.post(url, payload);
  }

  public RestResponse post(String url, String payload, int timeOut) throws Exception {
    RestClientInterface client = mRestInterface.getRestClient(timeOut);
    return client.post(url, payload);
  }

  public RestResponse delete(String url, String query) throws Exception {
    RestClientInterface client = mRestInterface.getRestClient(mTimeOut);
    return client.post(url, query);
  }
}
