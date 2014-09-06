package org.loosefx.events;

import org.loosefx.eventsourcing.AggregateVersion;
import org.loosefx.eventsourcing.DomainEvent;

import java.util.UUID;

public abstract class AbstractDomainEvent implements DomainEvent {
    private UUID aggregateId;
    private final UUID eventId;
    private AggregateVersion aggregateVersion;

    protected AbstractDomainEvent( final UUID eventId ) {
        this.eventId = eventId;
    }

    protected AbstractDomainEvent() {
        this( UUID.randomUUID() );
    }

    protected AbstractDomainEvent( final UUID aggregateId, final UUID eventId ) {
        this( eventId );
        this.aggregateId = aggregateId;
    }

    @Override
    public UUID getEventId() { return eventId; }

    @Override
    public UUID getAggregateId() { return aggregateId; }
    @Override
    public void setAggregateId( final UUID aggregateId ) { this.aggregateId = aggregateId; }

    @Override
    public AggregateVersion getVersion() { return aggregateVersion; }
    @Override
    public void setVersion( final AggregateVersion version ) { this.aggregateVersion = version; }
}
