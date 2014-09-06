package org.loosefx.eventsourcing.storage;

import org.loosefx.eventsourcing.DomainEvent;
import org.loosefx.eventsourcing.aggregate.EventProvider;

import java.util.List;
import java.util.UUID;

public interface DomainEventStorage extends SnapShotStorage {
    List<DomainEvent> GetAllEvents( UUID eventProviderId );
    List<DomainEvent> GetEventsSinceLastSnapShot( UUID eventProviderId );
    int GetEventCountSinceLastSnapShot( UUID eventProviderId );
    void Save( EventProvider eventProvider );
}
