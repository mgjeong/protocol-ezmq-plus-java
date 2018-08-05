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

import org.datamodel.aml.AMLData;
import org.datamodel.aml.AMLException;
import org.datamodel.aml.AMLObject;
import org.protocol.ezmqx.EZMQXAmlSubscriber;
import org.protocol.ezmqx.EZMQXAmlSubscriber.EZMQXAmlSubCallback;
import org.protocol.ezmqx.EZMQXConfig;
import org.protocol.ezmqx.EZMQXEndPoint;
import org.protocol.ezmqx.EZMQXErrorCode;
import org.protocol.ezmqx.EZMQXException;
import org.protocol.ezmqx.EZMQXTopic;

public class AmlSubscriber {
	public static EZMQXConfig mConfig = null;
	public static EZMQXAmlSubscriber mSubscriber = null;
	public static Lock terminateLock = new ReentrantLock();
	public static java.util.concurrent.locks.Condition condVar = terminateLock.newCondition();
	public static final String AML_FILE_PATH = "sample_data_model.aml";
	public static final String TNS_CONFIG_FILE_PATH = "tnsConf.json";
	public static final String LOCAL_HOST = "localhost";

	public static EZMQXAmlSubCallback mCallback = new EZMQXAmlSubCallback() {
		public void onMessage(String topic, AMLObject data) {
			System.out.println("[APP Callback] Topic : " + topic);
			try {
				List<String> dataKeys = data.getDataNames();
				for (String key : dataKeys) {
					System.out.println("Key: " + key);
				}
			} catch (AMLException e) {
				e.printStackTrace();
			}
			printAMLObject(data);
		}

		public void onError(String topic, EZMQXErrorCode errorCode) {
			System.out.println("[APP Callback] Topic: " + topic + "  Errorcode: " + errorCode);
		}
	};

	public static void printAMLObject(AMLObject amlObj) {
		try {
			System.out.println("{");
			System.out.println("    \"device\" : " + amlObj.getDeviceId() + ",");
			System.out.println("    \"timestamp\" : " + amlObj.getTimeStamp() + ",");
			System.out.println("    \"id\" : " + amlObj.getId() + ",");

			List<String> dataNames = amlObj.getDataNames();
			for (String n : dataNames) {
				AMLData data = amlObj.getData(n);

				System.out.println("    \"" + n + "\" : ");
				printAMLData(data, 1);
				if (dataNames.indexOf(n) != dataNames.size() - 1)
					System.out.println(",");
			}
			System.out.println("\n}");
		} catch (AMLException e) {
			System.out.println(e.toString());
		}
	}

	public static void printAMLData(AMLData amlData, int depth) {
		try {
			String indent = new String("");
			for (int i = 0; i < depth; i++)
				indent += "    ";

			System.out.println(indent + "{");
			List<String> keys = amlData.getKeys();
			for (String key : keys) {
				System.out.print(indent + "    \"" + key + "\" : ");

				AMLData.ValueType type = amlData.getValueType(key);
				if (AMLData.ValueType.STRING == type) {
					String valStr = amlData.getValueToStr(key);
					System.out.print(valStr);
				} else if (AMLData.ValueType.STRING_LIST == type) {
					List<String> valStrList = amlData.getValueToStrList(key);
					System.out.print("[");
					for (String val : valStrList) {
						System.out.print(val);
						if (valStrList.indexOf(val) != valStrList.size() - 1)
							System.out.print(", ");
					}
					System.out.print("]");
				} else if (AMLData.ValueType.AMLDATA == type) {
					AMLData valAMLData = amlData.getValueToAMLData(key);
					System.out.print("\n");
					printAMLData(valAMLData, depth + 1);
				}
				if (keys.indexOf(key) != keys.size() - 1)
					System.out.print(",");
				System.out.print("\n");
			}
			System.out.print(indent + "}");
		} catch (AMLException e) {
			System.out.println(e.toString());
		}
	}

	private static void printError() {
		System.out.println("\nRe-run the application as shown in below example: ");
		System.out.println("\n  (1) For running in standalone mode: ");
		System.out.println("      $ java -jar ezmqx-amlsubscriber-sample.jar -ip 192.168.1.1 -port 5562 -t /topic");
		System.out.println("\n  (2) For running in docker mode: ");
		System.out.println("      $ java -jar ezmqx-amlsubscriber-sample.jar -t /topic -h true");
		System.out.println("\n Note: -h stands for hierarchical search for topic from TNS server");
		System.exit(-1);
	}

	public static void main(String[] args) {
		if (args.length != 4 && args.length != 6) {
			printError();
		}

		int n = 0;
		String topic = null;
		String ip = null;
		String hierarchical = null;
		int port = 0;
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
			} else if (args[n].equalsIgnoreCase("-h")) {
				hierarchical = args[n + 1];
				System.out.println("Is hierarchical : " + hierarchical);
				n = n + 2;
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
				mConfig.startStandAloneMode(LOCAL_HOST, false, "");
			} else {
				mConfig.startDockerMode(TNS_CONFIG_FILE_PATH);
			}
			System.out.println("Config done");

			List<String> amlFilePath = new ArrayList<String>();
			amlFilePath.add(AML_FILE_PATH);
			List<String> IdList = mConfig.addAmlModel(amlFilePath);
			System.out.println("Id is: " + IdList.get(0));

			if (isStandAlone) {
				EZMQXEndPoint endPoint = new EZMQXEndPoint(ip, port);
				EZMQXTopic topic1 = new EZMQXTopic(topic, IdList.get(0), endPoint);
				mSubscriber = EZMQXAmlSubscriber.getSubscriber(topic1, mCallback);
			} else {
				mSubscriber = EZMQXAmlSubscriber.getSubscriber(topic, true, mCallback);
			}
			if (null == mSubscriber) {
				System.out.println("Subscriber is null");
				return;
			}
			System.out.println("Suscribed to publisher.. -- Waiting for Events -- [press ctrl+c to exit]");
		} catch (EZMQXException e) {
			System.out.println("[App] Exception occured [Errorcode]: " + e.getCode() + "  [Message]: " + e.getMsg());
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
