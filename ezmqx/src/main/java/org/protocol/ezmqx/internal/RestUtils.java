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

public class RestUtils {
  // URLs
  public static final String NODE = "http://172.17.0.1:48098";
  public static final String PREFIX = "/api/v1";
  public static final String API_CONFIG = "/management/device/configuration";
  public static final String API_APPS = "/management/apps";
  public static final String API_DETAIL = "/management/detail";
  public static final String TNS_KNOWN_PORT = "48323";
  public static final String TOPIC = "/tns/topic";
  public static final String TNS_KEEP_ALIVE = "/tns/keepalive";
  public static final String HTTP_PREFIX = "http://";
  public static final String QUERY_NAME = "name=";
  public static final String QUERY_HIERARCHICAL = "&hierarchical=";
  public static final String QUERY_TRUE = "yes";
  public static final String QUERY_FALSE = "no";

  // JSON Keys
  public static final String CONF_PROPS = "properties";
  public static final String CONF_REMOTE_ADDR = "anchoraddress";
  public static final String CONF_NODE_ADDR = "nodeaddress";
  public static final String APPS_PROPS = "apps";
  public static final String APPS_ID = "id";
  public static final String APPS_STATE = "state";
  public static final String APPS_STATE_RUNNING = "running";
  public static final String APPS_EXIT_CODE = "exitcode";
  public static final String APPS_STATUS = "status";
  public static final String SERVICES_PROPS = "services";
  public static final String SERVICES_CON_NAME = "name";
  public static final String SERVICES_CON_ID = "cid";
  public static final String SERVICES_CON_PORTS = "ports";
  public static final String PORTS_PRIVATE = "PrivatePort";
  public static final String PORTS_PUBLIC = "PublicPort";
  public static final String PAYLOAD_OPTION = "indentation";
  public static final String PAYLOAD_TOPIC = "topic";
  public static final String PAYLOAD_TOPICS = "topics";
  public static final String PAYLOAD_NAME = "name";
  public static final String PAYLOAD_ENDPOINT = "endpoint";
  public static final String PAYLOAD_DATAMODEL = "datamodel";
  public static final String PAYLOAD_KEEPALIVE_INTERVAL = "ka_interval";

  // HostName file path
  public static final String HOSTNAME = "/etc/hostname";

  // HTTP status codes
  public static final int HTTP_OK = 200;
  public static final int HTTP_CREATED = 201;
  public static final int CONNECTION_TIMEOUT = 5;

  // Strings
  public static final String SLASH = "/";
  public static final String DOUBLE_SLASH = "//";
  public static final String COLON = ":";
  public static final String QUESTION_MARK = "?";
  public static final String REGISTER = "register";
  public static final String UNREGISTER = "unregister";
  public static final String KEEPALIVE = "keepalive";
  public static final String SHUTDOWN = "shutdown";
}
