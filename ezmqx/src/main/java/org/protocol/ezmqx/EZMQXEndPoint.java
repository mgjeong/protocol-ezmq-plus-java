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
 * This class represents EZMQX EndPoint.
 */
public class EZMQXEndPoint {
    private String mAddress;
    private int mPort;
    private final String COLLON = ":";

    /**
     * Constructor for EZMQX EndPoint.
     *
     * @param address Address to set in end point. <br>
     *        Example: 127.0.0.0:4545 <br>
     *        Note: If address contains only IP address port will be
     *        assigned with -1.
     */
    public EZMQXEndPoint(String address) {
        if (null == address) {
            return;
        }
        if (!address.contains(COLLON)) {
            mAddress = address;
            mPort = -1;
            return;
        }
        String[] ipPort = address.split(COLLON);
        mAddress = ipPort[0];
        mPort = Integer.parseInt(ipPort[1]);
    }

    /**
     * Constructor for EZMQX EndPoint.
     *
     * @param address address[ip] to set in end point.
     * @param port port to set in end point.
     */
    public EZMQXEndPoint(String address, int port) {
        mAddress = address;
        mPort = port;
    }

    /**
     * Get address of end point.
     *
     * @return Address as string.
     */
    public String getAddr() {
        return mAddress;
    }

    /**
     * Get port of end point.
     *
     * @return Port as integer.
     */
    public int getPort() {
        return mPort;
    }

    /**
     * Get endpoint as string.
     *
     * @return Endpoint as String.
     */
    public String toString() {
        return mPort == -1 ? mAddress : mAddress + COLLON + mPort;
    }
}
