package org.loosefx.eventsourcing;

public class AggregateVersion implements Comparable<AggregateVersion> {
    private final int version;

    public AggregateVersion( final int version ) { this.version = version; }

    public AggregateVersion incrementVersion() { return new AggregateVersion( version + 1 ); }

    @Override
    public int compareTo( final AggregateVersion rhs ) { return version - rhs.version; }
}
