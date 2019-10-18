RELEASE_VERSION ?= latest

SUBDIRS=java/kafka/hello-world-consumer java/kafka/hello-world-producer java/kafka/hello-world-streams
DOCKER_TARGETS=docker_build docker_push docker_tag

all: $(SUBDIRS)
build: $(SUBDIRS)
clean: $(SUBDIRS)
$(DOCKER_TARGETS): $(SUBDIRS)

$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: all $(SUBDIRS) $(DOCKER_TARGETS)
