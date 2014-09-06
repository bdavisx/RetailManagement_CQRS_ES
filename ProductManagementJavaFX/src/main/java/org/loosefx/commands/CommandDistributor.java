package org.loosefx.commands;

import org.loosefx.eventbus.EventService;
import org.loosefx.eventbus.EventSubscriber;

public class CommandDistributor {
    private final EventService eventService;

    /** As a general rule, the eventService will be "local", it shouldn't be the application wide event service. */
    public CommandDistributor( final EventService eventService ) {
        this.eventService = eventService;
    }

    public <T> void register( Class<T> messageClass, EventSubscriber<T> consumer ) {
        eventService.getSubscribers( messageClass ).stream().forEach( ( subscriber ) ->
            eventService.unsubscribe( messageClass, subscriber ) );
        eventService.subscribeExactly( messageClass, consumer );
    }

    public <T> void unregister( Class<T> messageClass, EventSubscriber<T> consumer ) {
        eventService.unsubscribe( messageClass, consumer );
    }

    public void send( Object command ) {
        if( eventService.getSubscribers( command.getClass() ).size() == 0 ) {
            throw new NoHandlerForCommandException( command );
        }
        eventService.publish( command );
    }
}
