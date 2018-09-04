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
	public static final String LOCAL_HOST = "localhost";

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
		System.out.println("\n  (2) For running in docker mode: ");
		System.out.println("      $ java -jar ezmqx-publisher-sample.jar -t /topic");
		System.exit(-1);
	}

	private static void publishData(AMLObject amlObject, int numberOfEvents) throws EZMQXException {
		System.out.println(
				"------------- Will publish " + numberOfEvents + " events at the interval of 2 seconds --------------");
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

	public static void main(String[] args) {
		if (args.length != 2 && args.length != 4) {
			printError();
		}

		int n = 0;
		String topic = null;
		int port = 0;
		boolean isStandAlone = false;
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
			} else {
				printError();
			}
		}

		// handle command line ctrl+c signal, terminate publisher and ezmq
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
				mConfig.startStandAloneMode(LOCAL_HOST, false, "");
			} else {
				mConfig.startDockerMode(TNS_CONFIG_FILE_PATH);
			}
			System.out.println("Config done");

			List<String> amlFilePath = new ArrayList<String>();
			amlFilePath.add(AML_FILE_PATH);
			List<String> IdList = mConfig.addAmlModel(amlFilePath);
			System.out.println("Id is: " + IdList.get(0));
			mPublisher = EZMQXAmlPublisher.getPublisher(topic, EZMQXAmlModelInfo.AML_MODEL_ID, IdList.get(0), port);
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
				publishData(amlObject, 10);
			} else {
				publishData(amlObject, 100000);
			}
		} catch (EZMQXException e) {
			System.out.println("[App] Exception occured [Errorcode]: " + e.getCode() + "  [Message]: " + e.getMsg());
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
