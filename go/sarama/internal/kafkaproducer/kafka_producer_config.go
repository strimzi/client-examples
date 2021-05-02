package kafkaproducer

import (
	"os"
	"strconv"
)

// environment variables declaration
const (
	BootstrapServerEnvVar = "BOOTSTRAP_SERVERS"
	TopicEnvVar           = "TOPIC"
	DelayEnvVar           = "DELAY_MS"
	MessageEnvVar         = "MESSAGE"
	MessageCountEnvVar    = "MESSAGE_COUNT"
	ProducerAcksEnvVar    = "PRODUCER_ACKS"
)

// default values for environment variables
const (
	BootstrapServersDefault = "localhost:9092"
	TopicDefault            = "my-topic"
	DelayDefault            = 1000
	MessageDefault          = "Hello from Go Kafka Sarama"
	MessageCountDefault     = 10
	ProducerAcksDefault     = int16(1)
)

// ProducerConfig defines the producer configuration
type ProducerConfig struct {
	BootstrapServers string
	Topic            string
	Delay            int
	Message          string
	MessageCount     int64
	ProducerAcks     int16
}

func NewProducerConfig() *ProducerConfig {
	config := ProducerConfig{
		BootstrapServers: lookupStringEnv(BootstrapServerEnvVar, BootstrapServersDefault),
		Topic:            lookupStringEnv(TopicEnvVar, TopicDefault),
		Delay:            lookupIntEnv(DelayEnvVar, DelayDefault),
		Message:          lookupStringEnv(MessageEnvVar, MessageDefault),
		MessageCount:     lookupInt64Env(MessageCountEnvVar, MessageCountDefault),
		ProducerAcks:     lookupInt16Env(ProducerAcksEnvVar, ProducerAcksDefault),
	}
	return &config
}

func lookupStringEnv(envVar string, defaultValue string) string {
	envVarValue, ok := os.LookupEnv(envVar)
	if !ok {
		return defaultValue
	}
	return envVarValue
}

func lookupIntEnv(envVar string, defaultValue int) int {
	envVarValue, ok := os.LookupEnv(envVar)
	if !ok {
		return defaultValue
	}
	intVal, _ := strconv.Atoi(envVarValue)
	return intVal
}

func lookupInt64Env(envVar string, defaultValue int64) int64 {
	envVarValue, ok := os.LookupEnv(envVar)
	if !ok {
		return defaultValue
	}
	int64Val, _ := strconv.ParseInt(envVarValue, 10, 64)
	return int64Val
}

func lookupInt16Env(envVar string, defaultValue int16) int16 {
	envVarValue, ok := os.LookupEnv(envVar)
	if !ok {
		return defaultValue
	}
	int16Val, _ := strconv.ParseInt(envVarValue, 10, 16)
	return int16(int16Val)
}
