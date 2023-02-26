PROJECT_NAME=java-http-vertx-consumer
# override copy scripts folder because this projects is one more level down
COPY_SCRIPTS=cp ../../../../scripts/run.sh ./scripts/run.sh

all: java_build docker_build docker_push
build: java_build
clean: java_clean

include ../../../../Makefile.docker

include ../../../../Makefile.maven

.PHONY: build clean