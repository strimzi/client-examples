import os

KAFKA_PREFIX = "KAFKA_"
STRIMZI_PREFIX = "STRIMZI"

# Strimzi environment variables declaration
TOPIC = "STRIMZI_TOPIC"
MESSAGE_COUNT = "STRIMZI_MESSAGE_COUNT"

# default values for Strimzi environment variables
DEFAULT_TOPIC = "my-topic"
DEFAULT_MESSAGE_COUNT = 10

# default values for Kafka environmental variables in a dictionary
DEFAULT_PROPERTIES = {
    'bootstrap.servers': 'localhost:9092',
    'group.id': 'my-kafka-consumer',
    'auto.offset.reset': 'latest',
}


def convertEnvVarToPropertyKey(e):
    return e[e.index('_') + 1:].lower()


class KafkaConsumerConfig:
    def __init__(self, topic, message_count, properties):
        self.topic = topic
        self.message_count = message_count
        self.properties = properties


def from_env():
    topic = os.getenv(TOPIC, DEFAULT_TOPIC)
    message_count = os.getenv(MESSAGE_COUNT, DEFAULT_MESSAGE_COUNT)
    for k, v in os.environ.items():
        if k.startswith('KAFKA_'):
            DEFAULT_PROPERTIES[convertEnvVarToPropertyKey(k)] = v
    return KafkaConsumerConfig(topic, message_count, DEFAULT_PROPERTIES)
