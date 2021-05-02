package main

import (
	"context"
	"log"
	"os"
	"os/signal"
	"syscall"

	"github.com/Shopify/sarama"
	"github.com/strimzi/client-examples/go/sarama/internal/kafkaconsumer"
)

func main() {
	config := kafkaconsumer.NewConsumerConfig()
	log.Printf("Go consumer starting with config=%+v\n", config)

	signals := make(chan os.Signal, 1)
	signal.Notify(signals, syscall.SIGINT, syscall.SIGKILL)

	consumerGroup, err := sarama.NewConsumerGroup([]string{config.BootstrapServers}, config.GroupID, nil)
	if err != nil {
		log.Printf("Error creating the Sarama consumer: %v", err)
		os.Exit(1)
	}

	cgh := &consumerGroupHandler{
		toReceive: config.MessageCount,
		end:       make(chan int, 1),
	}
	ctx := context.Background()
	go func() {
		for {
			// this method calls the methods handler on each stage: setup, consume and cleanup
			consumerGroup.Consume(ctx, []string{config.Topic}, cgh)
		}
	}()

	// waiting for the end of all messages received or an OS signal
	select {
	case <-cgh.end:
		log.Printf("Finished to receive %d messages\n", config.MessageCount)
	case sig := <-signals:
		log.Printf("Got signal: %v\n", sig)
	}

	err = consumerGroup.Close()
	if err != nil {
		log.Printf("Error closing the Sarama consumer: %v", err)
		os.Exit(1)
	}
	log.Printf("Consumer closed")
}

// struct defining the handler for the consuming Sarama method
type consumerGroupHandler struct {
	toReceive int64
	end       chan int
}

func (cgh *consumerGroupHandler) Setup(sarama.ConsumerGroupSession) error {
	log.Printf("Consumer group handler setup\n")
	return nil
}

func (cgh *consumerGroupHandler) Cleanup(sarama.ConsumerGroupSession) error {
	log.Printf("Consumer group handler cleanup\n")
	return nil
}

func (cgh *consumerGroupHandler) ConsumeClaim(session sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	for message := range claim.Messages() {
		log.Printf("Message received: value=%s, partition=%d, offset=%d", string(message.Value), message.Partition, message.Offset)
		session.MarkMessage(message, "")
		if cgh.toReceive--; cgh.toReceive == 0 {
			cgh.end <- 1
			break
		}
	}
	return nil
}
