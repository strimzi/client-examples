import sys
import logging
import time
from datetime import datetime
from confluent_kafka import Producer
from kafka_producer_config import from_env

logging.basicConfig(format='%(message)s',
                    level=logging.INFO, stream=sys.stdout)

log = logging.getLogger('librdkafka')
log.setLevel(logging.DEBUG)

p = Producer({'bootstrap.servers': 'mybroker1,mybroker2'})
def delivery_report(err, msg):
    """ Called once for each message produced to indicate delivery result.
        Triggered by poll() or flush(). """
    if err is not None:
        print('Message delivery failed: {}'.format(err))
    else:
        print('Message delivered to {} [{}]'.format(msg.topic(), msg.partition()))

for data in some_data_source:
    # Trigger any available delivery report callbacks from previous produce() calls
    p.poll(0)

    # Asynchronously produce a message. The delivery report callback will
    # be triggered from the call to poll() above, or flush() below, when the
    # message has been successfully delivered or failed permanently.
    p.produce('mytopic', data.encode('utf-8'), callback=delivery_report)

# Wait for any outstanding messages to be delivered and delivery report
# callbacks to be triggered.
p.flush()

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

