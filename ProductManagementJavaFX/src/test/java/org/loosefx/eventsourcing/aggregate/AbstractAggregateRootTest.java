package org.loosefx.eventsourcing.aggregate;

import org.loosefx.eventbus.EventSubscriber;
import org.loosefx.eventbus.ThreadSafeEventService;
import org.junit.Assert;
import org.junit.Test;
import org.loosefx.events.AbstractDomainEvent;
import org.loosefx.events.ApplicationEventHandler;
import org.loosefx.eventsourcing.DomainEvent;

import java.util.UUID;

public class AbstractAggregateRootTest {
  private boolean wasEventReceived = false;

  @Test
  public void itShouldPublishAnEventWhenEventBusIsSet() {
    ThreadSafeEventService eventBus = new ThreadSafeEventService();
    EventSubscriber<DummyEvent> eventSubscriber = new EventSubscriber<DummyEvent>() {
      @Override
      public void onEvent( final DummyEvent event ) {
        wasEventReceived = true;
      }
    };
    eventBus.subscribe( DummyEvent.class, eventSubscriber );
    ConcreteAbstractAggregateRoot handler = new ConcreteAbstractAggregateRoot();
    handler.setEventBus( eventBus );

    UUID eventId = UUID.randomUUID();
    handler.causeEvent( eventId );

    Assert.assertSame( handler.eventFromApplyDummyEvent.getEventId(), eventId );
    Assert.assertTrue( wasEventReceived );
  }

  private void handleDummyEvent( DummyEvent event ) { wasEventReceived = true; }

  private static class ConcreteAbstractAggregateRoot extends AbstractAggregateRoot {
    public DummyEvent eventFromApplyDummyEvent;

    private ConcreteAbstractAggregateRoot() {
      registerEvent( DummyEvent.class, this::applyDummyEvent );
    }

    public void causeEvent( final UUID eventId ) {
      apply( new DummyEvent( eventId ) );
    }

    @ApplicationEventHandler
    private void applyDummyEvent( final DomainEvent event ) {
      eventFromApplyDummyEvent = (DummyEvent) event;
    }
  }

  private static class DummyEvent extends AbstractDomainEvent {
    private DummyEvent( final UUID eventId ) {
      super( eventId );
    }

  }
}
