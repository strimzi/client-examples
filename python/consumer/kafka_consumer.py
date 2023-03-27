from datetime import datetime
from confluent_kafka import Consumer
from kafka_consumer_config import from_env

if __name__ == '__main__':
    config = from_env()

    consumer = Consumer(
        config.properties
    )
    consumer.subscribe([config.topic])

    while True:
        message = consumer.poll(timeout=config.delay)
        if message is None:
            print("Waiting...")
        else:
            print(f'Consuming message @ {datetime.now()}', (message.value()))
