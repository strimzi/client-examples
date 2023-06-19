import sys
import logging
from datetime import datetime
from confluent_kafka import Consumer
from kafka_consumer_config import from_env

logging.basicConfig(format='%(message)s',
                    level=logging.DEBUG, stream=sys.stdout)

log = logging.getLogger(__name__)
log.setLevel(logging.DEBUG)

if __name__ == '__main__':
    config = from_env()

    consumer = Consumer(
        config.properties
    )
    consumer.subscribe([config.topic])

    while True:
        message = consumer.poll(timeout=config.delay)
        if message is None:
            log.info("Waiting...")
        else:
            log.info(f'Consuming message @ {datetime.now()}', (message.value()))
