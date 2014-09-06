package org.loosefx.events;

import org.loosefx.eventbus.EventSubscriber;
import org.loosefx.eventbus.ThreadSafeEventService;
import org.junit.Assert;
import org.junit.Test;

public class EventContainerTest {

  @Test
  public void itShouldPublishAnEventWhenEventIsStored() {
    DummyEvent event = new DummyEvent();

    EventHandler handler = new EventHandler();
    ThreadSafeEventService eventBus = new ThreadSafeEventService();
    eventBus.subscribe( DummyEvent.class, new EventSubscriber() {
      @Override
      public void onEvent( final Object event ) {
        handler.handleDummyEvent( (DummyEvent) event );
      }
    } );
    EventContainer eventContainer = new EventContainer( eventBus );

    eventContainer.addEvent( event );

    Assert.assertSame( event, handler.event );
  }

  private static class EventHandler {
    private DummyEvent event;

    public void handleDummyEvent( DummyEvent event ) {
      this.event = event;
    }
  }

  private static class DummyEvent {}
}
