# ezmq-plus library build instructions

## How to build ##
1. Goto: ~/protocol-ezmq-plus-java/
2. Run the script:

   ```
   ./build.sh          : Native build for x86_64
   ./build_arm.sh      : Native build for armhf [RPI]
   ./build_common.sh   : Generic build script.
   ./unittests.sh      : Unit test build script. 
   ```

**Notes** </br>
(a) For getting help about script option: **$ ./build_common.sh --help** </br>
(b) If you are building for the first time, set <i>with_dependencies</i> option true. (e.g. $./build.sh **--with_dependencies=true**).</br>
(c) If you are not having the protobuf installed, set <i>install_prerequisites</i> option true. (e.g. $./build.sh **--install_prerequisites=true**)</br>
(d) Before running the **unittests.sh**, build ezmq-java library. unittests.sh can be used for x86_64/armhf architecture.</br>
(e) Script needs sudo permission for installing the libraries. In future it will be removed.</br>
(f) To build in **debug** mode:
   - Goto: `~/protocol-ezmq-plus-java/ezmqx/src/main/resources`
   - Modify logging level in **application.properties**: 
        ` ezmqx.logging.level=DEBUG`
	
(g) To build in **unsecure** mode:
   - Goto: `~/protocol-ezmq-plus-java/ezmqx/src/main/resources`
   - Modify security flag in **application.properties**: 
        ` ezmqx.security=FALSE`  

## ezmq-plus Binary info ##
   - ezmq-plus library
     - **Library :** ezmqx-0.0.1-SNAPSHOT.jar
     - **Features :** Various data streaming/serialization/deserialization middlewares along with some added functionalities  

## Usage guide for ezmq-plus library (For micro-services) ## 
   - Include following in pom.xml
   ```
   <properties>
    <version>0.0.1-SNAPSHOT</version>
   </properties>

   <dependency>
       <groupId>org.protocol.ezmqx</groupId>
       <artifactId>ezmqx</artifactId>
       <version>${version}</version>
    </dependency>
  
    <dependency>
		<groupId>com.datamodel.aml</groupId>
		<artifactId>datamodel-aml-java</artifactId>
		<version>${version}</version>
    </dependency>
   ```
   - Include libaml.so and libjniaml.so in library path.  **Example:** </br>
`export LD_LIBRARY_PATH=~/protocol-ezmq-plus-java/dependencies/datamodel-aml-java`
