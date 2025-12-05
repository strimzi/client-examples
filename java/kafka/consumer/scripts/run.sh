#!/bin/bash
set +x

if [ -z "$JAVA_OPTS" ]; then
    export JAVA_OPTS="${JAVA_OPTS} -Dlog4j2.configurationFile=file:bin/log4j2.properties"
fi

# Make sure that we use /dev/urandom
JAVA_OPTS="${JAVA_OPTS} -Dvertx.cacheDirBase=/tmp/vertx-cache -Djava.security.egd=file:/dev/./urandom"

exec java $JAVA_OPTS -jar $JAR "$@"
