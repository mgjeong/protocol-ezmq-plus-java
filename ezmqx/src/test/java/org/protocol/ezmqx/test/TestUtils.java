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

import java.util.ArrayList;
import org.datamodel.aml.AMLData;
import org.datamodel.aml.AMLException;
import org.datamodel.aml.AMLObject;

public class TestUtils {
    public static final String ADDRESS = "127.0.0.1";
    public static final int PORT = 5562;
    public static final String TOPIC = "/topic";
    public static final String FILE_PATH = "src/test/resources/sample_data_model.aml";

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

}
