package io.github.jesusblazquez.ledger.config;

import io.github.jesusblazquez.ledger.application.port.out.DomainEventPublisher;
import io.github.jesusblazquez.ledger.domain.event.DomainEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes domain events inside the application.
 *
 * <p>In a system split into services this adapter would write to Kafka instead; nothing above it
 * would change, which is the whole point of having the port.
 */
@Component
class SpringDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SpringDomainEventPublisher.class);

    private final ApplicationEventPublisher publisher;

    SpringDomainEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(List<DomainEvent> events) {
        events.forEach(event -> {
            log.debug("Domain event: {}", event);
            publisher.publishEvent(event);
        });
    }
}
