# Apache Kafka client examples

This repository contains examples of Apache Kafka clients written using the Apache Kafka Java APIs:
* Message Producer which periodically produces messages into a topic
* Streams application which reads messages from a topic, transforms them (reverses the message payload) and sends them to another topic
* Consumer which is consuming messages from a topic

All examples are assembled into Docker images which allows them to be deployed on Kubernetes or OpenShift.
This may serve as a basic usage example for the [Strimzi project](https://strimzi.io).

This repository contains `Deployments` for the clients as well as `KafkaTopic` and `KafkaUsers` for use by Strimzi operators.
Logging configuration can be found in the `log4j2.properties` file for the producer and consumer separately.

## Build

The pre-built images are available on our Docker Hub.
But if you want to do any modifications to the examples, you will need to build your own versions.

To build these examples you need some basic requirements.
Make sure you have `make`, `docker`, `JDK 1.8` and `mvn` installed. 
After cloning this repository to your folder Hello World example is fully ready to be build.
By one single command Java sources are compiled into JAR files, Docker images are created and pushed to repository.
By default the Docker organization to which images are pushed is the one defined by the `USER` environment variable which is assigned to the `DOCKER_ORG` one.
The organization can be changed exporting a different value for the `DOCKER_ORG` and it can also be the internal registry of an OpenShift running cluster.

The command for building the examples is:

```
make all
```

## Usage

Basic requirement to run this example is a Kubernetes cluster with Strimzi managed Apache Kafka cluster deployed.
Examples how to deploy Apache Kafka using Strimzi can be found on the [Strimzi website](https://strimzi.io/quickstarts/minikube/).

After successfully building the images (which will cause the images to be pushed to the specified Docker repository) you are ready to deploy the producer and consumer containers along with Kafka and Zookeper.

You can deploy the examples individually by applying [`hello-world-producer.yaml`](./java/kafka/hello-world-producer.yaml), [`hello-world-consumer.yaml`](./java/kafka/hello-world-consumer.yaml) and [`hello-world-streams.yaml`](./java/kafka/hello-world-streams.yaml) files.
This will create Kubernetes `Deployments` with the example image.
The second option is to apply `deployment.yaml` file.
This deploys the producer, consumer and streams and also creates the topics they are using.

If you built your own version of these examples, remember to update the `image` field with the path where the image was pushed during the build and it's available (i.e. `<my-docker-org>/hello-world-consumer:latest`).

When using [`deployment.yaml`](./java/kafka/deployment.yaml) file for deployment you can start observing the sending messages in producer container's log and the receiving of messages in consumer container's log.
It's also available as a [`deployment-ssl.yaml`](./java/kafka/deployment-ssl.yaml) which deploys the same producer and consumer applications but using a TLS encryption and [`deployment-ssl-auth.yaml`](./java/kafka/deployment-ssl-auth.yaml) which uses TLS client authentication and ACLs.

You can also use these example clients with OAuth authentication. See the example [`deployment-oauth.yaml`](./java/kafka/deployment-oauth.yaml) for more details.
To run the OAuth example, you will need to have your Kafka cluster configured with OAuth and change the configuration in [`deployment-oauth.yaml`](./java/kafka/deployment-oauth.yaml) to point to your OAuth server.

## Configuration

Although this Hello World is simple example it is fully configurable.
Below are listed and described environmental variables.

Producer  
* `BOOTSTRAP_SERVERS` - comma-separated host and port pairs that is a list of Kafka broker addresses. The form of pair is `host:port`, e.g. `my-cluster-kafka-bootstrap:9092` 
* `TOPIC` - the topic the producer will send to  
* `DELAY_MS` - the delay, in ms, between messages  
* `MESSAGE_COUNT` - the number of messages the producer should send  
* `CA_CRT` - the certificate of the CA which signed the brokers' TLS certificates, for adding to the client's trust store
* `USER_CRT` - the user's certificate
* `USER_KEY` - the user's private key
* `LOG_LEVEL` - logging level  
* `PRODUCER_ACKS` = acknowledgement level
* `ADDITIONAL_CONFIG` = additional configuration for a producer application. The form is `key=value` records separated by new line character

Consumer  
* `BOOTSTRAP_SERVERS` - comma-separated host and port pairs that is a list of Kafka broker addresses. The form of pair is `host:port`, e.g. `my-cluster-kafka-bootstrap:9092` 
* `TOPIC` - name of topic which consumer subscribes  
* `GROUP_ID` - specifies the consumer group id for the consumer
* `MESSAGE_COUNT` - the number of messages the consumer should receive
* `CA_CRT` - the certificate of the CA which signed the brokers' TLS certificates, for adding to the client's trust store
* `USER_CRT` - the user's certificate
* `USER_KEY` - the user's private key
* `LOG_LEVEL` - logging level  
* `ADDITIONAL_CONFIG` = additional configuration for a consumer application. The form is `key=value` records separated by new line character

Streams  
* `BOOTSTRAP_SERVERS` - comma-separated host and port pairs that is a list of Kafka broker addresses. The form of pair is `host:port`, e.g. `my-cluster-kafka-bootstrap:9092`
* `APPLICATION_ID` - The Kafka Streams application ID
* `SOURCE_TOPIC` - name of topic which will be used as the source of messages
* `TARGET_TOPIC` - name of topic where the transformed images are sent
* `COMMIT_INTERVAL_MS` - the interval for the Kafka Streams consumer part committing the offsets
* `CA_CRT` - the certificate of the CA which signed the brokers' TLS certificates, for adding to the client's trust store
* `USER_CRT` - the user's certificate
* `USER_KEY` - the user's private key
* `LOG_LEVEL` - logging level
* `ADDITIONAL_CONFIG` = additional configuration for a streams application. The form is `key=value` records separated by new line character

### Tracing

The examples support tracing using the [OpenTracing Apache Kafka Instrumentation](https://github.com/opentracing-contrib/java-kafka-client) and the [Jaeger project](https://www.jaegertracing.io/).
To enable tracing, configure the Jaeger Tracer using [environment variables](https://github.com/jaegertracing/jaeger-client-java/tree/master/jaeger-core#configuration-via-environment).

You can also use the provided example in [`deployment-tracing.yaml`](./java/kafka/deployment-tracing.yaml).
