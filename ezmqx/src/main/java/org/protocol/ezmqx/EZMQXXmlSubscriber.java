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
import java.util.Map;
import org.datamodel.aml.AMLException;
import org.datamodel.aml.AMLObject;
import org.datamodel.aml.Representation;
import org.edgexfoundry.ezmq.EZMQMessage;
import org.edgexfoundry.ezmq.bytedata.EZMQByteData;

/**
 * This class represents EZMQX XML subscriber. It provides APIs for
 * creating subscriber to subscribe for given topic.
 */
public class EZMQXXmlSubscriber extends EZMQXSubscriber {

  private EZMQXXmlSubCallback mSubCallback;

  private EZMQXSubCallback mInternalCallback = new EZMQXSubCallback() {
    public void onMessage(String topic, EZMQMessage ezmqMessage) {
      if (null == topic || topic.isEmpty() || (!(mAMLRepDic.containsKey(topic)))) {
        mSubCallback.onError(topic, EZMQXErrorCode.UnknownTopic);
        return;
      } else {
        AMLObject amlObject = null;
        Representation representation = mAMLRepDic.get(topic);
        if (null == representation) {
          mSubCallback.onError(topic, EZMQXErrorCode.UnKnownState);
          return;
        }
        EZMQByteData byteData = (EZMQByteData) ezmqMessage;
        try {
          amlObject = representation.ByteToData(byteData.getByteData());
          String amlString = representation.DataToAml(amlObject);
          mSubCallback.onMessage(topic, amlString);
        } catch (AMLException e) {
          mSubCallback.onError(topic, EZMQXErrorCode.BrokenPayload);
        }
      }
    }
  };

  /**
   * Interface to receive data/error callback from EZMQX XML subscriber.
   */
  public interface EZMQXXmlSubCallback {

    /**
     * Invoked when data is received for a specific topic.
     *
     * @param topic Topic for the received data.
     * @param data Received data.
     */
    public void onMessage(String topic, String data);

    /**
     * Invoked when error occurred for a specific topic.
     *
     * @param topic Topic for the error occurred.
     * @param errorCode {@link EZMQXErrorCode}
     */
    public void onError(String topic, EZMQXErrorCode errorCode);
  }

  protected EZMQXXmlSubscriber(EZMQXXmlSubCallback subCallback) throws EZMQXException {
    super();
    setSubCallback(mInternalCallback);
    mSubCallback = subCallback;
  }

  /**
   * Get XML subscriber instance.
   * Note: <br>
   * (1) It will internally query to TNS server with given topic and Hierarchical option.
   * 
   * @param topic Topic for which subscriber will subscribe.
   * @param isHierarchical Query TNS with hierarchical option.
   * @param subCallback {@link EZMQXXmlSubCallback}
   *
   * @return EZMQ XML subscriber instance.
   */
  public static EZMQXXmlSubscriber getSubscriber(String topic, boolean isHierarchical,
      EZMQXXmlSubCallback subCallback) throws EZMQXException {
    EZMQXXmlSubscriber subscriber = new EZMQXXmlSubscriber(subCallback);
    subscriber.initialize(topic, isHierarchical);
    subscriber.mSecured = false;
    return subscriber;
  }


  /**
   * Get XML subscriber instance.
   *
   * @param topic Topic for which subscriber will subscribe.[
   *        {@link EZMQXTopic} ]
   * @param subCallback {@link EZMQXXmlSubCallback}
   *
   * @return EZMQ XML subscriber instance.
   */
  public static EZMQXXmlSubscriber getSubscriber(EZMQXTopic topic, EZMQXXmlSubCallback subCallback)
      throws EZMQXException {
    if (topic.isSecured()) {
      throw new EZMQXException("topic is secured", EZMQXErrorCode.InvalidParam);
    }
    EZMQXXmlSubscriber subscriber = new EZMQXXmlSubscriber(subCallback);
    List<EZMQXTopic> topics = new ArrayList<EZMQXTopic>();
    topics.add(topic);
    subscriber.initialize(topics);
    subscriber.mSecured = false;
    return subscriber;
  }

  /**
   * Get XML subscriber instance. 
   * 
   * @param topics List of topics for which subscriber will subscribe.[
   *        {@link EZMQXTopic} ]
   * @param subCallback {@link EZMQXXmlSubCallback}
   *
   * @return EZMQ XML subscriber instance.
   */
  public static EZMQXXmlSubscriber getSubscriber(List<EZMQXTopic> topics,
      EZMQXXmlSubCallback subCallback) throws EZMQXException {
    for (EZMQXTopic topic : topics) {
      if (topic.isSecured()) {
        throw new EZMQXException("topic is secured", EZMQXErrorCode.InvalidParam);
      }
    }
    EZMQXXmlSubscriber subscriber = new EZMQXXmlSubscriber(subCallback);
    subscriber.initialize(topics);
    subscriber.mSecured = false;
    return subscriber;
  }

  /**
   * Get Secured XML subscriber instance.
   * Note: <br>
   * (1) Key should be 40-character string encoded in the Z85 encoding format.
   *
   * @param topic Topic for which subscriber will subscribe.
   * @param serverPublicKey Public key for server(publisher) that related with given topic.
   * @param clientPublicKey Public key for client(subscriber) that shared with given topic's owner. 
   * @param clientSecretKey Secret key for client(subscriber) that pair of given clientPublickey. 
   * @param isHierarchical Query TNS with hierarchical option.
   * @param subCallback {@link EZMQXXmlSubCallback}
   *
   * @return EZMQ AML subscriber instance.
   */
  public static EZMQXXmlSubscriber getSecuredSubscriber(EZMQXTopic topic, String serverPublicKey,
      String clientPublicKey, String clientSecretKey, EZMQXXmlSubCallback subCallback)
      throws EZMQXException {
    if (!topic.isSecured()) {
      throw new EZMQXException("topic is unsecured", EZMQXErrorCode.InvalidParam);
    }
    EZMQXXmlSubscriber subscriber = new EZMQXXmlSubscriber(subCallback);
    subscriber.initialize(topic, serverPublicKey, clientPublicKey, clientSecretKey);
    subscriber.mSecured = true;
    return subscriber;
  }


  /**
   * Get Secured XML subscriber instance. 
   * Note:<br> 
   * (1) Key should be 40-character string encoded in the Z85 encoding format.
   *
   * @param topicKeyMap Map of Topic and server's public keys.
   * @param clientPublicKey Public key for client(subscriber) that shared with given topic's owner. 
   * @param clientSecretKey Secret key for client(subscriber) that pair of given clientPublickey. 
   * @param subCallback {@link EZMQXXmlSubCallback}
   *
   * @return EZMQ Secured XML subscriber instance.
   */
  public static EZMQXXmlSubscriber getSecuredSubscriber(Map<EZMQXTopic, String> topicKeyMap,
      String clientPublicKey, String clientSecretKey, EZMQXXmlSubCallback subCallback)
      throws EZMQXException {
    EZMQXXmlSubscriber subscriber = new EZMQXXmlSubscriber(subCallback);
    subscriber.initialize(topicKeyMap, clientPublicKey, clientSecretKey);
    subscriber.mSecured = true;
    return subscriber;
  }
}
