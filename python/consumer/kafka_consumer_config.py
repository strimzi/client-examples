import os

KAFKA_PREFIX = 'KAFKA_'
STRIMZI_PREFIX = 'STRIMZI'

# Strimzi environment variables declaration
TOPIC = 'STRIMZI_TOPIC'

# default values for Strimzi environment variables
DEFAULT_TOPIC = 'my-topic'

# default values for Kafka environmental variables in a dictionary
DEFAULT_PROPERTIES = {
    'bootstrap.servers': 'localhost:9092',
    'group.id': 'my-kafka-consumer',
    'client.id': 'my-kafka-client'
}


def convertEnvVarToPropertyKey(e):
    return e[e.index('_') + 1:].lower().replace('_', '.')


class KafkaConsumerConfig:
    def __init__(self, topic, properties):
        self.topic = topic
        self.properties = properties


def from_env():
    topic = os.getenv(TOPIC, DEFAULT_TOPIC)
    for k, v in os.environ.items():
        if k.startswith(KAFKA_PREFIX):
            DEFAULT_PROPERTIES[convertEnvVarToPropertyKey(k)] = v
    return KafkaConsumerConfig(topic, DEFAULT_PROPERTIES)
