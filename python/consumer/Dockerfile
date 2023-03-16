FROM registry.access.redhat.com/ubi8/ubi-minimal:latest

USER root

RUN microdnf update \
    && microdnf install python3-devel shadow-utils \
    && microdnf clean all

# Add strimzi user with UID 1001
# The user is in the group 0 to have access to the mounted volumes and storage
RUN useradd -r -m -u 1001 -g 0 strimzi

ARG version=latest
ENV VERSION ${version}

ADD kafka_consumer.py /
ADD kafka_consumer_config.py /
ADD requirements.txt /

RUN pip3 install -r requirements.txt

USER 1001

CMD ["python3", "/kafka_consumer.py"]