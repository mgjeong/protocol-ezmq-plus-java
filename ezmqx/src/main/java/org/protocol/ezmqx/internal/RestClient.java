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

import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class RestClient implements RestClientInterface {

  private ResteasyClient mRestClient;

  public RestClient(int timeOut) {
    mRestClient = new ResteasyClientBuilder().establishConnectionTimeout(timeOut, TimeUnit.SECONDS)
        .connectionPoolSize(6).build();
  }

  public RestResponse get(String url) throws Exception {
    ResteasyWebTarget target = (ResteasyWebTarget) mRestClient.target(url);
    Response jaxResponse = target.request().get();
    RestResponse response =
        new RestResponse(jaxResponse.getStatus(), jaxResponse.readEntity(String.class));
    return response;
  }

  public RestResponse get(String url, String query) throws Exception {
    ResteasyWebTarget target =
        (ResteasyWebTarget) mRestClient.target(url + RestUtils.QUESTION_MARK + query);
    Response jaxResponse = target.request().get();
    RestResponse response =
        new RestResponse(jaxResponse.getStatus(), jaxResponse.readEntity(String.class));
    return response;
  }

  public RestResponse put(String url, String payload) throws Exception {
    ResteasyWebTarget target = (ResteasyWebTarget) mRestClient.target(url);
    Response jaxResponse = target.request().put(Entity.json(payload));
    RestResponse response =
        new RestResponse(jaxResponse.getStatus(), jaxResponse.readEntity(String.class));
    return response;
  }

  public RestResponse post(String url, String payload) throws Exception {
    ResteasyWebTarget target = (ResteasyWebTarget) mRestClient.target(url);
    Response jaxResponse =
        target.request().post(Entity.entity(payload, MediaType.APPLICATION_JSON));
    RestResponse response =
        new RestResponse(jaxResponse.getStatus(), jaxResponse.readEntity(String.class));
    return response;
  }

  public RestResponse delete(String url, String query) throws Exception {
    ResteasyWebTarget target =
        (ResteasyWebTarget) mRestClient.target(url + RestUtils.QUESTION_MARK + query);
    Response jaxResponse = target.request().delete();
    RestResponse response =
        new RestResponse(jaxResponse.getStatus(), jaxResponse.readEntity(String.class));
    return response;
  }
}
