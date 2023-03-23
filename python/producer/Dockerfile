FROM registry.access.redhat.com/ubi8/python-39:latest

USER root

ARG version=latest
ENV VERSION ${version}

ADD kafka_producer.py $HOME
ADD kafka_producer_config.py $HOME
ADD requirements.txt $HOME

RUN pip3 install -r requirements.txt

CMD ["python3", "kafka_producer.py"]
