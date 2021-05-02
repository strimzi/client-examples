package kafkaconsumer

import (
	"os"
	"strconv"
)

// environment variables declaration
const (
	BootstrapServerEnvVar = "BOOTSTRAP_SERVERS"
	TopicEnvVar           = "TOPIC"
	GroupIDEnvVar         = "GROUP_ID"
	MessageCountEnvVar    = "MESSAGE_COUNT"
)

// default values for environment variables
const (
	BootstrapServersDefault = "localhost:9092"
	TopicDefault            = "my-topic"
	GroupIDDefault          = "my-group"
	MessageCountDefault     = 10
)

// ConsumerConfig defines the producer configuration
type ConsumerConfig struct {
	BootstrapServers string
	Topic            string
	GroupID          string
	MessageCount     int64
}

func NewConsumerConfig() *ConsumerConfig {
	config := ConsumerConfig{
		BootstrapServers: lookupStringEnv(BootstrapServerEnvVar, BootstrapServersDefault),
		Topic:            lookupStringEnv(TopicEnvVar, TopicDefault),
		GroupID:          lookupStringEnv(GroupIDEnvVar, GroupIDDefault),
		MessageCount:     lookupInt64Env(MessageCountEnvVar, MessageCountDefault),
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

func lookupInt64Env(envVar string, defaultValue int64) int64 {
	envVarValue, ok := os.LookupEnv(envVar)
	if !ok {
		return defaultValue
	}
	int64Val, _ := strconv.ParseInt(envVarValue, 10, 64)
	return int64Val
}
