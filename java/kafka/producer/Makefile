PROJECT_NAME=java-kafka-producer

all: java_install docker_build docker_push
build: java_install
clean: java_clean

include ../../../Makefile.docker

include ../../../Makefile.maven

.PHONY: build clean