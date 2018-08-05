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
 * This class represents EZMQX exception.
 */
@SuppressWarnings("serial")
public class EZMQXException extends Exception {
  private String mMsg;
  private EZMQXErrorCode mCode;

  /**
   * Constructor for EZMQX exception.
   *
   * @param msg String message to be set in exception.
   * @param code Error code to be set in exception.
   *        {@link EZMQXErrorCode}
   */
  public EZMQXException(String msg, EZMQXErrorCode code) {
    mMsg = msg;
    mCode = code;
  }

  /**
   * Get exception message.
   *
   * @return String message.
   */
  public String getMsg() {
    return mMsg;
  }

  /**
   * Get error code.
   *
   * @return EZMQXErrorCode
   */
  public EZMQXErrorCode getCode() {
    return mCode;
  }
}
