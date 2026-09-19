package io.github.jesusblazquez.ledger.application.port.out;

import io.github.jesusblazquez.ledger.domain.event.DomainEvent;
import java.util.List;

/**
 * Publishes what happened so other parts of the system can react.
 *
 * <p>Today the implementation is in-process. The point of the port is that replacing it with Kafka
 * would not change a single line of the application or the domain.
 */
public interface DomainEventPublisher {

    void publish(List<DomainEvent> events);
}
