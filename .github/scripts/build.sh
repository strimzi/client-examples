#!/usr/bin/env bash

set -e

export DOCKER_ORG=${DOCKER_ORG:-tealc}
export DOCKER_REGISTRY=${DOCKER_REGISTRY:-quay.io}
export DOCKER_TAG=$COMMIT

echo "GITHUB_HEAD_REF: ${GITHUB_HEAD_REF}"
echo "GITHUB_REF: ${GITHUB_REF}"

make build

if [ -z "$GITHUB_HEAD_REF" ] ; then
  make docker_build
  echo "Building Pull Request - nothing to push"
elif [[ "$GITHUB_REF" != "refs/tags/"* ]] && [ "$GITHUB_REF" != "refs/heads/main" ]; then
    make docker_build
    echo "Not in main branch or in release tag - nothing to push"
else
    if [ "$GITHUB_REF" == "refs/heads/main" ]; then
        export DOCKER_TAG="latest"
    else
        export DOCKER_TAG="${$GITHUB_REF#refs/tags/}"
    fi

    make docker_build

    echo "In main branch or in release tag - pushing images"
    docker login -u $DOCKER_USER -p $DOCKER_PASS $DOCKER_REGISTRY
    make docker_push
fi
