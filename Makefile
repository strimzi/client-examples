include ./Makefile.os

RELEASE_VERSION ?= latest

SUBDIRS=java/common java/kafka/consumer java/kafka/producer java/kafka/streams java/http/java-http-consumer java/http/java-http-producer java/http/vertx/java-http-vertx-consumer java/http/vertx/java-http-vertx-producer
DOCKERDIRS=java/kafka/consumer java/kafka/producer java/kafka/streams java/http/java-http-consumer java/http/java-http-producer java/http/vertx/java-http-vertx-consumer java/http/vertx/java-http-vertx-producer
DOCKER_TARGETS=docker_build docker_push docker_tag docker_load docker_save docker_amend_manifest docker_push_manifest docker_sign_manifest docker_delete_manifest docker_delete_archive
JAVA_TARGETS=java_build java_install java_clean

all: $(SUBDIRS)
build: $(SUBDIRS)
clean: $(SUBDIRS)

$(JAVA_TARGETS): $(SUBDIRS)
$(DOCKER_TARGETS): $(DOCKERDIRS)

spotbugs: $(SUBDIRS)

$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: all $(SUBDIRS) $(DOCKER_TARGETS)
