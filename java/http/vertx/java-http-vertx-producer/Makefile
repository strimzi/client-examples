PROJECT_NAME=java-http-vertx-producer
# override copy scripts folder because this projects is one more level down
COPY_SCRIPTS=cp ../../../../scripts/run.sh ./scripts/run.sh

all: java_build docker_build docker_push
build: java_build
clean: java_clean

java_install_root:
	echo "Installing root pom ..."
	mvn $(MVN_ARGS) install -f ../../../pom.xml -N

include ../../../../Makefile.docker

include ../../../../Makefile.maven

.PHONY: build clean