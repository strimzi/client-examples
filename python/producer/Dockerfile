FROM registry.access.redhat.com/ubi8/python-38:latest

USER root

# Add strimzi user with UID 1002
# The user is in the group 0 to have access to the mounted volumes and storage
RUN useradd -r -m -u 1002 -g 0 strimzi

ARG version=latest
ENV VERSION ${version}

ADD kafka_producer.py $HOME
ADD kafka_producer_config.py $HOME
ADD requirements.txt $HOME

RUN pip3 install -r requirements.txt

USER 1002

CMD ["python3", "/kafka_producer.py"]
