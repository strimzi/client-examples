import os

KAFKA_PREFIX = "KAFKA_"
STRIMZI_PREFIX = "STRIMZI"

# Strimzi environment variables declaration
TOPIC = "STRIMZI_TOPIC"
MESSAGE = "STRIMZI_MESSAGE"

# default values for Strimzi environment variables
DEFAULT_TOPIC = "my-topic"
DEFAULT_MESSAGE = 'Hello to the World'


# default values for environmental variables in dictionary
DEFAULT_PROPERTIES = {
    'bootstrap.servers': 'localhost:9092'
}


# convert the kafka (strimzi) prefix
def convertEnvVarToPropertyKey(e):
    return e[e.index('_') + 1:].lower()


class KafkaProducerConfig:
    def __init__(self, topic, message, properties):
        self.topic = topic
        self.message = message
        self.properties = properties


def from_env():
    topic = os.getenv(TOPIC, DEFAULT_TOPIC)
    message = os.getenv(MESSAGE, DEFAULT_MESSAGE)
    properties = DEFAULT_PROPERTIES

    for k, v in os.environ.items():
        if k.startswith('KAFKA_'):
            DEFAULT_PROPERTIES[convertEnvVarToPropertyKey(k)] = v
    return KafkaProducerConfig(topic, message,properties)
