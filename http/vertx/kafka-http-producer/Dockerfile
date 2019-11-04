FROM openjdk:8-jre-alpine

ADD target/kafka-http-producer.jar /

CMD ["java", "-Dvertx.cacheDirBase=/tmp", "-Dvertx.disableDnsResolver=true", "-jar", "kafka-http-producer.jar"]