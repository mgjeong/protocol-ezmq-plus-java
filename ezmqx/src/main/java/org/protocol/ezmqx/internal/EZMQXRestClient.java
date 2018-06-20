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

public class EZMQXRestClient {

    private ResteasyClient mRestClient;

    public EZMQXRestClient(int timeOut) {
        mRestClient =
                new ResteasyClientBuilder().establishConnectionTimeout(timeOut, TimeUnit.SECONDS)
                        .connectionPoolSize(6).build();
    }

    public Response get(String url) throws Exception {
        ResteasyWebTarget target = (ResteasyWebTarget) mRestClient.target(url);
        return target.request().get();
    }

    public Response get(String url, String query) throws Exception {
        ResteasyWebTarget target =
                (ResteasyWebTarget) mRestClient.target(url + EZMQXRestUtils.QUESTION_MARK + query);
        return target.request().get();
    }

    public Response put(String url, String payload) throws Exception {
        ResteasyWebTarget target = (ResteasyWebTarget) mRestClient.target(url);
        return target.request().put(Entity.json(payload));
    }

    public Response post(String url, String payload) throws Exception {
        ResteasyWebTarget target = (ResteasyWebTarget) mRestClient.target(url);
        return target.request().post(Entity.entity(payload, MediaType.APPLICATION_JSON));
    }

    public Response delete(String url, String query) throws Exception {
        ResteasyWebTarget target =
                (ResteasyWebTarget) mRestClient.target(url + EZMQXRestUtils.QUESTION_MARK + query);
        return target.request().delete();
    }
}
