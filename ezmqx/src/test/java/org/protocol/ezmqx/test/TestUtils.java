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

package org.protocol.ezmqx.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.datamodel.aml.AMLData;
import org.datamodel.aml.AMLException;
import org.datamodel.aml.AMLObject;
import org.protocol.ezmqx.internal.RestUtils;

public class TestUtils {
  public static final String ADDRESS = "127.0.0.1";
  public static final String TNS_ADDRESS = "http://192.168.0.1:80/tns-server";
  public static final String LOCAL_HOST = "localhost";
  public static final int PORT = 5562;
  public static final String TOPIC = "/topic";
  public static final String INVALID_TOPIC = "topic";
  public static final String FILE_PATH = "src/test/resources/sample_data_model.aml";
  public static final String TNS_CONFIG_FILE_PATH = "src/test/resources/tnsConf.json";

  // this key only used on unittests.
  public static String SERVER_SECRET_KEY = "[:X%Q3UfY+kv2A^.wv:(qy2E=bk0L][cm=mS3Hcx";
  public static String SERVER_PUBLIC_KEY = "tXJx&1^QE2g7WCXbF.$$TVP.wCtxwNhR8?iLi&S<";
  public static String CLIENT_PUBLIC_KEY = "-QW?Ved(f:<::3d5tJ$[4Er&]6#9yr=vha/caBc(";
  public static String CLIENT_SECRET_KEY = "ZB1@RS6Kv^zucova$kH(!o>tZCQ.<!Q)6-0aWFmW";

  public static final String CONFIG_URL =
      "http://pharos-node:48098/api/v1/management/device/configuration";

  // TODO insert pharos-web-client-ip pharos-node-ip
  public static final String VALID_CONFIG_RESPONSE =
      "{\"properties\": [{  \"pinginterval\": \"10\",  \"readOnly\": false}, {  \"anchoraddress\": \"${PHAROS-WEB-CLIENT-IP}\",  \"readOnly\": true}, {  \"deviceid\": \"71e8707c-f93b-4b77-a606-2860868429b7\",  \"readOnly\": true}, {  \"devicename\": \"MgmtServer\",  \"readOnly\": false}, {  \"nodeaddress\": \"${PHAROS-NODE-IP}\",  \"readOnly\": true}, {  \"readOnly\": false,  \"reverseproxy\": {\"enabled\": true  }}, {  \"anchorendpoint\": \"http://${PHAROS-WEB-CLIENT-IP}:80/pharos-anchor/api/v1\",  \"readOnly\": true}, {  \"os\": \"linux\",  \"readOnly\": true}, {  \"platform\": \"Ubuntu 16.04.4 LTS\",  \"readOnly\": true}, {  \"processor\": [{\"cpu\": \"0\",\"modelname\": \"Intel(R) Core(TM) i5 CPU 750  @ 2.67GHz\"  }, {\"cpu\": \"1\",\"modelname\": \"Intel(R) Core(TM) i5 CPU 750  @ 2.67GHz\"  }, {\"cpu\": \"2\",\"modelname\": \"Intel(R) Core(TM) i5 CPU 750  @ 2.67GHz\"  }, {\"cpu\": \"3\",\"modelname\": \"Intel(R) Core(TM) i5 CPU 750  @ 2.67GHz\"  }],  \"readOnly\": true}] }";
  public static final String TNS_INFO_URL =
      "http://${PHAROS-WEB-CLIENT-IP}:80/pharos-anchor/api/v1/search/nodes?imageName=system-tns-server-go/ubuntu_x86_64";
  public static final String VALID_TNS_INFO_RESPONSE =
      "{\"nodes\": [{  \"id\": \"node_id_sample\",  \"ip\": \"192.168.0.1\",  \"status\": \"connected\",  \"apps\": [ \"app_id_sample1\", \"app_id_sample2\"  ],  \"config\": { \"properties\": [ {\"deviceid\": \"00000000-0000-0000-0000-000000000000\",\"readOnly\": true }, {\"devicename\": \"EdgeDevice\",\"readOnly\": false }, {\"pinginterval\": \"10\",\"readOnly\": false }, {\"os\": \"linux\",\"readOnly\": true }, {\"processor\": [{  \"cpu\": \"0\",  \"modelname\": \"Intel(R) Core(TM) i7-2600 CPU @ 3.40GHz\"}],\"readOnly\": true }, {\"platform\": \"Ubuntu 16.04.3 LTS\",\"readOnly\": true }, {\"reverseproxy\": {\"enabled\": true},\"readOnly\": true } ]  }}] }";
  public static final String RUNNING_APPS_URL = "http://pharos-node:48098/api/v1/management/apps";
  public static final String VALID_RUNNING_APPS_RESPONSE =
      "{\"apps\": [{\"id\": \"103dd8cca769ce1aee520511f7379fdfe2a909cc\",\"state\": \"running\"}] }";
  public static final String RUNNING_APP_INFO_URL =
      "http://pharos-node:48098/api/v1/management/apps/103dd8cca769ce1aee520511f7379fdfe2a909cc";

  public final static String hostName = readHostName(RestUtils.HOSTNAME);
  public static final String RUNNING_APP_INFO_RESPONSE =
      "{\"images\": [{\"name\": \"system-provisioning-director/ubuntu_x86_64\"   }],     \"services\": [{  \"cid\": \""
          + hostName
          + "\",\"name\": \"system-provisioning-director\",   \"ports\": [{ \"IP\": \"0.0.0.0\",  \"PrivatePort\": 4000,   \"PublicPort\": 4000,\"Type\": \"tcp\" }],   \"state\": {  \"exitcode\": \"0\",  \"status\": \"running\"   }   }],     \"state\": \"running\" }";

  public static final String TOPIC_DISCOVERY_H_URL =
      "http://192.168.0.1:80/tns-server/api/v1/tns/topic?name=/topic&hierarchical=yes";
  public static final String TOPIC_DISCOVERY_URL =
      "http://192.168.0.1:80/tns-server/api/v1/tns/topic?name=/topic&hierarchical=no";
  public static final String VALID_TOPIC_DISCOVERY_RESPONSE =
      "{ \"topics\": [ { \"name\": \"topicName\", \"datamodel\": \"GTC_Robot_0.0.1\", \"endpoint\": \"localhost:5562\" , \"secured\": \"false\"} ] }";

  public static final String PUB_TNS_URL = "http://192.168.0.1:80/tns-server/api/v1/tns/topic";
  public static final String VALID_PUB_TNS_RESPONSE = "{\"ka_interval\": \"200\"}";
  public static final String SUB_TOPIC_H_URL =
      "http://192.168.0.1:80/tns-server/api/v1/tns/topic?name=/topic&hierarchical=yes";
  public static final String SUB_TOPIC_RESPONSE =
      "{ \"topics\": [ { \"name\": \"topicName\", \"datamodel\": \"GTC_Robot_0.0.1\", \"endpoint\": \"localhost:5562\" , \"secured\": \"false\"} ] }";
  public static final String SUB_TOPIC_URL =
      "http://192.168.0.1:80/tns-server/api/v1/tns/topic?name=/topic&hierarchical=no";


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

    // Add Data to AMLObject
    amlObj.addData("Model", model);
    amlObj.addData("Sample", sample);

    return amlObj;
  }

  public static String readHostName(String filePath) {
    String hostName = "";
    BufferedReader bufferedReader = null;
    FileReader fileReader = null;
    try {
      fileReader = new FileReader(filePath);
      bufferedReader = new BufferedReader(fileReader);
      hostName = bufferedReader.readLine();
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
    return hostName;
  }
}
