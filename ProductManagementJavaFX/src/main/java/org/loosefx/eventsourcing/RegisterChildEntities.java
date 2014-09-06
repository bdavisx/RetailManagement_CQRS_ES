package org.loosefx.eventsourcing;

public interface RegisterChildEntities {
    void registerChildEventProvider( EntityEventProvider entityEventProvider );
}
