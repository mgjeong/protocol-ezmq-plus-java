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
import org.protocol.ezmqx.internal.EZMQXContext;

/**
 * This class represents EZMQX configure. It provides APIs for
 * start/stop/set modes of EZMQX stack. EZMQX needs to be started
 * before using any EZMQX feature.
 */
public class EZMQXConfig {
    private static EZMQXConfig mInstance;
    private EZMQXContext mContext;
    private AtomicBoolean mInitialized;

    private final static EdgeXLogger logger = EdgeXLoggerFactory.getEdgeXLogger(EZMQXConfig.class);

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

    private EZMQXConfig() throws EZMQXException {
        mInitialized = new AtomicBoolean(false);
        mContext = EZMQXContext.getInstance();
    }

    /**
     * Get instance of EZMQXConfig.
     *
     * @return EZMQXConfig instance.
     */
    public static synchronized EZMQXConfig getInstance() throws EZMQXException {
        if (null == mInstance) {
            mInstance = new EZMQXConfig();
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
     */
    public synchronized void startDockerMode() throws EZMQXException {
        if (mInitialized.get()) {
            throw new EZMQXException("Already started", EZMQXErrorCode.Initialized);
        }
        mContext.initializeDockerMode();
        mInitialized.set(true);
        logger.debug("Started docker mode");
    }

    /**
     * Start/Configure EZMQX in stand-alone mode.
     *
     * @param useTns Whether to use TNS [Topic name server] or not.
     * @param tnsAddr TNS address [IP:Port], if useTns is false this value
     *        will be ignored.
     */
    public synchronized void startStandAloneMode(boolean useTns, String tnsAddr) throws EZMQXException {
        if (mInitialized.get()) {
            throw new EZMQXException("Already started", EZMQXErrorCode.Initialized);
        }
        mContext.initializeStandAloneMode(useTns, tnsAddr);
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
