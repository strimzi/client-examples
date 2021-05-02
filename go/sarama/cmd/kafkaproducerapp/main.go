package main

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/Shopify/sarama"
	"github.com/strimzi/client-examples/go/sarama/internal/kafkaproducer"
)

func main() {
	config := kafkaproducer.NewProducerConfig()
	log.Printf("Go producer starting with config=%+v\n", config)

	signals := make(chan os.Signal, 1)
	signal.Notify(signals, syscall.SIGINT, syscall.SIGKILL)

	producerConfig := sarama.NewConfig()
	producerConfig.Producer.RequiredAcks = sarama.RequiredAcks(config.ProducerAcks)
	producerConfig.Producer.Return.Successes = true
	producer, err := sarama.NewSyncProducer([]string{config.BootstrapServers}, producerConfig)
	if err != nil {
		log.Printf("Error creating the Sarama sync producer: %v", err)
		os.Exit(1)
	}

	end := make(chan int, 1)
	go func() {
		for i := int64(0); i < config.MessageCount; i++ {
			value := fmt.Sprintf("%s-%d", config.Message, i)
			msg := &sarama.ProducerMessage{
				Topic: config.Topic,
				Value: sarama.StringEncoder(value),
			}
			log.Printf("Sending message: value=%s\n", msg.Value)
			partition, offset, err := producer.SendMessage(msg)
			if err != nil {
				log.Printf("Erros sending message: %v\n", err)
			} else {
				log.Printf("Message sent: partition=%d, offset=%d\n", partition, offset)
			}

			// sleep before next message or avoid sleeping
			// and signal the end on the last message
			if i == config.MessageCount-1 {
				end <- 1
			} else {
				time.Sleep(time.Duration(config.Delay) * time.Millisecond)
			}
		}
	}()

	// waiting for the end of all messages sent or an OS signal
	select {
	case <-end:
		log.Printf("Finished to send %d messages\n", config.MessageCount)
	case sig := <-signals:
		log.Printf("Got signal: %v\n", sig)
	}

	err = producer.Close()
	if err != nil {
		log.Printf("Error closing the Sarama sync producer: %v", err)
		os.Exit(1)
	}
	log.Printf("Producer closed")
}
