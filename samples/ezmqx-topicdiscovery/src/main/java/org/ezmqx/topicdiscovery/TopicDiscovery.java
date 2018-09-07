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

package org.ezmqx.topicdiscovery;

import java.util.List;
import org.protocol.ezmqx.EZMQXConfig;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.EZMQXTopic;
import org.protocol.ezmqx.EZMQXTopicDiscovery;

public class TopicDiscovery {
  public static final String TNS_CONFIG_FILE_PATH = "tnsConf.json";

  private static void printError() {
    System.out.println("\nRe-run the application as shown in below example: ");
    System.out.println("\n  (1) For running in standalone mode: ");
    System.out
        .println("      $ java -jar ezmqx-topicdiscovery-sample.jar -t /a/b/c -tns 192.168.8.6");
    System.out.println("\n  (2) For running in docker mode: ");
    System.out.println("      $ java -jar ezmqx-topicdiscovery-sample.jar -t /a/b/c");
    System.out
        .println("\nNote: docker mode will work only when sample is running in docker container");
    System.exit(-1);
  }

  public static void printTopicList(List<EZMQXTopic> topicList) {
    if (0 == topicList.size()) {
      System.out.println("Topic list is empty.....");
      return;
    }
    for (EZMQXTopic ezmqXTopic : topicList) {
      System.out.println("=================================================");
      System.out.println("Topic: " + ezmqXTopic.getName());
      System.out.println("Endpoint: " + ezmqXTopic.getEndPoint().toString());
      System.out.println("Data Model: " + ezmqXTopic.getDatamodel());
      System.out.println("Is secured: " + ezmqXTopic.isSecured());
      System.out.println("=================================================");
    }
  }

  public static void main(String[] args) {
    if (args.length != 2 && args.length != 4) {
      printError();
    }
    int n = 0;
    String topic = null;
    String tnsAddress = null;
    boolean isStandAlone = false;
    while (n < args.length) {
      if (args[n].equalsIgnoreCase("-t")) {
        topic = args[n + 1];
        System.out.println("Topic is : " + topic);
        n = n + 2;
      } else if (args[n].equalsIgnoreCase("-tns")) {
        tnsAddress = args[n + 1];
        System.out.println("TNS address is : " + tnsAddress);
        n = n + 2;
        isStandAlone = true;
      } else {
        printError();
      }
    }

    EZMQXConfig config = null;
    try {
      config = EZMQXConfig.getInstance();
      if (isStandAlone) {
        config.startStandAloneMode("localhost", true, "http://" + tnsAddress + ":80/tns-server");
      } else {
        config.startDockerMode(TNS_CONFIG_FILE_PATH);
      }
      System.out.println("Config done");
    } catch (EZMQXException e) {
      System.out.println("Config exception Errorcode: " + e.getCode() + "  Message: " + e.getMsg());
      return;
    }

    List<EZMQXTopic> topicList;
    try {
      EZMQXTopicDiscovery topicDiscovery = new EZMQXTopicDiscovery();
      topicList = topicDiscovery.hierarchicalQuery(topic);
    } catch (EZMQXException e) {
      System.out.println("Caught exception: " + e.getMsg());
      return;
    }
    printTopicList(topicList);

    // In docker mode, wait for 5 minutes before exit [For docker mode].
    if (!isStandAlone) {
      try {
        System.out.println(
            "Waiting for 5 minutes before program exit for docker mode... [press ctrl+c to exit]");
        Thread.sleep(300000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    try {
      config.reset();
      System.out.println("Reset Config done!!");
    } catch (EZMQXException e) {
      e.printStackTrace();
    }
  }
}
