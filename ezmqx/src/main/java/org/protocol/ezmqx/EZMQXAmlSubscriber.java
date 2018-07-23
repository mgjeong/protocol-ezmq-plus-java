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
import org.edgexfoundry.ezmq.EZMQMessage;
import org.edgexfoundry.ezmq.bytedata.EZMQByteData;

/**
 * This class represents EZMQX AML subscriber. It provides APIs for
 * creating subscriber to subscribe for given topic.
 */
public class EZMQXAmlSubscriber extends EZMQXSubscriber {

  private EZMQXAmlSubCallback mSubCallback;

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
        }
        EZMQByteData byteData = (EZMQByteData) ezmqMessage;
        try {
          amlObject = representation.ByteToData(byteData.getByteData());
          mSubCallback.onMessage(topic, amlObject);
        } catch (AMLException e) {
          mSubCallback.onError(topic, EZMQXErrorCode.BrokenPayload);
        }
      }
    }
  };

  /**
   * Interface to receive data/error callback from EZMQX AML subscriber.
   */
  public interface EZMQXAmlSubCallback {

    /**
     * Invoked when data is received for a specific topic.
     *
     * @param topic Topic for the received data.
     * @param data Received data [AMLObject].
     */
    public void onMessage(String topic, AMLObject data);

    /**
     * Invoked when error occurred for a specific topic.
     *
     * @param topic Topic for the error occurred.
     * @param errorCode {@link EZMQXErrorCode}
     */
    public void onError(String topic, EZMQXErrorCode errorCode);
  }

  protected EZMQXAmlSubscriber(List<EZMQXTopic> topics, EZMQXAmlSubCallback subCallback)
      throws EZMQXException {
    super();
    mSubCallback = subCallback;
    setSubCallback(mInternalCallback);
  }

  protected EZMQXAmlSubscriber(String topic, boolean isHierarchical,
      EZMQXAmlSubCallback subCallback) throws EZMQXException {
    super();
    mSubCallback = subCallback;
    setSubCallback(mInternalCallback);
  }

    /**
     * Get AML subscriber instance. Note: This API will work only when
     * EZMQX is configured/started in docker mode. It will internally
     * query to TNS server with given topic and Hierarchical option.
     *
     * @param topic Topic for which subscriber will subscribe.
     * @param isHierarchical Query TNS with hierarchical option.
     * @param subCallback {@link EZMQXAmlSubCallback}
     *
     * @return EZMQ AML subscriber instance.
     */
  public static EZMQXAmlSubscriber getSubscriber(String topic, boolean isHierarchical,
      EZMQXAmlSubCallback subCallback) throws EZMQXException {
    EZMQXAmlSubscriber subscriber = new EZMQXAmlSubscriber(topic, isHierarchical, subCallback);
    subscriber.initialize(topic, isHierarchical);
    return subscriber;
  }

    /**
     * Get AML subscriber instance. Note: This API will work only when
     * EZMQX is configured/started in stand alone mode.
     *
     * @param topic Topic for which subscriber will subscribe. [
     *        {@link EZMQXTopic} ]
     * @param subCallback {@link EZMQXAmlSubCallback}
     *
     * @return EZMQ AML subscriber instance.
     */
  public static EZMQXAmlSubscriber getSubscriber(EZMQXTopic topic, EZMQXAmlSubCallback subCallback)
      throws EZMQXException {
    List<EZMQXTopic> topics = new ArrayList<EZMQXTopic>();
    topics.add(topic);
    EZMQXAmlSubscriber subscriber = new EZMQXAmlSubscriber(topics, subCallback);
    subscriber.initialize(topics);
    return subscriber;
  }

    /**
     * Get AML subscriber instance. Note: This API will work only when
     * EZMQX is configured/started in stand alone mode.
     *
     * @param topics List of topics for which subscriber will subscribe.[
     *        {@link EZMQXTopic} ]
     * @param subCallback {@link EZMQXAmlSubCallback}
     *
     * @return EZMQ AML subscriber instance.
     */
  public static EZMQXAmlSubscriber getSubscriber(List<EZMQXTopic> topics,
      EZMQXAmlSubCallback subCallback) throws EZMQXException {
    EZMQXAmlSubscriber subscriber = new EZMQXAmlSubscriber(topics, subCallback);
    subscriber.initialize(topics);
    return subscriber;
  }
}
