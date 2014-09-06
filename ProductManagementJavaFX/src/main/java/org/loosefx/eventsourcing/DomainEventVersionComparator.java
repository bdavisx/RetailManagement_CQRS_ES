package org.loosefx.eventsourcing;

import java.util.Comparator;

public class DomainEventVersionComparator implements Comparator<DomainEvent> {
    @Override
    public int compare( final DomainEvent lhs, final DomainEvent rhs ) {
        return lhs.getVersion().compareTo( rhs.getVersion() );
    }
}
