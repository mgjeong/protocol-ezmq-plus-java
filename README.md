# ezmq-plus library (Java)

protocol-ezmq-plus-java is a library (jar) which provides a standard messaging interface over various data streaming
and serialization / deserialization middlewares along with some added functionalities.</br>
  - Currently supports streaming using 0mq and serialization / deserialization using protobuf.
  - Publisher -> Multiple Subscribers broadcasting.
  - High speed serialization and deserialization.
  - Topic name discovery [TNS]. 
  - Automation Markup language [AML]


## Prerequisites ##
- JDK
  - Version : 1.8
  - [How to install](https://docs.oracle.com/javase/8/docs/technotes/guides/install/linux_jdk.html)
- Maven
  - Version : 3.5.2
  - [Where to download](https://maven.apache.org/download.cgi)
  - [How to install](https://maven.apache.org/install.html)
  - [Setting up proxy for maven](https://maven.apache.org/guides/mini/guide-proxies.html)
- protocol-ezmq-java
  - Since [protocol-ezmq-java](https://github.com/edgexfoundry-holding/protocol-ezmq-java) will be downloaded and built when protocol-ezmq-plus-java is built, check the prerequisites of it. It can be installed by build option (See 'How to build')
- datamodel-aml-java
  - Since [datamodel-aml-java](https://github.com/edgexfoundry-holding/datamodel-aml-java) will be downloaded and built when protocol-ezmq-plus-java is built, check the prerequisites of it. It can be installed by build option (See 'How to build')
 
 **Note:** Set proxy for git, if required:
 ```shell
 $ git config --global http.proxy http://proxyuser:proxypwd@proxyserver.com:8080
 ```


## How to build ##
  - Build guide of **ezmq-plus library** is given [here](./ezmqx/README.md)


## How to run ##
  - Build and run guide of **ezmq-plus samples** is given [here](./samples/README.md)

## Usage guide for ezmq library (for microservices)

1. Reference ezmq-plus library APIs : [doc/javadoc/index.html](doc/javadoc/index.html)
2. Topic naming convention guide : [Naming Guide](https://github.com/mgjeong/protocol-ezmq-plus-cpp/blob/master/TOPIC_NAMING_CONVENTION.md)
