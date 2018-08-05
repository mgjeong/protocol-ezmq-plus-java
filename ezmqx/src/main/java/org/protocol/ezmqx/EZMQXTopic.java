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

package org.protocol.ezmqx;

/**
 * This class represents EZMQX topic.
 */
public class EZMQXTopic {
  private String mName;
  private String mDataModel;
  private EZMQXEndPoint mEndPoint;

  /**
   * Constructor for EZMQX topic.
   *
   * @param name Topic name.
   * @param dataModel AML Data model ID.
   * @param endPoint EZMQX end point.
   *
   */
  public EZMQXTopic(String name, String dataModel, EZMQXEndPoint endPoint) {
    mName = name;
    mDataModel = dataModel;
    mEndPoint = endPoint;
  }

  /**
   * Get topic name.
   *
   * @return Topic name as string.
   */
  public String getName() {
    return mName;
  }

  /**
   * Get AML data model id.
   *
   * @return Data model id as string.
   */
  public String getDatamodel() {
    return mDataModel;
  }

  /**
   * Get EZMQX end point.
   *
   * @return EZMQXEndPoint
   *
   */
  public EZMQXEndPoint getEndPoint() {
    return mEndPoint;
  }
}
