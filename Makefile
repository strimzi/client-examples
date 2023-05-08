RELEASE_VERSION ?= latest

SUBDIRS=java/common java/kafka/consumer java/kafka/producer java/kafka/streams java/http/java-http-consumer java/http/java-http-producer java/http/vertx/java-http-vertx-consumer java/http/vertx/java-http-vertx-producer

DOCKER_TARGETS=docker_build docker_push docker_tag

all: $(SUBDIRS)
build: $(SUBDIRS)
clean: $(SUBDIRS)
$(DOCKER_TARGETS): $(SUBDIRS)

$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: all $(SUBDIRS) $(DOCKER_TARGETS)
