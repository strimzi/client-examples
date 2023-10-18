import sys
import logging
import time
from datetime import datetime
from confluent_kafka import Producer
from kafka_producer_config import from_env

logging.basicConfig(format='%(message)s',
                    level=logging.DEBUG, stream=sys.stdout)

log = logging.getLogger(__name__)
log.setLevel(logging.DEBUG)


if __name__ == '__main__':
    config = from_env()
    producer = Producer(
        config.properties,
    )

    log.info(f'Config Properties: \n{str(config.properties)}')

    while True:
        producer_message = config.message.encode('utf-8')
        producer.produce(config.topic, producer_message, )
        producer.flush()

        log.info(f'Producing message @ {datetime.now()} | Message = {str(producer_message)}')
        time.sleep(config.delay)

