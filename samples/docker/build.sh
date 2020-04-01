###############################################################################
# Copyright 2018 Samsung Electronics All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
###############################################################################

#!/bin/sh

DOCKER_ROOT=$(pwd)

if [ -d "./samples" ] ; then
        echo "samples folder exists"
    else
        mkdir samples
fi

cd ./samples
# copy all the samples
cp ./../../ezmqx-publisher/target/ezmqx-publisher-sample.jar .
cp ./../../ezmqx-amlsubscriber/target/ezmqx-amlsubscriber-sample.jar .
cp ./../../ezmqx-xmlsubscriber/target/ezmqx-xmlsubscriber-sample.jar .
cp ./../../ezmqx-topicdiscovery/target/ezmqx-topicdiscovery-sample.jar .
#copy .aml file
cp ./../../ezmqx-publisher/src/main/resources/sample_data_model.aml .
#copy tnsConf file
cp ./../../ezmqx-publisher/src/main/resources/tnsConf.json .

cd $DOCKER_ROOT
if [ -d "./libs" ] ; then
        echo "libs folder exists"
    else
        mkdir libs
fi

cd ./libs
# copy aml libs
cp ./../../../dependencies/datamodel-aml-java/libaml.so .
cp ./../../../dependencies/datamodel-aml-java/libjniaml.so .
#copy protobuf.so
cp /usr/local/lib/libprotobuf.so.14 .
#copy libstd++.so.6
cp /usr/lib/arm-linux-gnueabihf/libstdc++.so.6 .

cd $DOCKER_ROOT

# build the ezmq-plus-java sample image
sudo docker build -t protocol-ezmq-plus-java-sample -f Dockerfile_arm .


