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

import org.protocol.ezmqx.EZMQXException;

public class Utils {
  public static final String LOCAL_HOST = "localhost";
  public static final int LOCAL_PORT_START = 4000;
  public static final int LOCAL_PORT_MAX = 100;
  private final static String SLASH = "/";
  private final static String DOUBLE_SLASH = "//";
  private final static String TOPIC_PATTERN = "(/)[a-zA-Z0-9-_./]+";
  // private final static String TOPIC_WILD_CARD = "*";
  // private final static String TOPIC_WILD_PATTERN = "/*/";

  public static boolean validateTopic(String topic) throws EZMQXException {
    if (null == topic || topic.isEmpty()) {
      return false;
    }
    if (topic.contains(DOUBLE_SLASH) || topic.endsWith(SLASH)) {
      return false;
    }
    if (!(topic.matches(TOPIC_PATTERN))) {
      return false;
    }
    return true;
  }
}
