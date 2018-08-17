# Description of client-examples

This repository contains example Kafka clients. 
Both producer and consumer clients are assembled into Docker images which allows them to be deployed on Kubernetes or OpenShift.
This may serve as a basic usage example for the [Strimzi][strimziGH] project.

This repository contains a `deployment.yaml` file with `Deployments` for the producer and consumer as well as `KafkaTopic` and `KafkaUsers` for use by Strimzi operator.
Logging configuration can be found in the `log4j2.properties` file for the producer and consumer separately.

## Build

To build this example you need some basic requirements.
Make sure you have `make`, `docker`, `JDK 1.8` and `mvn` installed. 
After cloning this repository to your folder Hello World example is fully ready to be build with `maven`.
By one single command Java sources are compiled into JAR files, Docker images are created and pushed to repository.
By default the Docker organization to which images are pushed is the one defined by the `USER` environment variable which is assigned to the `DOCKER_ORG` one.
The organization can be changed exporting a different value for the `DOCKER_ORG` and it can also be the internal registry of an OpenShift running cluster.

The command for making the examples is:

    make all

Note: Be sure `docker` and `oc` cluster where images should be pushed are running.

## Usage

Basic requirement to run this example is running Kubernetes or OpenShift cluster with deployed Kafka and Zookeeper containers.
Examples how to deploy basic configuration can be found in the [Strimzi documentation][strimziDoc].

After successfully building the images (which will cause the images to be pushed to the specified Docker repository) you are ready to deploy the producer and consumer containers along with Kafka and Zookeper.

This can be done in two ways:
* By applying `hello-world-producer.yaml` and `hello-world-consumer.yaml` files.
This deploys producer image ready to publish to the topic which is specified in the `hello-world-producer.yaml` file and and consumer image ready to subscribe to the topic which is specified in the `hello-world-consumer.yaml`.
* The second option is to apply `deployment.yaml` file. This deploys the producer and consumer ans also creates the topic and example is in ready-to-observe state.

Before deploying the producer and the consumer, remember to update the `image` field with the path where the image was pushed during the build and it's available (i.e. `<my-docker-org>/hello-world-consumer:latest`)

When using `deployment.yaml` file for deployment you can start observing the sending messages in producer container's log and the receiving of messages in consumer container's log.
The producer sends every `DELAY_MS` ms a message. 
This message is received and printed by the consumer.
Consumer is running until `MESSAGE_COUNT` messages are received.

It's also available a `deployment-ssl.yaml` file which deploys the same producer and consumer applications but using a TLS connection to the cluster.

## Configuration

Although this Hello World is simple example it is fully configurable.
Below are listed and described environmental variables.

Producer  
* `BOOTSTRAP_SERVERS` - comma-separated host and port pairs that is a list of Kafka broker addresses. The form of pair is `host:port`, e.g. `my-cluster-kafka-bootstrap:9092` 
* `TOPIC` - the topic the producer will send to  
* `DELAY_MS` - the delay, in ms, between messages  
* `MESSAGE_COUNT` - the number of messages the producer should send  
* `TRUSTSTORE_PASSWORD` - password to unlock the truststore file  
* `TRUSTSTORE_PATH` - location of the truststore file containing the collection of CA certificates for connecting via TLS to the brokers  
* `KEYSTORE_PASSWORD` - password to unlock the keystore file   
* `KEYSTORE_PATH` - location of the keystore file containing certificate and private key for TLS client authentication  
* `LOG_LEVEL` - logging level  

Consumer  
* `BOOTSTRAP_SERVERS` - comma-separated host and port pairs that is a list of Kafka broker addresses. The form of pair is `host:port`, e.g. `my-cluster-kafka-bootstrap:9092` 
* `TOPIC` - name of topic which consumer subscribes  
* `GROUP_ID` - specifies the consumer group id for the consumer
* `MESSAGE_COUNT` - the number of messages the consumer should receive
* `TRUSTSTORE_PASSWORD` - password to unlock the truststore file
* `TRUSTSTORE_PATH` - location of the truststore file containing the collection of CA certificates for connecting via TLS to the brokers  
* `KEYSTORE_PASSWORD` - password to unlock the keystore file
* `KEYSTORE_PATH` - location of the keystore file containing certificate and private key for TLS client authentication  
* `LOG_LEVEL` - logging level  

Logging configuration is done by setting up `EXAMPLE_LOG_LEVEL` environmental variable.
Value of this variable is substituted into `log4j2.properties` file under `client/src/main/resources/log4j2.properties`.
In this file you can set optional appender and loggers.

[strimziDoc]: http://strimzi.io/docs/master/
[strimziGH]: https://github.com/strimzi/strimzi-kafka-operator