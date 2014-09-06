package org.loosefx.eventsourcing;

import java.util.UUID;

public interface EntityEventProvider {
    void clear();
    void loadFromHistory( Iterable<DomainEvent> domainEvents );
    void hookUpVersionProvider( VersionProvider versionProvider );
    Iterable<DomainEvent> getChanges();
    UUID getId();
}
