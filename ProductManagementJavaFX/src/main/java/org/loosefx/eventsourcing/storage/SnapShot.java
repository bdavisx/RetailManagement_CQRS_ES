package org.loosefx.eventsourcing.storage;

import org.loosefx.eventsourcing.AggregateVersion;
import org.loosefx.eventsourcing.Memento;

import java.util.UUID;

public interface SnapShot {
    Memento getMemento();
    UUID getEventProviderId();
    AggregateVersion getVersion();
}
