package org.loosefx.eventsourcing.storage;

import org.loosefx.eventsourcing.aggregate.EventProvider;

import java.util.UUID;

public interface SnapShotStorage {
    SnapShot GetSnapShot( UUID entityId );
    void SaveShapShot( EventProvider entity );
}
