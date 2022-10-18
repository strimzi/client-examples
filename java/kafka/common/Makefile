PROJECT_NAME=java-kafka-common

all: java_install
build: java_install
clean: java_clean

docker_build:

docker_tag:

docker_push:

java_install:
	echo "Installing root pom ..."
	mvn install -f ../../../pom.xml

include ../../../Makefile.maven

.PHONY: all build clean
