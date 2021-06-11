#!/usr/bin/env bash

set -e

export DOCKER_ORG=${DOCKER_ORG:-strimzi-examples}
export DOCKER_REGISTRY=${DOCKER_REGISTRY:-quay.io}
export DOCKER_TAG=$COMMIT

echo "Build reason: ${BUILD_REASON}"
echo "Source branch: ${BRANCH}"

make build

if [ "$BUILD_REASON" == "PullRequest" ] ; then
  make docker_build
  echo "Building Pull Request - nothing to push"
elif [[ "$BRANCH" != "refs/tags/"* ]] && [ "$BRANCH" != "refs/heads/main" ]; then
    make docker_build
    echo "Not in main branch or in release tag - nothing to push"
else
    if [ "$BRANCH" == "refs/heads/main" ]; then
        export DOCKER_TAG="latest"
    else
        export DOCKER_TAG="${BRANCH#refs/tags/}"
    fi

    make docker_build

    echo "In main branch or in release tag - pushing images"
    docker login -u $DOCKER_USER -p $DOCKER_PASS $DOCKER_REGISTRY
    make docker_push
fi
