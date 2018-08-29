#!/usr/bin/env bash

set -e

export PULL_REQUEST=${PULL_REQUEST:-true}
export BRANCH=${BRANCH:-master}
export TAG=${TAG:-latest}
export DOCKER_ORG=${DOCKER_ORG:-strimzici}
export DOCKER_REGISTRY=${DOCKER_REGISTRY:-docker.io}
export DOCKER_TAG=$COMMIT

make build docker_build

if [ "$PULL_REQUEST" != "false" ] ; then
    echo "Building PR: Nothing to push"
else
  docker login -u $DOCKER_USER -p $DOCKER_PASS
  if [ "$TAG" = "latest" ] && [ "$BRANCH" != "master" ]; then
    export DOCKER_ORG=strimzici
    export DOCKER_TAG=$TAG
  else
    export DOCKER_ORG=strimzi
    export DOCKER_TAG=$TAG
  fi
  make docker_push
fi
