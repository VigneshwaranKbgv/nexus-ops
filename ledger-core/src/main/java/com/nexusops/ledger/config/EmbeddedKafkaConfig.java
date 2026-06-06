package com.nexusops.ledger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

@Configuration
public class EmbeddedKafkaConfig {

    @Bean
    public EmbeddedKafkaBroker embeddedKafkaBroker() {
        // In Spring Boot 2.7.x / Spring Kafka 2.8.x, we instantiate EmbeddedKafkaBroker directly.
        // It automatically handles the Zookeeper and Kafka broker lifecycles in-memory.
        EmbeddedKafkaBroker broker = new EmbeddedKafkaBroker(1, true, "marketplace-events", "remediation-commands");
        broker.kafkaPorts(9092);
        return broker;
    }
}
