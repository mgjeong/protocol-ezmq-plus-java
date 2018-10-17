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

package org.ezmqx.subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.protocol.ezmqx.EZMQXAmlSubscriber;
import org.protocol.ezmqx.EZMQXConfig;
import org.protocol.ezmqx.EZMQXEndPoint;
import org.protocol.ezmqx.EZMQXErrorCode;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.EZMQXTopic;
import org.protocol.ezmqx.EZMQXTopicDiscovery;
import org.protocol.ezmqx.EZMQXXmlSubscriber;
import org.protocol.ezmqx.EZMQXXmlSubscriber.EZMQXXmlSubCallback;

public class XmlSubscriber {
  public static EZMQXConfig mConfig;
  public static EZMQXXmlSubscriber mSubscriber;
  public static Lock terminateLock = new ReentrantLock();
  public static java.util.concurrent.locks.Condition condVar = terminateLock.newCondition();
  public static final String AML_FILE_PATH = "sample_data_model.aml";
  public static final String TNS_CONFIG_FILE_PATH = "tnsConf.json";
  public static final String LOCAL_HOST = "localhost";
  private static String mServerPublicKey = "tXJx&1^QE2g7WCXbF.$$TVP.wCtxwNhR8?iLi&S<";
  private static String mClientPublicKey = "-QW?Ved(f:<::3d5tJ$[4Er&]6#9yr=vha/caBc(";
  private static String mClientSecretKey = "ZB1@RS6Kv^zucova$kH(!o>tZCQ.<!Q)6-0aWFmW";

  public static EZMQXXmlSubCallback mCallback = new EZMQXXmlSubCallback() {
    public void onMessage(String topic, String data) {
      System.out.println("[APP Callback] Topic: " + topic + "  data: " + data);
    }

    public void onError(String topic, EZMQXErrorCode errorCode) {
      System.out.println("[APP Callback] Topic: " + topic + "  Errorcode: " + errorCode);
    }
  };

  private static String getTNSAddres(String address) {
    return "http://" + address + ":80/tns-server";
  }

  private static void printError() {
    System.out.println("\nRe-run the application as shown in below example: ");
    System.out.println("\n  (1) For running in standalone mode: ");
    System.out.println(
        "      $ java -jar ezmqx-xmlsubscriber-sample.jar -ip 192.168.1.1 -port 5562 -t /topic");
    System.out.println("\n  (2) For running in standalone mode [Secured]: ");
    System.out.println(
        "      $ java -jar ezmqx-xmlsubscriber-sample.jar -ip 192.168.1.1 -port 5562 -t /topic -secured 1");
    System.out.println("\n  (3) For running in standalone mode: [With TNS] ");
    System.out
        .println("      $ java -jar ezmqx-xmlsubscriber-sample.jar -t /topic -tns 192.168.10.1");
    System.out.println("\n  (4) For running in standalone mode [With TNS + Secured]: ");
    System.out.println(
        "      $ java -jar ezmqx-xmlsubscriber-sample.jar -t /topic -tns 192.168.10.1 -secured 1 ");
    System.out.println("\n  (5) For running in docker mode: ");
    System.out.println("      $ java -jar ezmqx-xmlsubscriber-sample.jar -t /topic -h true");
    System.out.println("\n  (6) For running in docker mode [Secured]: ");
    System.out
        .println("      $ java -jar ezmqx-xmlsubscriber-sample.jar -t /topic -h true -secured 1");
    System.out.println("\nNote:");
    System.out.println("  (1) hierarchical search for topic from TNS server is not supported in this sample");
    System.out
        .println("  (2) docker mode will work only when sample is running in docker container");

    System.exit(-1);
  }

