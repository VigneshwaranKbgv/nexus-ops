package com.nexusops.ledger.service;

import com.nexusops.ledger.event.MarketplaceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishEvent(String topic, MarketplaceEvent event) {
        log.info("Streaming event {} to topic {}: {}", event.getEventType(), topic, event.getEventId());
        kafkaTemplate.send(topic, event.getEventId(), event);
    }
}
