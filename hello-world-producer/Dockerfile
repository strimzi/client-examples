FROM strimzi/java-base:latest

ARG version=latest
ENV VERSION ${version}

COPY ./scripts/ /bin
COPY ./src/main/resources/log4j2.properties /bin/log4j2.properties

ADD target/hello-world-producer-1.0-SNAPSHOT.jar /

CMD ["/bin/run.sh", "/hello-world-producer-1.0-SNAPSHOT.jar"]