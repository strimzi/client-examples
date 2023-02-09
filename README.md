[![Build Status](https://dev.azure.com/cncf/strimzi/_apis/build/status/client-examples?branchName=main)](https://dev.azure.com/cncf/strimzi/_build/latest?definitionId=33&branchName=main)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Twitter Follow](https://img.shields.io/twitter/follow/strimziio.svg?style=social&label=Follow&style=for-the-badge)](https://twitter.com/strimziio)


# Client examples

This repository contains examples of [Apache Kafka®](https://kafka.apache.org) client applications written using the Apache Kafka Java APIs:
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

You can deploy the examples individually by applying [`java-kafka-producer.yaml`](./java/kafka/java-kafka-producer.yaml), [`java-kafka-consumer.yaml`](./java/kafka/java-kafka-consumer.yaml) and [`java-kafka-streams.yaml`](./java/kafka/java-kafka-streams.yaml) files.
This will create Kubernetes `Deployments` with the example image.
The second option is to apply `deployment.yaml` file.
This deploys the producer, consumer and streams and also creates the topics they are using.

If you built your own version of these examples, remember to update the `image` field with the path where the image was pushed during the build and it's available (i.e. `<my-docker-org>/java-kafka-consumer:latest`).

When using [`deployment.yaml`](./java/kafka/deployment.yaml) file for deployment you can start observing the sending messages in producer container's log and the receiving of messages in consumer container's log.
It's also available as a [`deployment-ssl.yaml`](./java/kafka/deployment-ssl.yaml) which deploys the same producer and consumer applications but using a TLS encryption and [`deployment-ssl-auth.yaml`](./java/kafka/deployment-ssl-auth.yaml) which uses TLS client authentication and ACLs.

You can also use these example clients with OAuth authentication. See the example [`deployment-oauth.yaml`](./java/kafka/deployment-oauth.yaml) for more details.
To run the OAuth example, you will need to have your Kafka cluster configured with OAuth and change the configuration in [`deployment-oauth.yaml`](./java/kafka/deployment-oauth.yaml) to point to your OAuth server.

## Configuration

Below are listed and described the available environment variables that can be used for configuration.

Producer  
* `STRIMZI_TOPIC` - the topic the producer will send to  
* `STRIMZI_DELAY_MS` - the delay, in ms, between messages  
* `STRIMZI_MESSAGE_COUNT` - the number of messages the producer should send
* `STRIMZI_MESSAGE` - the message the producer will send
* `STRIMZI_LOG_LEVEL` - logging level  
* `STRIMZI_HEADERS` - custom headers list separated by commas of `key1=value1, key2=value2`
* `STRIMZI_TRACING_SYSTEM` - if it's set to `jaeger` or `opentelemetry`, this will enable tracing. 

Consumer  
* `STRIMZI_TOPIC` - name of topic which consumer subscribes  
* `STRIMZI_MESSAGE_COUNT` - the number of messages the consumer should receive
* `STRIMZI_LOG_LEVEL` - logging level  
* `STRIMZI_TRACING_SYSTEM` - if it's set to `jaeger` or `opentelemetry`, this will enable tracing.

Streams  
* `STRIMZI_SOURCE_TOPIC` - name of topic which will be used as the source of messages
* `STRIMZI_TARGET_TOPIC` - name of topic where the transformed images are sent
* `STRIMZI_TRACING_SYSTEM` - if it's set to `jaeger` or `opentelemetry`, this will enable tracing.

Additionally, any Kafka Consumer API or Kafka Producer API configuration option can be passed as an environment variable.
It should be prefixed with `KAFKA_` and use `_` instead of `.`.
For example environment variable `KAFKA_BOOTSTRAP_SERVERS` will be used as the `bootstrap.servers` configuration option in the Kafka Consumer API.

### Tracing

The examples support tracing using the [OpenTracing Apache Kafka Instrumentation](https://github.com/opentracing-contrib/java-kafka-client), 
[OpenTelemetry Java Instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation) and the [Jaeger project](https://www.jaegertracing.io/).
To enable tracing, configure the Jaeger Tracer using [environment variables](https://github.com/jaegertracing/jaeger-client-java/tree/master/jaeger-core#configuration-via-environment).

To run Jaeger Tracing, you can also use the provided example in [`deployment-tracing-jaeger.yaml`](./java/kafka/deployment-tracing-jaeger.yaml).
To run Opentelemetry Tracing, you can also use the provided example in [`deployment-tracing-opentelemetry.yaml`](./java/kafka/deployment-tracing-opentelemetry.yaml).

Jaeger / OpenTracing tracing is supported only in consumers / producers because OpenTracing support for Kafka Streams API is not compatible with the latest Kafka versions.