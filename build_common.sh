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

#!/bin/bash
set +e
#Colors
RED="\033[0;31m"
GREEN="\033[0;32m"
BLUE="\033[0;34m"
NO_COLOUR="\033[0m"

PROJECT_ROOT=$(pwd)
EZMQX_TARGET_ARCH="x86_64"
EZMQX_WITH_DEP=false
EZMQX_INSTALL_PREREQUISITES=false

PROTOCOL_EZMQ_JAVA_VERSION=v1.0_rc1
DATAMODEL_AML_JAVA_VERSION=v1.0_rc1

install_dependencies() {
    if [ -d "./dependencies" ] ; then
        echo "dependencies folder exists"
    else
        mkdir dependencies
    fi
    cd ./dependencies

    # protocol-ezmq-java
    if [ -d "./protocol-ezmq-java" ] ; then
        echo "protocol-ezmq-java already exists"
    else
        git clone git@github.sec.samsung.net:RS7-EdgeComputing/protocol-ezmq-java.git
    fi

    cd ./protocol-ezmq-java
    git checkout ${PROTOCOL_EZMQ_JAVA_VERSION}
    echo -e "Building protocol-ezmq-java library"
    ./build.sh

    # datamodel-aml-java
    cd ../
    if [ -d "./datamodel-aml-java" ] ; then
        echo "datamodel-aml-java already exists"
    else
        git clone git@github.sec.samsung.net:RS7-EdgeComputing/datamodel-aml-java.git
    fi

    cd ./datamodel-aml-java
    git checkout ${DATAMODEL_AML_JAVA_VERSION}
    echo -e "Building datamodel-aml-java library"
    ./build_common.sh --target_arch=${EZMQX_TARGET_ARCH} --install_prerequisites=${EZMQX_INSTALL_PREREQUISITES}
    if [ $? -ne 0 ]; then
        echo -e "${RED}Build failed${NO_COLOUR}"
        exit 1
    fi
    echo -e "Installation of dependencies done"
}

usage() {
    echo -e "${BLUE}Usage:${NO_COLOUR} ./build_common.sh <option>"
    echo -e "${GREEN}Options:${NO_COLOUR}"
    echo "  --target_arch=[x86_64|armhf]                                 :  Choose Target Architecture"
    echo "  --with_dependencies=[true|false](default: false)             :  Build ezmq-plus along with dependencies [ezmq and aml]"
    echo "  --install_prerequisites=[true|false](default: false)         :  Install the prerequisite S/W to build internal aml library [protobuf]"
    echo "  -c                                                           :  Clean ezmq plus repository"
    echo "  -h / --help                                                  :  Display help and exit"
}

build() {
    if [ ${EZMQX_WITH_DEP} = true ]; then
        install_dependencies
    fi

    echo -e "Building EZMQ Plus Java library("${EZMQX_TARGET_ARCH}").."
    # Build EZMQX Java
    cd $PROJECT_ROOT/ezmqx
    ./build.sh
    if [ $? -ne 0 ]; then
        echo -e "${RED}Build failed${NO_COLOUR}"
        exit 1
    fi

    # Build Samples
    cd $PROJECT_ROOT/samples/ezmqx-publisher
    ./build.sh
    if [ $? -ne 0 ]; then
        echo -e "${RED}ezmqx-publisher build failed${NO_COLOUR}"
        exit 1
    fi

    cd $PROJECT_ROOT/samples/ezmqx-amlsubscriber
    ./build.sh
    if [ $? -ne 0 ]; then
        echo -e "${RED}ezmqx-amlsubscriber build failed${NO_COLOUR}"
        exit 1
    fi

    cd $PROJECT_ROOT/samples/ezmqx-xmlsubscriber
    ./build.sh
    if [ $? -ne 0 ]; then
        echo -e "${RED}ezmqx-xmlsubscriber build failed${NO_COLOUR}"
        exit 1
    fi

    cd $PROJECT_ROOT/samples/ezmqx-topicdiscovery
    ./build.sh
    if [ $? -ne 0 ]; then
        echo -e "${RED}ezmqx-topicdiscovery build failed${NO_COLOUR}"
        exit 1
    fi
    echo -e "Done building EZMQ Plus Java library("${EZMQX_TARGET_ARCH}")"
}

clean() {
    cd $PROJECT_ROOT
    make clean

    cd $PROJECT_ROOT/java
    mvn clean

    cd $PROJECT_ROOT/samples/ezmqx-publisher
    mvn clean

    cd $PROJECT_ROOT/samples/ezmqx-amlsubscriber
    mvn clean

    cd $PROJECT_ROOT/samples/ezmqx-xmlsubscriber
    mvn clean

    cd $PROJECT_ROOT/samples/ezmqx-topicdiscovery
    mvn clean

    echo -e "Finished Cleaning"
}

process_cmd_args() {
    if [ "$#" -eq 0  ]; then
        echo -e "No argument.."
        usage; exit 1
    fi
    while [ "$#" -gt 0  ]; do
        case "$1" in
            --with_dependencies=*)
                EZMQX_WITH_DEP="${1#*=}";
                if [ ${EZMQX_WITH_DEP} != true ] && [ ${EZMQX_WITH_DEP} != false ]; then
                    echo -e "${RED}Unknown option for --with_dependencies${NO_COLOUR}"
                    exit 1
                fi
                echo -e "${GREEN}Install dependencies [ezmq and aml] before build: ${EZMQX_WITH_DEP}${NO_COLOUR}"
                shift 1;
                ;;
            --install_prerequisites=*)
                EZMQX_INSTALL_PREREQUISITES="${1#*=}";
                if [ ${EZMQX_INSTALL_PREREQUISITES} != true ] && [ ${EZMQX_INSTALL_PREREQUISITES} != false ]; then
                    echo -e "${RED}Unknown option for --install_prerequisites${NO_COLOUR}"
                    exit 1
                fi
                echo -e "${GREEN}Install the prerequisites before build: ${EZMQX_INSTALL_PREREQUISITES}${NO_COLOUR}"
                shift 1;
                ;;
            --target_arch=*)
                EZMQX_TARGET_ARCH="${1#*=}";
                echo -e "${GREEN}Target Arch is: $EZMQX_TARGET_ARCH${NO_COLOUR}"
                shift 1
                ;;
            -c)
                clean
                shift 1; exit 0
                ;;
            -h)
                usage; exit 0
                ;;
            --help)
                usage; exit 0
                ;;
            -*)
                echo -e "${RED}"
                echo "unknown option: $1" >&2;
                echo -e "${NO_COLOUR}"
                usage; exit 1
                ;;
            *)
                echo -e "${RED}"
                echo "unknown option: $1" >&2;
                echo -e "${NO_COLOUR}"
                usage; exit 1
                ;;
        esac
    done
}

process_cmd_args "$@"
build

exit 0

