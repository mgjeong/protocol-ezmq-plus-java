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

import java.util.ArrayList;
import java.util.List;
import org.datamodel.aml.AMLException;
import org.datamodel.aml.AMLObject;
import org.datamodel.aml.Representation;
import org.edgexfoundry.ezmq.EZMQErrorCode;
import org.edgexfoundry.ezmq.bytedata.EZMQByteData;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;
import org.protocol.ezmqx.internal.Utils;

/**
 * This class represents EZMQX AML publisher. It provides APIs for
 * creating publisher and publishing data.
 */
public class EZMQXAmlPublisher extends EZMQXPublisher {
  private Representation mRepresentation;
  private final static EdgeXLogger logger =
      EdgeXLoggerFactory.getEdgeXLogger(EZMQXAmlPublisher.class);

  private EZMQXAmlPublisher() {
    super();
  }

  // finalize method to be called by Java Garbage collector
  // before destroying
  // this object.
  @Override
  protected void finalize() {

  }

  /**
   * Get EZMQX publisher instance.
   *
   * @param topic Topic on which publisher will publish.
   * @param modelInfo Enum value for AML model info
   *        {@link EZMQXAmlModelInfo}.
   * @param modelId AML model ID or AML file path.
   * @param optionalPort Port to be used for publishing data. It will be
   *        used only when EZMQX configured in stand-alone mode.
   *
   * @return AML Publisher {@link EZMQXAmlPublisher}
   */
  public static EZMQXAmlPublisher getPublisher(String topic, EZMQXAmlModelInfo modelInfo,
      String modelId, int optionalPort) throws EZMQXException {
    EZMQXAmlPublisher publisher = new EZMQXAmlPublisher();
    publisher.initialize(optionalPort, Utils.EMPTY_STRING);
    publisher.registerTopic(topic, modelInfo, modelId, optionalPort);
    return publisher;
  }

  /**
   * Get Secured EZMQX publisher instance.<br>
   * <b>Note:</b> <br>
   * (1) serverPrivateKey should be 40-character string encoded in the Z85 encoding format <br>
   *
   * @param topic Topic on which publisher will publish.
   * @param serverPrivateKey Server private/Secret key.
   * @param modelInfo Enum value for AML model info
   *        {@link EZMQXAmlModelInfo}.
   * @param modelId AML model ID or AML file path.
   * @param optionalPort Port to be used for publishing data. It will be
   *        used only when EZMQX configured in stand-alone mode.
   *
   * @return Secured AML Publisher {@link EZMQXAmlPublisher}
   */
  public static EZMQXAmlPublisher getSecuredPublisher(String topic, String serverPrivateKey,
      EZMQXAmlModelInfo modelInfo, String modelId, int optionalPort) throws EZMQXException {
    EZMQXAmlPublisher publisher = new EZMQXAmlPublisher();
    publisher.initialize(optionalPort, serverPrivateKey);
    publisher.registerTopic(topic, modelInfo, modelId, optionalPort);
    return publisher;
  }

  private void registerTopic(String topic, EZMQXAmlModelInfo modelInfo, String modelId,
      int optionalPort) throws EZMQXException {
    boolean result = Utils.validateTopic(topic);
    if (false == result) {
      mPublisher.stop();
      throw new EZMQXException("Invalid topic", EZMQXErrorCode.InvalidTopic);
    }
    if (EZMQXAmlModelInfo.AML_MODEL_ID == modelInfo) {
      mRepresentation = mContext.getAmlRep(modelId);
    } else if (EZMQXAmlModelInfo.AML_FILE_PATH == modelInfo) {
      List<String> amlFilePath = new ArrayList<String>();
      amlFilePath.add(modelId);
      amlFilePath = mContext.addAmlRep(amlFilePath);
      mRepresentation = mContext.getAmlRep(amlFilePath.get(0));
    } else {
      mPublisher.stop();
      throw new EZMQXException("Invalid aml model info", EZMQXErrorCode.UnKnownState);
    }
    EZMQXTopic ezmqTopic = null;
    try {
      ezmqTopic = new EZMQXTopic(topic, mRepresentation.getRepresentationId(), mSecured,
          mContext.getHostEp(mLocalPort));
    } catch (AMLException e) {
      mPublisher.stop();
      throw new EZMQXException("Invalid aml model id", EZMQXErrorCode.UnKnownState);
    } catch (EZMQXException e) {
      mPublisher.stop();
      throw new EZMQXException(e.getMsg(), e.getCode());
    }
    registerTopic(ezmqTopic);
  }

  /**
   * Publish AMLObject on the socket for subscribers.
   *
   * @param payload Data to be published.
   */
  public void publish(AMLObject payload) throws EZMQXException {
    if (mContext.isTerminated()) {
      terminate();
      throw new EZMQXException("Publisher terminated", EZMQXErrorCode.Terminated);
    }

    byte[] byteAML;
    try {
      byteAML = mRepresentation.DataToByte(payload);
    } catch (AMLException e) {
      throw new EZMQXException("Invalid data", EZMQXErrorCode.UnKnownState);
    }
    if (null == mPublisher) {
      throw new EZMQXException("Publisher is null", EZMQXErrorCode.UnKnownState);
    }
    EZMQByteData data = new EZMQByteData(byteAML);
    EZMQErrorCode result = mPublisher.publish(mTopic.getName(), data);
    logger.debug("Publish result: " + result);
  }
}