  public static void main(String[] args) {
    if (args.length != 4 && args.length != 6 && args.length != 8) {
      printError();
    }

    int n = 0;
    String topic = null;
    String ip = null;
    String tnsAddr = null;
    int port = 0;
    int isSecured = 0;
    boolean isStandAlone = false;
    while (n < args.length) {
      if (args[n].equalsIgnoreCase("-ip")) {
        ip = args[n + 1];
        System.out.println("Given Ip: " + ip);
        n = n + 2;
        isStandAlone = true;
      } else if (args[n].equalsIgnoreCase("-port")) {
        port = Integer.parseInt(args[n + 1]);
        System.out.println("Given Port: " + port);
        n = n + 2;
      } else if (args[n].equalsIgnoreCase("-t")) {
        topic = args[n + 1];
        System.out.println("Topic is : " + topic);
        n = n + 2;
      } else if (args[n].equalsIgnoreCase("-secured")) {
        isSecured = Integer.parseInt(args[n + 1]);
        System.out.println("Is Secured: " + isSecured);
        n = n + 2;
      } else if (args[n].equalsIgnoreCase("-tns")) {
        tnsAddr = args[n + 1];
        System.out.println("TNS Address : " + tnsAddr);
        n = n + 2;
        isStandAlone = true;
      } else {
        printError();
      }
    }

    // handle command line ctrl+c signal, terminate subscriber
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      public void run() {
        try {
          if (null != mSubscriber) {
            mSubscriber.terminate();
            System.out.println("Terminated subscriber!!");
          }
          if (null != mConfig) {
            mConfig.reset();
            System.out.println("Reset Config done!!");
          }
          terminateLock.lock();
          condVar.signalAll();
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          terminateLock.unlock();
        }
      }
    }));

    try {
      mConfig = EZMQXConfig.getInstance();
      if (isStandAlone) {
        if (null == tnsAddr) {
          mConfig.startStandAloneMode("", false, "");
        } else {
          mConfig.startStandAloneMode("", true, getTNSAddres(tnsAddr));
        }
      } else {
        mConfig.startDockerMode(TNS_CONFIG_FILE_PATH);
      }
      System.out.println("Config done");

      List<String> amlFilePath = new ArrayList<String>();
      amlFilePath.add(AML_FILE_PATH);
      List<String> IdList = mConfig.addAmlModel(amlFilePath);
      System.out.println("Id is: " + IdList.get(0));

      if (isStandAlone) {
        if (0 == isSecured) {
          if (null == tnsAddr) {
            EZMQXEndPoint endPoint = new EZMQXEndPoint(ip, port);
            EZMQXTopic topic1 = new EZMQXTopic(topic, IdList.get(0), false, endPoint);
            mSubscriber = EZMQXXmlSubscriber.getSubscriber(topic1, mCallback);
          } else {
            mSubscriber = EZMQXXmlSubscriber.getSubscriber(topic, true, mCallback);
          }
        } else {
          //Topic discovery
          if (null == tnsAddr) {
            EZMQXEndPoint endPoint = new EZMQXEndPoint(ip, port);
            EZMQXTopic topic1 = new EZMQXTopic(topic, IdList.get(0), true, endPoint);
            mSubscriber = EZMQXXmlSubscriber.getSecuredSubscriber(topic1, mServerPublicKey,
                mClientPublicKey, mClientSecretKey, mCallback);
          } else {
            EZMQXTopicDiscovery topicDiscovery = new EZMQXTopicDiscovery();
            EZMQXTopic tdTopic = topicDiscovery.query(topic);
            EZMQXTopic topic1 = new EZMQXTopic(tdTopic.getName(), tdTopic.getDatamodel(),
                tdTopic.isSecured(), tdTopic.getEndPoint());
            mSubscriber = EZMQXXmlSubscriber.getSecuredSubscriber(topic1, mServerPublicKey,
                mClientPublicKey, mClientSecretKey, mCallback);
          }
        }
      } else {
        if (0 == isSecured) {
          mSubscriber = EZMQXXmlSubscriber.getSubscriber(topic, true, mCallback);
        } else {
          // Do topic discovery to get end point info
          EZMQXTopicDiscovery topicDiscovery = new EZMQXTopicDiscovery();
          EZMQXTopic tdTopic = topicDiscovery.query(topic);
          EZMQXTopic topic1 = new EZMQXTopic(tdTopic.getName(), tdTopic.getDatamodel(),
              tdTopic.isSecured(), tdTopic.getEndPoint());
          mSubscriber = EZMQXXmlSubscriber.getSecuredSubscriber(topic1, mServerPublicKey,
              mClientPublicKey, mClientSecretKey, mCallback);
        }
      }
      if (null == mSubscriber) {
        System.out.println("Subscriber is null");
        return;
      }
      System.out
          .println("Suscribed to publisher.. -- Waiting for Events -- [press ctrl+c to exit]");
    } catch (EZMQXException e) {
      System.out.println(
          "[App] Exception occured [Errorcode]: " + e.getCode() + "  [Message]: " + e.getMsg());
      return;
    }

    // Prevent main thread from exit
    try {
      terminateLock.lock();
      condVar.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      terminateLock.unlock();
    }
  }
}
