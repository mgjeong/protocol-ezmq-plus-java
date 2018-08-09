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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.edgexfoundry.ezmq.EZMQAPI;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;
import org.protocol.ezmqx.internal.Context;
import org.protocol.ezmqx.internal.RestClientFactory;
import org.protocol.ezmqx.internal.RestClientFactoryInterface;
import org.protocol.ezmqx.internal.RestFactory;

/**
 * This class represents EZMQX configure. It provides APIs for
 * start/stop/set modes of EZMQX stack. EZMQX needs to be started
 * before using any EZMQX feature.
 */
public class EZMQXConfig {
  private static EZMQXConfig mInstance;
  private Context mContext;
  private AtomicBoolean mInitialized;

  // setting log level as per application.properties
  static {
    InputStream stream = null;
    try {
      Properties props = new Properties();
      stream = EZMQAPI.class.getResourceAsStream("/application.properties");
      props.load(stream);
      String mode = props.getProperty("ezmqx.logging.level");
      if ((null != mode) && (mode.equalsIgnoreCase("DEBUG"))) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (null != stream) {
        try {
          stream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private final static EdgeXLogger logger = EdgeXLoggerFactory.getEdgeXLogger(EZMQXConfig.class);

  private EZMQXConfig() {
    mInitialized = new AtomicBoolean(false);
    mContext = Context.getInstance();
  }

  /**
   * Get instance of EZMQXConfig.
   *
   * @return EZMQXConfig instance.
   */
  public static synchronized EZMQXConfig getInstance() {
    if (null == mInstance) {
      mInstance = new EZMQXConfig();
      RestClientFactoryInterface factory = new RestClientFactory();
      RestFactory.getInstance().setFactory(factory);
    }
    return mInstance;
  }

  // finalize method to be called by Java Garbage collector
  // before destroying
  // this object.
  @Override
  protected void finalize() {
    mContext.terminate();
  }

  /**
   * Start/Configure EZMQX in docker mode.
   * 
   * @param tnsConfPath Path to TNS configuration file.
   */
  public synchronized void startDockerMode(String tnsConfPath) throws EZMQXException {
    if (mInitialized.get()) {
      throw new EZMQXException("Already started", EZMQXErrorCode.Initialized);
    }
    mContext.initializeDockerMode(tnsConfPath);
    mInitialized.set(true);
    logger.debug("Started docker mode");
  }

  /**
   * Start/Configure EZMQX in stand-alone mode.
   *
   * @param hostAddr IP address of host machine.
   * @param useTns Whether to use TNS [Topic name server] or not.
   * @param tnsAddr TNS address [Complete Rest address], if useTns is false this value
   *        will be ignored.Examples:<br>
   *        With Reverse-proxy: http://192.168.0.1:80/tns-server<br>
   *        Without Reverse-proxy: http://192.168.0.1:48323 
   *             
   */
  public synchronized void startStandAloneMode(String hostAddr, boolean useTns, String tnsAddr)
      throws EZMQXException {
    if (mInitialized.get()) {
      throw new EZMQXException("Already started", EZMQXErrorCode.Initialized);
    }
    mContext.initializeStandAloneMode(hostAddr, useTns, tnsAddr);
    mInitialized.set(true);
    logger.debug("Started Standalone mode");
  }

  /**
   * Add aml model file for publish or subscribe AML data.
   *
   * @param amlFilePath List of AML files.
   *
   * @return List of AML Ids corresponding to given AML files.
   */
  public List<String> addAmlModel(List<String> amlFilePath) throws EZMQXException {
    if (!mInitialized.get()) {
      throw new EZMQXException("Not initialized", EZMQXErrorCode.NotInitialized);
    }
    return mContext.addAmlRep(amlFilePath);
  }

  /**
   * Reset/Terminate EZMQX stack.
   */
  public synchronized void reset() throws EZMQXException {
    if (!mInitialized.get()) {
      throw new EZMQXException("Not initialized", EZMQXErrorCode.NotInitialized);
    }
    mContext.terminate();
    mInitialized.set(false);
    logger.debug("EZMQX reset done");
  }
}
