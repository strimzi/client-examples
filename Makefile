RELEASE_VERSION ?= latest

SUBDIRS=java/common java/kafka/consumer java/kafka/producer java/kafka/streams http/vertx/java-http-vertx-consumer http/vertx/java-http-vertx-producer
DOCKER_TARGETS=docker_build docker_push docker_tag

all: $(SUBDIRS)
build: $(SUBDIRS)
clean: $(SUBDIRS)
$(DOCKER_TARGETS): $(SUBDIRS)

$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: all $(SUBDIRS) $(DOCKER_TARGETS)
