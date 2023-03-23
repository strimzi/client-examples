# Python Client 

This repository contains examples of [Apache Kafka®](https://kafka.apache.org) client applications written using the Confluent-Kafka Python client library.
* Message Producer which periodically produces messages into a topic
* Consumer which is consuming messages from a topic

All examples are assembled into Docker images which allows them to be deployed on Kubernetes or OpenShift.
This may serve as a basic usage example for the [Strimzi project](https://strimzi.io).

## Build
The pre-built images are available on our Docker Hub.
But if you want to do any modifications to the examples, you will need to build your own versions.

To build these examples you need some basic requirements.
Make sure you have `make` and `docker` installed. 
After cloning this repository to your folder Hello World example is fully ready to be build.
By one single command, Docker images are created and pushed to repository.
By default, the Docker organization to which images are pushed is the one defined by the `USER` environment variable which is assigned to the `DOCKER_ORG` one.
The organization can be changed exporting a different value for the `DOCKER_ORG` and it can also be the internal registry of an OpenShift running cluster.

The command for building the examples is:

```
make all
```

## Usage

Basic requirement to run this example is a Kubernetes cluster with Strimzi managed Apache Kafka cluster deployed.
Examples how to deploy Apache Kafka using Strimzi can be found on the [Strimzi website](https://strimzi.io/quickstarts/minikube/).

After successfully building the images (which will cause the images to be pushed to the specified Docker repository) you are ready to deploy the producer and consumer containers along with Kafka and Zookeper.

You can deploy the examples individually by applying [`python-kafka-producer.yaml`](./python/kafka/python-kafka-producer.yaml) and [`python-kafka-consumer.yaml`](./python/kafka/python-kafka-consumer.yaml) files.
This will create Kubernetes `Deployments` with the example image.
The second option is to apply `deployment.yaml` file.
This deploys the producer and consumer and also creates the topics they are using.

If you built your own version of these examples, remember to update the `image` field with the path where the image was pushed during the build and it's available (i.e. `<my-docker-org>/python-kafka-consumer:latest`).

When using [`python-kafka-deployment.yaml`](./python/python-kafka-deployment.yaml) file for deployment you can start observing the sending messages in producer container's log and the receiving of messages in consumer container's log.

## Configuration

Below are listed and described the available environment variables that can be used for configuration.

Producer  
* `STRIMZI_TOPIC` - the topic the producer will send to
* `STRIMZI_MESSAGE` - the message the producer will send

Consumer  
* `STRIMZI_TOPIC` - name of topic which consumer subscribes  


Additionally, any Kafka Consumer API or Kafka Producer API configuration option can be passed as an environment variable.
It should be prefixed with `KAFKA_` and use `_` instead of `.`.
For example environment variable `KAFKA_BOOTSTRAP_SERVERS` will be used as the `bootstrap.servers` configuration option in the Kafka Consumer API.
