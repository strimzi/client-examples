import os

# Used in fromEnv() to find evnVar starting with KAFKA_
KAFKA_PREFIX = 'KAFKA_'

# Strimzi environment variables declaration
TOPIC = 'STRIMZI_TOPIC'
MESSAGE = 'STRIMZI_MESSAGE'
DELAY_MS = 'STRIMZI_DELAY_MS'
LOG_LEVEL = 'STRIMZI_LOG_LEVEL'

# default values for Strimzi environment variables
DEFAULT_TOPIC = 'my-topic'
DEFAULT_MESSAGE = 'Hello World'
DEFAULT_DELAY_MS = '1000'
DEFAULT_LOG_LEVEL = '6'

# default values for environmental variables in dictionary
DEFAULT_PROPERTIES = {
    'bootstrap.servers': 'localhost:9092'
}


# Converts env var names to variable names that are compliant with Kafka properties file format
def convertEnvVarToPropertyKey(e):
    if 'LOG' not in e and 'log' not in e:
        return e[e.index('_') + 1:].lower().replace('_', '.')
    else:
        return e


class KafkaProducerConfig:
    def __init__(self, topic, message, delay, properties):
        self.topic = topic
        self.message = message
        self.delay = delay
        self.properties = properties


def from_env():
    topic = os.getenv(TOPIC, DEFAULT_TOPIC)
    message = os.getenv(MESSAGE, DEFAULT_MESSAGE)
    delay = (int(os.getenv(DELAY_MS, DEFAULT_DELAY_MS)) / 1000)
    properties = DEFAULT_PROPERTIES

    for k, v in os.environ.items():
        if k.startswith(KAFKA_PREFIX):
            DEFAULT_PROPERTIES[convertEnvVarToPropertyKey(k)] = v
    return KafkaProducerConfig(topic, message, delay, properties)


