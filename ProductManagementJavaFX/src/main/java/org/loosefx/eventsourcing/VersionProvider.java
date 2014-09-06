package org.loosefx.eventsourcing;

public interface VersionProvider {
    public AggregateVersion getVersion();
}
