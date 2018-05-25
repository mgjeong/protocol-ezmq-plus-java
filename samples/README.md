# ezmq-plus samples instructions [executable jar]

ezmq-plus has publisher, amlsubscriber, xmlsubscriber and topic-discovery sample applications. Build and run using the following guide to experiment different options in sample.

## Prerequisites
 - Built ezmq-plus library

## How to build
(1) If  ezmq-plus library is built using provided build scripts, It will build all the samples along with library.</br>
(2) In case you want to build specific sample. Go to required sample folder and run **build.sh**.

## How to run
### Topic Discovery sample ###
1. Goto: ~/protocol-ezmq-java/samples/ezmqx-topicdiscovery/target/
2. export LD_LIBRARY_PATH=../../../dependencies/datamodel-aml-java/
3. Run the sample:
    ```
    $ java -jar ezmqx-topicdiscovery-sample.jar
    ```
**Note:** It will give list of options for running the sample. 

### Publisher sample ###
1. Goto: ~/protocol-ezmq-java/samples/ezmqx-publisher/target/
2. export LD_LIBRARY_PATH=../../../dependencies/datamodel-aml-java/
3. Run the sample:
    ```
    $ java -jar ezmqx-publisher-sample.jar
    ```
**Note:** It will give list of options for running the sample. 

### AML Subscriber sample ###
1. Goto: ~/protocol-ezmq-java/samples/ezmqx-amlsubscriber/target/
2. export LD_LIBRARY_PATH=../../../dependencies/datamodel-aml-java/
3. Run the sample:
    ```
    $ java -jar ezmqx-amlsubscriber-sample.jar
    ```
**Note:** It will give list of options for running the sample. 

### XML Subscriber sample ###
1. Goto: ~/protocol-ezmq-java/samples/ezmqx-xmlsubscriber/target/
2. export LD_LIBRARY_PATH=../../../dependencies/datamodel-aml-java/
3. Run the sample:
    ```
    $ java -jar ezmqx-xmlsubscriber-sample.jar
    ```
**Note:** It will give list of options for running the sample. 

# ezmq-plus samples instructions [docker]

## Prerequisites
 - Built ezmq-plus library
 - docker-ce
    - Version: 17.09
    - [How to install](https://docs.docker.com/engine/installation/linux/docker-ce/ubuntu/)

## How to build
1. Go to ~/protocol-ezmq-plus-java/samples/docker
2. Build the docker image:
   ```
   $ ./build.sh
   ```
**Notes:** </br>
(1) It will build the docker image [**docker.sec.samsung.net:5000/protocol-ezmq-plus-java-sample**] for ezmq-plus samples. </br>
(2) Run following command to see image details: 
    ```
     $ docker images
    ``` 

## How to run
### Topic Discovery sample ###
1. Run the sample:
    ```
    $ docker run docker.sec.samsung.net:5000/protocol-ezmq-plus-java-sample /run.sh topicdiscovery /topic
    ```
**Note:** Update the **/topic**, with topic of interest.

### Publisher sample ###
1. Run the sample:
    ```
    $ docker run docker.sec.samsung.net:5000/protocol-ezmq-plus-java-sample /run.sh publisher /topic
    ```
**Note:** Update the **/topic**, with topic of interest.

### AML Subscriber sample ###
1. Run the sample:
    ```
    $ docker run docker.sec.samsung.net:5000/protocol-ezmq-plus-java-sample /run.sh amlsubscriber /topic
    ```
**Note:** Update the **/topic**, with topic of interest.

### XML Subscriber sample ###
1. Run the sample:
    ```
    $ docker run docker.sec.samsung.net:5000/protocol-ezmq-plus-java-sample /run.sh xmlsubscriber /topic
    ```
**Note:** Update the **/topic**, with topic of interest.