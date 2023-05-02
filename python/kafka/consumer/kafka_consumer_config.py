import os

# Used in fromEnv() to find evnVar starting with KAFKA_
KAFKA_PREFIX = 'KAFKA_'

# Strimzi environment variables declaration
TOPIC = 'STRIMZI_TOPIC'
DELAY_MS = 'STRIMZI_DELAY_MS'
# default values for Strimzi environment variables
DEFAULT_TOPIC = 'my-topic'
DEFAULT_DELAY_MS = '2000'
# default values for Kafka environmental variables in a dictionary
DEFAULT_PROPERTIES = {
    'bootstrap.servers': 'localhost:9092',
    'group.id': 'my-kafka-consumer',
    'client.id': 'my-consumer'
}


# Converts env var names to variable names that are compliant with Kafka properties file format
def convertEnvVarToPropertyKey(e):
    return e[e.index('_') + 1:].lower().replace('_', '.')


class KafkaConsumerConfig:
    def __init__(self, topic, delay, properties):
        self.topic = topic
        self.delay = delay
        self.properties = properties


def from_env():
    topic = os.getenv(TOPIC, DEFAULT_TOPIC)
    delay = (int(os.getenv(DELAY_MS, DEFAULT_DELAY_MS)) / 1000)
    properties = DEFAULT_PROPERTIES
    for k, v in os.environ.items():
        if k.startswith(KAFKA_PREFIX):
            DEFAULT_PROPERTIES[convertEnvVarToPropertyKey(k)] = v
    return KafkaConsumerConfig(topic, delay, properties)
