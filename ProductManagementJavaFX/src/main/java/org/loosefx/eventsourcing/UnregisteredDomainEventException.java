package org.loosefx.eventsourcing;

public class UnregisteredDomainEventException extends EventSourcingException {
    public UnregisteredDomainEventException( final String message ) {
        super( message );
    }
}
