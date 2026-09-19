package io.github.jesusblazquez.ledger.application.fake;

import io.github.jesusblazquez.ledger.application.port.out.DomainEventPublisher;
import io.github.jesusblazquez.ledger.domain.event.DomainEvent;
import java.util.ArrayList;
import java.util.List;

public class RecordingEventPublisher implements DomainEventPublisher {

    private final List<DomainEvent> published = new ArrayList<>();

    @Override
    public void publish(List<DomainEvent> events) {
        published.addAll(events);
    }

    public List<DomainEvent> published() {
        return List.copyOf(published);
    }
}
