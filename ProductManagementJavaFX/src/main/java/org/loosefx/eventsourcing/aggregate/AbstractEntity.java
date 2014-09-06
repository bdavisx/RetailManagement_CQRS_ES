package org.loosefx.eventsourcing.aggregate;

import org.loosefx.eventsourcing.DomainEvent;
import org.loosefx.eventsourcing.EntityEventProvider;
import org.loosefx.eventsourcing.UnregisteredDomainEventException;
import org.loosefx.eventsourcing.VersionProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class AbstractEntity implements EntityEventProvider {
    private UUID id;
    private final Map<Class, Consumer<DomainEvent>> registeredEvents;
    private final List<DomainEvent> appliedEvents;
    private VersionProvider versionProvider;

    public AbstractEntity() {
        registeredEvents = new HashMap<>();
        appliedEvents = new ArrayList<>();
    }

    public UUID getId() { return id; }
    protected void setId( final UUID id ) { this.id = id; }

    protected void registerEvent( final Class eventType, final Consumer<DomainEvent> eventHandler ) {
        registeredEvents.put( eventType, eventHandler );
    }

    protected void apply( final DomainEvent domainEvent ) {
        domainEvent.setAggregateId( getId() );
        domainEvent.setVersion( versionProvider.getVersion() );
        apply( domainEvent.getClass(), domainEvent );
        appliedEvents.add( domainEvent );
    }

    private void apply( final Class eventType, final DomainEvent domainEvent ) {
        if( !registeredEvents.containsKey( eventType ) ) {
            throw new UnregisteredDomainEventException(
                String.format( "The requested domain event '%s' is not registered in '%s'", eventType.getName(),
                    getClass().getName() ) );
        }

        final Consumer<DomainEvent> handler = registeredEvents.get( eventType );
        handler.accept( domainEvent );
    }

    @Override
    public void loadFromHistory( final Iterable<DomainEvent> domainEvents ) {
        for( final Iterator<DomainEvent> iterator = domainEvents.iterator(); iterator.hasNext(); ) {
            DomainEvent domainEvent = iterator.next();
            apply( domainEvent.getClass(), domainEvent );
        }
    }

    @Override
    public void hookUpVersionProvider( VersionProvider versionProvider ) {
        this.versionProvider = versionProvider;
    }

    @Override
    public Iterable<DomainEvent> getChanges() { return appliedEvents; }

    @Override
    public void clear() { appliedEvents.clear(); }
}
