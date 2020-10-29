FROM centos:7

RUN yum -y install java-1.8.0-openjdk-headless openssl && yum -y clean all

# Set JAVA_HOME env var
ENV JAVA_HOME /usr/lib/jvm/java

ARG version=latest
ENV VERSION ${version}

COPY ./scripts/ /bin
COPY ./src/main/resources/log4j2.properties /bin/log4j2.properties

ADD target/kafka-native-producer-1.0-SNAPSHOT.jar /

CMD ["/bin/run.sh", "/kafka-native-producer-1.0-SNAPSHOT.jar"]