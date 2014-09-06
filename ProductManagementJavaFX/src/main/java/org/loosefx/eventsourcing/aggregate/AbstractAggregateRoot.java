package org.loosefx.eventsourcing.aggregate;

import org.loosefx.eventbus.EventService;
import org.loosefx.eventsourcing.AggregateVersion;
import org.loosefx.eventsourcing.DomainEvent;
import org.loosefx.eventsourcing.DomainEventVersionComparator;
import org.loosefx.eventsourcing.EntityEventProvider;
import org.loosefx.eventsourcing.RegisterChildEntities;
import org.loosefx.eventsourcing.UnregisteredDomainEventException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractAggregateRoot implements EventProvider, RegisterChildEntities {
    private final List<DomainEvent> appliedEvents;
    private final List<EntityEventProvider> childEventProviders;
    private final Map<Class, Consumer<DomainEvent>> registeredEvents;

    private UUID id;
    private AggregateVersion version = new AggregateVersion( 0 );
    private AggregateVersion currentEventVersion = new AggregateVersion( 0 );
    private EventService eventBus = null;

    protected AbstractAggregateRoot() {
        appliedEvents = new ArrayList<>();
        childEventProviders = new ArrayList<>();
        registeredEvents = new HashMap<>();
    }

    public UUID getId() { return id; }
    protected void setId( final UUID id ) { this.id = id; }

    public AggregateVersion getVersion() { return version; }
    protected void setVersion( final AggregateVersion version ) { this.version = version; }

    public AggregateVersion getCurrentEventVersion() { return currentEventVersion; }
    protected void setCurrentEventVersion( final AggregateVersion currentEventVersion ) {
        this.currentEventVersion = currentEventVersion;
    }

    @Override
    public void loadFromHistory( final Iterable<DomainEvent> domainEvents ) {
        DomainEvent lastEvent = null;
        for( final Iterator<DomainEvent> iterator = domainEvents.iterator(); iterator.hasNext(); ) {
            DomainEvent domainEvent = iterator.next();
            apply( domainEvent.getClass(), domainEvent );
            lastEvent = domainEvent;
        }

        if( lastEvent != null ) {
            setVersion( lastEvent.getVersion() );
            setCurrentEventVersion( getVersion() );
        }
    }

    @Override
    public List<DomainEvent> getChanges() {
        List<DomainEvent> allChanges = new ArrayList<>( appliedEvents );
        allChanges.addAll( getChildEvents() );
        allChanges.sort( new DomainEventVersionComparator() );
        return allChanges;
    }

    @Override
    public void clearEvents() {
        childEventProviders.forEach( eventProvider -> eventProvider.clear() );
        appliedEvents.clear();
    }

    @Override
    public void updateVersion( AggregateVersion version ) {
        this.version = version;
    }

    @Override
    public void registerChildEventProvider( final EntityEventProvider entityEventProvider ) {
        entityEventProvider.hookUpVersionProvider( this::determineNewEventVersion );
        childEventProviders.add( entityEventProvider );
    }

    @Override
    public void registerEvent( final Class eventType, final Consumer<DomainEvent> eventHandler ) {
        registeredEvents.put( eventType, eventHandler );
    }

    protected void apply( final DomainEvent domainEvent ) {
        domainEvent.setAggregateId( getId() );
        domainEvent.setVersion( determineNewEventVersion() );
        apply( domainEvent.getClass(), domainEvent );
        appliedEvents.add( domainEvent );
    }

    protected void apply( final Class eventType, final DomainEvent domainEvent ) {
        makeSureEventHasHandler( eventType );
        final Consumer<DomainEvent> handler = registeredEvents.get( eventType );
        handler.accept( domainEvent );
        publishEvent( domainEvent );
    }

    private void makeSureEventHasHandler( final Class eventType ) {
        if( !registeredEvents.containsKey( eventType ) ) {
            throw new UnregisteredDomainEventException(
                String.format( "The requested domain event '%s' is not registered in '%s'", eventType.getName(),
                    getClass().getName() ) );
        }
    }

    private void publishEvent( final DomainEvent domainEvent ) {
        if( eventBus != null ) {
            eventBus.publish( domainEvent );
        }
    }

    //private Stream<DomainEvent> getChildEventsAndUpdateEventVersion() {
    private List<DomainEvent> getChildEvents() {
        List<DomainEvent> childEvents = new ArrayList<>();
        for( EntityEventProvider eventProvider : childEventProviders ) {
            for( DomainEvent event : eventProvider.getChanges() ) {
                childEvents.add( event );
            }
        }
        return childEvents;
    }

    /** I don't like having a change method return a value, but we want this to be atomic (I think). */
    private AggregateVersion determineNewEventVersion() {
        setCurrentEventVersion( getCurrentEventVersion().incrementVersion() );
        return getCurrentEventVersion();
    }

    public void setEventBus( final EventService eventBus ) {
        this.eventBus = eventBus;
    }
}
