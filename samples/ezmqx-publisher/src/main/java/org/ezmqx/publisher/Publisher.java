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

package org.ezmqx.publisher;

import java.util.ArrayList;
import java.util.List;
import org.datamodel.aml.AMLData;
import org.datamodel.aml.AMLException;
import org.datamodel.aml.AMLObject;
import org.protocol.ezmqx.EZMQXAmlModelInfo;
import org.protocol.ezmqx.EZMQXAmlPublisher;
import org.protocol.ezmqx.EZMQXConfig;
import org.protocol.ezmqx.EZMQXException;

public class Publisher {
  public static EZMQXConfig mConfig = null;
  public static EZMQXAmlPublisher mPublisher;
  public static boolean isStarted = false;
  public static final String AML_FILE_PATH = "sample_data_model.aml";
  public static final String TNS_CONFIG_FILE_PATH = "tnsConf.json";
  public static String mServerSecretKey = "[:X%Q3UfY+kv2A^.wv:(qy2E=bk0L][cm=mS3Hcx";

  public static AMLObject getAMLObject() throws AMLException {

    // create AMLObject
    String deviceId = "GTC001";
    String timeStamp = "123456789";

    AMLObject amlObj = new AMLObject(deviceId, timeStamp);

    // create "Model" data
    AMLData model = new AMLData();
    model.setValue("ctname", "Model_107.113.97.248");
    model.setValue("con", "SR-P7-970");

    // create "Sample" data
    AMLData axis = new AMLData();
    axis.setValue("x", "20");
    axis.setValue("y", "110");
    axis.setValue("z", "80");

    AMLData info = new AMLData();
    info.setValue("id", "f437da3b");
    info.setValue("axis", axis);

    ArrayList<String> appendix = new ArrayList<String>();
    appendix.add("52303");
    appendix.add("935");
    appendix.add("1442");

    AMLData sample = new AMLData();
    sample.setValue("info", info);
    sample.setValue("appendix", appendix);

    // Add Datas to AMLObject
    amlObj.addData("Model", model);
    amlObj.addData("Sample", sample);

    return amlObj;
  }

  private static void printError() {
    System.out.println("\nRe-run the application as shown in below example: ");
    System.out.println("\n  (1) For running in standalone mode: ");
    System.out.println("      $ java -jar ezmqx-publisher-sample.jar -t /topic -port 5562");
    System.out.println("\n  (2) For running in standalone mode [Secured]: ");
    System.out
        .println("      $ java -jar ezmqx-publisher-sample.jar -t /topic -port 5562 -secured 1");
    System.out.println("\n  (3) For running in standalone mode [With TNS]: ");
    System.out.println(
        "      $ java -jar ezmqx-publisher-sample.jar -t /topic -host 192.168.1.1 -port 5562 -tns 192.183.3.2");
    System.out.println("\n  (4) For running in standalone mode [With TNS + Secured]: ");
    System.out.println(
        "      $ java -jar ezmqx-publisher-sample.jar -t /topic -host 192.168.1.1 -port 5562 -tns 192.183.3.2 -secured 1");
    System.out.println("\n  (5) For running in docker mode : ");
    System.out.println("      $ java -jar ezmqx-publisher-sample.jar -t /topic");
    System.out.println("\n  (6) For running in docker mode [Secured]: ");
    System.out.println("      $ java -jar ezmqx-publisher-sample.jar -t /topic -secured 1");
    System.out
        .println("\nNote: docker mode will work only when sample is running in docker container");
    System.exit(-1);
  }


  private static void publishData(AMLObject amlObject, int numberOfEvents) throws EZMQXException {
    System.out.println("------------- Will publish " + numberOfEvents
        + " events at the interval of 2 seconds --------------");
    int i = 1;
    while (i <= numberOfEvents) {
      mPublisher.publish(amlObject);
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("Published event: " + i);
      i++;
    }
  }

  private static String getTNSAddres(String address) {
    return "http://" + address + ":80/tns-server";
  }

  public static void main(String[] args) {
    if (args.length != 2 && args.length != 4 && args.length != 6 && args.length != 8
        && args.length != 10) {
      printError();
    }

    int n = 0;
    String topic = null;
    String hostAddr = null;
    String tnsAddr = null;
    int port = 0;
    boolean isStandAlone = false;
    int isSecured = 0;
    while (n < args.length) {
      if (args[n].equalsIgnoreCase("-port")) {
        port = Integer.parseInt(args[n + 1]);
        System.out.println("Given Port: " + port);
        n = n + 2;
        isStandAlone = true;
      } else if (args[n].equalsIgnoreCase("-t")) {
        topic = args[n + 1];
        System.out.println("Topic is : " + topic);
        n = n + 2;
      } else if (args[n].equalsIgnoreCase("-secured")) {
        isSecured = Integer.parseInt(args[n + 1]);
        System.out.println("Is Secured: " + isSecured);
        n = n + 2;
      } else if (args[n].equalsIgnoreCase("-host")) {
        hostAddr = args[n + 1];
        System.out.println("host Address : " + hostAddr);
        n = n + 2;
      } else if (args[n].equalsIgnoreCase("-tns")) {
        tnsAddr = args[n + 1];
        System.out.println("TNS Address : " + tnsAddr);
        n = n + 2;
      } else {
        printError();
      }
    }

    // handle command line ctrl+c signal, terminate publisher
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      public void run() {
        try {
          if (isStarted && null != mPublisher) {
            mPublisher.terminate();
            System.out.println("Terminated publiher!!");
          }
          if (isStarted && null != mConfig) {
            mConfig.reset();
            System.out.println("Reset Config done!!");
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }));

    try {
      mConfig = EZMQXConfig.getInstance();
      if (isStandAlone) {
        if (null == tnsAddr) {
          mConfig.startStandAloneMode("", false, "");
        } else {
          mConfig.startStandAloneMode(hostAddr, true, getTNSAddres(tnsAddr));
        }
      } else {
        mConfig.startDockerMode(TNS_CONFIG_FILE_PATH);
      }
      System.out.println("Config done");

      List<String> amlFilePath = new ArrayList<String>();
      amlFilePath.add(AML_FILE_PATH);
      List<String> IdList = mConfig.addAmlModel(amlFilePath);
      System.out.println("Id is: " + IdList.get(0));
      if (0 == isSecured) {
        mPublisher = EZMQXAmlPublisher.getPublisher(topic, EZMQXAmlModelInfo.AML_MODEL_ID,
            IdList.get(0), port);
      } else {
        mPublisher = EZMQXAmlPublisher.getSecuredPublisher(topic, mServerSecretKey,
            EZMQXAmlModelInfo.AML_MODEL_ID, IdList.get(0), port);
      }
      isStarted = true;

      // create AML Object
      AMLObject amlObject = null;
      try {
        amlObject = getAMLObject();
      } catch (AMLException e) {
        System.out.println("AML object creation failed");
      }

      // This delay is added to prevent JeroMQ first packet drop during
      // initial connection of publisher and subscriber.
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      if (isStandAlone) {
        publishData(amlObject, 100000);
      } else {
        publishData(amlObject, 100000);
      }
    } catch (EZMQXException e) {
      System.out.println(
          "[App] Exception occured [Errorcode]: " + e.getCode() + "  [Message]: " + e.getMsg());
    } finally {

      try {
        if (null != mPublisher) {
          mPublisher.terminate();
          System.out.println("Terminated publiher!!");
        }
        if (null != mConfig) {
          mConfig.reset();
          System.out.println("Reset Config done!!");
        }
        isStarted = false;
      } catch (EZMQXException e) {
        e.printStackTrace();
      }
    }
  }
}
