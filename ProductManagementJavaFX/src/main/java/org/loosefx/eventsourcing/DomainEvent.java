package org.loosefx.eventsourcing;

import java.util.UUID;

public interface DomainEvent {
    UUID getEventId();

    UUID getAggregateId();
    void setAggregateId( UUID aggregateId );

    AggregateVersion getVersion();
    void setVersion( AggregateVersion version );
}
