package org.loosefx.events;

import org.loosefx.eventbus.EventService;

public class EventContainer {
    private final EventService eventService;

    public EventContainer( EventService eventBus ) {
        this.eventService = eventBus;
    }

    public void addEvent( Object event ) {
        eventService.publish( event );
    }
}
