#!/bin/bash
set +x

# Parameters:
# $1: Path to the new truststore
# $2: Truststore password
# $3: Public key to be imported
# $4: Alias of the certificate
function create_truststore {
   keytool -keystore $1 -storepass $2 -noprompt -alias $4 -import -file $3 -storetype PKCS12
}

if [ -z "$JAVA_OPTS" ]; then
    export JAVA_OPTS="${JAVA_OPTS} -Dlog4j2.configurationFile=file:bin/log4j2.properties"
fi

if [ "$OAUTH_CRT" ];
then
    echo "Preparing OAuth truststore"
    export OAUTH_SSL_TRUSTSTORE_PASSWORD=$(< /dev/urandom tr -dc _A-Z-a-z-0-9 | head -c32)
    echo "$OAUTH_CRT" > /tmp/oauth.crt
    create_truststore /tmp/oauth-truststore.p12 $OAUTH_SSL_TRUSTSTORE_PASSWORD /tmp/oauth.crt ca
    export OAUTH_SSL_TRUSTSTORE_LOCATION=/tmp/oauth-truststore.p12
fi

# Make sure that we use /dev/urandom
JAVA_OPTS="${JAVA_OPTS} -Dvertx.cacheDirBase=/tmp -Djava.security.egd=file:/dev/./urandom"

# Enable GC logging for memory tracking
JAVA_OPTS="${JAVA_OPTS} -Xlog:gc*:stdout:time -XX:NativeMemoryTracking=summary"

exec java $JAVA_OPTS -jar $JAR "$@"
