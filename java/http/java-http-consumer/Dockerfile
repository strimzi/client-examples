FROM registry.access.redhat.com/ubi8/ubi-minimal:latest

USER root

RUN microdnf update \
    && microdnf --setopt=install_weak_deps=0 --setopt=tsflags=nodocs install java-17-openjdk-headless openssl shadow-utils \
    && microdnf clean all

# Set JAVA_HOME env var
ENV JAVA_HOME=/usr/lib/jvm/jre-17

# Add strimzi user with UID 1001
# The user is in the group 0 to have access to the mounted volumes and storage
RUN useradd -r -m -u 1001 -g 0 strimzi

COPY ./scripts/ /bin
COPY ./src/main/resources/log4j2.properties /bin/log4j2.properties

ADD target/java-http-consumer-1.0-SNAPSHOT.jar /

USER 1001

CMD ["/bin/run.sh", "/java-http-consumer-1.0-SNAPSHOT.jar"]