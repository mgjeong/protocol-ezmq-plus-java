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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.datamodel.aml.AMLException;
import org.datamodel.aml.Representation;
import org.edgexfoundry.ezmq.EZMQAPI;
import org.edgexfoundry.ezmq.EZMQErrorCode;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;
import org.protocol.ezmqx.EZMQXEndPoint;
import org.protocol.ezmqx.EZMQXErrorCode;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.internal.RestUtils;
import org.protocol.ezmqx.internal.TopicHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Context {
  private static Context mInstance;
  private String mHostname;
  private String mHostAddr;
  private boolean mStandAlone;
  private boolean mTnsEnabled;
  private AtomicBoolean mInitialized;
  private AtomicBoolean mTerminated;

  private String mRemoteAddr;
  private Map<String, Representation> mAMLRepDic;
  private int mNumOfPort;
  private int mUsedIdx;
  private Map<Integer, Boolean> mUsedPorts;
  private Map<Integer, Integer> mPorts;

  private final static EdgeXLogger logger = EdgeXLoggerFactory.getEdgeXLogger(Context.class);

  private Context() {
    mAMLRepDic = new ConcurrentHashMap<String, Representation>();
    mUsedPorts = new HashMap<Integer, Boolean>();
    mPorts = new HashMap<Integer, Integer>();
    mInitialized = new AtomicBoolean(false);
    mTerminated = new AtomicBoolean(false);
    mUsedIdx = 0;
    mNumOfPort = 0;
    mStandAlone = false;
    mTnsEnabled = false;
  }

  public static synchronized Context getInstance() {
    if (null == mInstance) {
      mInstance = new Context();
    }
    return mInstance;
  }

  // finalize method to be called by Java Garbage collector before destroying
  // this object.
  @Override
  protected void finalize() {
    terminate();
  }

  public boolean isInitialized() {
    return mInitialized.get();
  }

  public boolean isTerminated() {
    return mTerminated.get();
  }

  public boolean isStandAlone() {
    return mStandAlone;
  }

  public boolean isTnsEnabled() {
    return mTnsEnabled;
  }

  public String getTnsAddr() {
    return mRemoteAddr;
  }

  public int assignDynamicPort() throws EZMQXException {
    int port = 0;
    while (true) {
      if (mNumOfPort >= Utils.LOCAL_PORT_MAX) {
        throw new EZMQXException("Could not assign port", EZMQXErrorCode.MaximumPortExceed);
      }
      int key = Utils.LOCAL_PORT_START + mUsedIdx;
      if (mUsedPorts.containsKey(key)
          && true == mUsedPorts.get(Utils.LOCAL_PORT_START + mUsedIdx)) {
        mUsedIdx++;
        if (mUsedIdx > Utils.LOCAL_PORT_MAX) {
          mUsedIdx = 0;
        }
      } else {
        mUsedPorts.put(key, true);
        port = key;
        mNumOfPort++;
        break;
      }
    }
    logger.debug("assigned dynamic Port: " + port);
    return port;
  }

  public void releaseDynamicPort(int port) throws EZMQXException {
    if (mUsedPorts.containsKey(port) && mUsedPorts.get(port)) {
      mUsedPorts.put(port, false);
      mNumOfPort--;
    } else {
      throw new EZMQXException("Could not release port", EZMQXErrorCode.ReleaseWrongPort);
    }
  }

  public EZMQXEndPoint getHostEp(int port) throws EZMQXException {
    int hostPort = 0;
    if (mStandAlone) {
      hostPort = port;
    } else {
      if (mPorts.containsKey(port)) {
        hostPort = mPorts.get(port);
      }
      if (0 == hostPort) {
        throw new EZMQXException("Invalid Port", EZMQXErrorCode.UnKnownState);
      }
    }
    EZMQXEndPoint endPoint = new EZMQXEndPoint(mHostAddr, hostPort);
    return endPoint;
  }

  public List<String> addAmlRep(List<String> amlFilePath) throws EZMQXException {
    List<String> modelId = new ArrayList<String>();
    String amlModelId;

    if (null == amlFilePath || amlFilePath.isEmpty()) {
      return modelId;
    }
    for (String filePath : amlFilePath) {
      Representation representation = null;
      try {
        logger.debug("AMLFile Path: " + filePath);
        representation = new Representation(filePath);
      } catch (AMLException exception) {
        throw new EZMQXException("Could not parse aml model file", EZMQXErrorCode.InvalidAmlModel);
      }
      try {
        amlModelId = representation.getRepresentationId();
      } catch (AMLException exception) {
        throw new EZMQXException("Invalid aml model id", EZMQXErrorCode.InvalidParam);
      }

      if (!(mAMLRepDic.containsKey(amlModelId))) {
        mAMLRepDic.put(amlModelId, representation);
      }
      modelId.add(amlModelId);
    }
    return modelId;
  }

  public Representation getAmlRep(String amlModelId) throws EZMQXException {
    if (!(mAMLRepDic.containsKey(amlModelId))) {
      throw new EZMQXException("AML rep dict does not contain: " + amlModelId,
          EZMQXErrorCode.InvalidAmlModel);
    }
    Representation rep = mAMLRepDic.get(amlModelId);
    if (null == rep) {
      throw new EZMQXException("Could not find matching Aml Rep", EZMQXErrorCode.UnknownAmlModel);
    }
    return rep;
  }

  public void initializeStandAloneMode(boolean useTns, String tnsAddr) throws EZMQXException {
    if (EZMQErrorCode.EZMQ_OK != EZMQAPI.getInstance().initialize()) {
      throw new EZMQXException("Could not start ezmq context", EZMQXErrorCode.UnKnownState);
    }
    mStandAlone = true;
    setHostInfo(Utils.LOCAL_HOST, Utils.LOCAL_HOST);
    if (useTns) {
      setTnsInfo(tnsAddr);
    }
    mInitialized.set(true);
    mTerminated.set(false);
    logger.debug("EZMQX Context created");
  }

  public void setHostInfo(String hostName, String hostAddr) {
    mHostname = hostName;
    mHostAddr = hostAddr;
  }

  public void setTnsInfo(String remoteAddr) {
    mTnsEnabled = true;
    mRemoteAddr = remoteAddr;
  }

  private void parseConfigResponse(RestResponse response) {
    logger.debug("[Config] Status code: " + response.getStatusCode());
    String jsonString = response.getResponse();
    logger.debug("[Config] Response: " + jsonString);

    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = null;
    try {
      root = mapper.readTree(jsonString);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    JsonNode propertiesNode = root.path(RestUtils.CONF_PROPS);
    for (JsonNode node : propertiesNode) {
      if (node.has(RestUtils.CONF_REMOTE_ADDR)) {
        String anchoraddress = node.path(RestUtils.CONF_REMOTE_ADDR).asText();
        logger.debug("[Config] anchoraddress: " + anchoraddress);
        setTnsInfo(anchoraddress);
      }
      if (node.has(RestUtils.CONF_NODE_ADDR)) {
        String nodeaddress = node.path(RestUtils.CONF_NODE_ADDR).asText();
        logger.debug("[Config] nodeaddress: " + nodeaddress);
        mHostAddr = nodeaddress;
      }
    }
  }

  private void readFromFile(String filePath) {
    logger.debug("File Path: " + filePath);
    BufferedReader bufferedReader = null;
    FileReader fileReader = null;
    try {
      fileReader = new FileReader(filePath);
      bufferedReader = new BufferedReader(fileReader);
      mHostname = bufferedReader.readLine();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (bufferedReader != null)
          bufferedReader.close();
        if (fileReader != null)
          fileReader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private List<String> parseAppsResponse(RestResponse response) {
    logger.debug("[Running apps] Status code: " + response.getStatusCode());
    String jsonString = response.getResponse();
    logger.debug("[Running apps] Response: " + jsonString);

    List<String> runningApps = new ArrayList<String>();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = null;
    try {
      root = mapper.readTree(jsonString);
    } catch (IOException e) {
      e.printStackTrace();
      return runningApps;
    }
    JsonNode appsNode = root.path(RestUtils.APPS_PROPS);
    String id = null;
    String state = null;
    for (JsonNode node : appsNode) {
      if (node.has(RestUtils.APPS_ID)) {
        id = node.path(RestUtils.APPS_ID).asText();
        logger.debug("[Running Apps] id: " + id);
      }
      if (node.has(RestUtils.APPS_STATE)) {
        state = node.path(RestUtils.APPS_STATE).asText();
        logger.debug("[Running Apps] state: " + state);
        if (null != id && null != state && state.equalsIgnoreCase(RestUtils.APPS_STATE_RUNNING)) {
          runningApps.add(id);
        }
      }
    }
    return runningApps;
  }

  private void parsePortInfo(JsonNode node) {
    JsonNode ports;
    if (node.has(RestUtils.SERVICES_CON_PORTS)) {
      ports = node.path(RestUtils.SERVICES_CON_PORTS);
      for (JsonNode port : ports) {
        if (port.has(RestUtils.PORTS_PRIVATE) && port.has(RestUtils.PORTS_PUBLIC)) {
          int privatePort = port.path(RestUtils.PORTS_PRIVATE).asInt();
          int publicPort = port.path(RestUtils.PORTS_PUBLIC).asInt();
          if (privatePort > -1 && publicPort > -1) {
            logger.debug("Putting private port: " + privatePort + " Public Port: " + publicPort);
            mPorts.put(privatePort, publicPort);
          }
        }
      }
    }
  }

  private void parseAppInfoResponse(RestResponse response) {
    logger.debug("[App info] Status code: " + response.getStatusCode());
    String jsonString = response.getResponse();
    logger.debug("[app info] Response: " + jsonString);

    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = null;
    try {
      root = mapper.readTree(jsonString);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    JsonNode services = root.path(RestUtils.SERVICES_PROPS);
    String cid;
    for (JsonNode node : services) {
      if (node.has(RestUtils.SERVICES_CON_ID)) {
        cid = node.path(RestUtils.SERVICES_CON_ID).asText();
        cid = cid.substring(0, mHostname.length());
        logger.debug("[app info] cid: " + cid + " host Name: " + mHostname);
        System.out.println("###### [app info] cid: " + cid + " host Name: " + mHostname);
        if (cid.equals(mHostname)) {
          parsePortInfo(node);
        }
      }
    }
  }

  public void initializeDockerMode() throws EZMQXException {
    if (EZMQErrorCode.EZMQ_OK != EZMQAPI.getInstance().initialize()) {
      throw new EZMQXException("Could not start ezmq context", EZMQXErrorCode.UnKnownState);
    }

    RestFactory restClient = RestFactory.getInstance();
    try {
      // Configuration resource
      String configURL = RestUtils.NODE + RestUtils.PREFIX + RestUtils.API_CONFIG;
      logger.debug("[Config] Rest URL: " + configURL);
      RestResponse configResponse = restClient.get(configURL);
      parseConfigResponse(configResponse);

      // Get Host Name
      readFromFile(RestUtils.HOSTNAME);

      // Applications resource
      String appsURL = RestUtils.NODE + RestUtils.PREFIX + RestUtils.API_APPS;
      logger.debug("[Running Apps] Rest URL: " + appsURL);
      RestResponse appResponse = restClient.get(appsURL);
      List<String> runningApps = parseAppsResponse(appResponse);

      // Application Info details
      String appInfoURL = RestUtils.NODE + RestUtils.PREFIX + RestUtils.API_APPS + RestUtils.SLASH;
      RestResponse appInfoResponse = null;
      for (String appId : runningApps) {
        appInfoURL = appInfoURL + appId;
        logger.debug("[App Info] Rest URL: " + appInfoURL);
        appInfoResponse = restClient.get(appInfoURL);
        parseAppInfoResponse(appInfoResponse);
      }
    } catch (Exception e) {
      logger.debug("Caught exception: " + e.getMessage());
      throw new EZMQXException("Rest client error: " + e.getMessage(), EZMQXErrorCode.UnKnownState);
    }
    mInitialized.set(true);
    mTerminated.set(false);
    logger.debug("EZMQX Context created");
  }

  public void terminate() {
    if (mTerminated.get()) {
      logger.debug("Context already terminated");
      return;
    }

    logger.debug("EZMQX terminating the context");
    // terminate topic handler [Topic handler]
    try {
      TopicHandler.getInstance().terminateHandler();
    } catch (EZMQXException e) {
      logger.debug("Caught exception: " + e.getMessage());
    }

    mPorts.clear();
    mUsedPorts.clear();
    mAMLRepDic.clear();
    mHostname = "";
    mHostAddr = "";
    mRemoteAddr = "";
    mUsedIdx = 0;
    mNumOfPort = 0;
    mStandAlone = false;
    mTnsEnabled = false;
    logger.debug("EZMQAPI try to terminate");
    // Release EZMQAPI
    if (EZMQErrorCode.EZMQ_OK != EZMQAPI.getInstance().terminate()) {
      logger.debug("ezmq API terminate failed");
    }
    mTerminated.set(true);
    mInitialized.set(false);
    logger.debug("EZMQX Context terminated");
  }
}
