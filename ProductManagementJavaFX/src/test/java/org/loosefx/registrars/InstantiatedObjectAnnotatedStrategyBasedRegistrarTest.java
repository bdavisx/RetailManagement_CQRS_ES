package org.loosefx.registrars;

import org.axonframework.domain.AbstractAggregateRoot;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.junit.Assert;
import org.junit.Test;
import org.loosefx.events.ApplicationEventHandler;
import org.loosefx.eventsourcing.AggregateVersion;
import org.loosefx.eventsourcing.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InstantiatedObjectAnnotatedStrategyBasedRegistrarTest {
    @Test
    public void itShouldRegisterAnnotatedMethods() throws Exception {
        InstantiatedObjectAnnotatedStrategyBasedRegistrar registrar =
            new InstantiatedObjectAnnotatedStrategyBasedRegistrar( ApplicationEventHandler.class );

        TestRegister itemBeingRegistrared = new TestRegister();
        UUID presentationId = UUID.randomUUID();
        UUID event1Id = UUID.randomUUID();
        UUID event2Id = UUID.randomUUID();

        registrar.registerAnnotatedHandlers( itemBeingRegistrared, itemBeingRegistrared::registerEvent );

        itemBeingRegistrared.causeEvent1( event1Id, presentationId );
        itemBeingRegistrared.causeEvent2( event2Id, presentationId );

        Assert.assertTrue(
            itemBeingRegistrared.receivedEvents.stream().anyMatch( e -> e.getEventId().equals( event1Id ) ) );
        Assert.assertTrue(
            itemBeingRegistrared.receivedEvents.stream().anyMatch( e -> e.getEventId().equals( event2Id ) ) );
    }

    private class AbstractEvent implements DomainEvent {
        protected final UUID eventId;
        protected UUID presentationId;
        private AggregateVersion aggregateVersion;

        public AbstractEvent(
            final UUID eventId, UUID presentationId ) {
            this.eventId = eventId;
            this.presentationId = presentationId;
        }

        @Override
        public UUID getEventId() { return eventId; }
        @Override
        public UUID getAggregateId() { return getPresentationId(); }
        @Override
        public void setAggregateId( final UUID aggregateId ) { presentationId = aggregateId; }
        @Override
        public AggregateVersion getVersion() { return aggregateVersion; }
        @Override
        public void setVersion( final AggregateVersion version ) { this.aggregateVersion = version; }
        public UUID getPresentationId() {return presentationId;}
    }

    private class Event1 extends AbstractEvent {
        private Event1( final UUID eventId, final UUID presentationId ) {
            super( eventId, presentationId );
        }
    }

    private class Event2 extends AbstractEvent {
        private Event2( final UUID eventId, final UUID presentationId ) {
            super( eventId, presentationId );
        }
    }

    public class TestRegister extends AbstractAnnotatedAggregateRoot {
        public List<DomainEvent> receivedEvents = new ArrayList<>();

        public void causeEvent1( final UUID eventId, final UUID presentationId ) {
            apply( new Event1( eventId, presentationId ) );
        }
        public void causeEvent2( final UUID eventId, final UUID presentationId ) {
            apply( new Event2( eventId, presentationId ) );
        }

        @ApplicationEventHandler
        private void handle1( Event1 event ) {
            receivedEvents.add( event );
        }

        @ApplicationEventHandler
        private void handle2( Event2 event ) {
            receivedEvents.add( event );
        }

        @Override
        public Object getIdentifier() {
            // TODO: change default implementation
            return null;
        }
    }
}
