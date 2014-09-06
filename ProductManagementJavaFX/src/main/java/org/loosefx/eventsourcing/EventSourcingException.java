package org.loosefx.eventsourcing;

public class EventSourcingException extends RuntimeException {
    public EventSourcingException( final String message ) {
        super( message );
    }

    public EventSourcingException( final String message, final Throwable cause ) {
        super( message, cause );
    }
}
