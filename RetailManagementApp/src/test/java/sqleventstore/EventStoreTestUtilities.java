package sqleventstore;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.GenericDomainEventMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventStoreTestUtilities {
    public static List<TestEvent> extractTestEvents( final List<DomainEventMessage<TestEvent>> eventMessages ) {
        List<TestEvent> testEvents = new ArrayList<>();
        for( DomainEventMessage<TestEvent> eventMessage : eventMessages ) {
            testEvents.add( eventMessage.getPayload() );
        }
        return testEvents;
    }

    public static List<DomainEventMessage<TestEvent>> extractReturnedEventMessages(
        final ObjectInputStreamAdapter adapter ) {
        List<DomainEventMessage<TestEvent>> returnedEventMessages = new ArrayList<>();
        while( adapter.hasNext() ) {
            returnedEventMessages.add( adapter.next() );
        }
        return returnedEventMessages;
    }

    public static List<DomainEventMessage<TestEvent>> createEventMessages( UUID aggregateIdentifier ) {
        List<DomainEventMessage<TestEvent>> eventMessages = new ArrayList<>();
        for( long sequence = 0; sequence < 10; sequence++ ) {
            final TestEvent testEvent = new TestEvent();
            DomainEventMessage message = new GenericDomainEventMessage<TestEvent>(
                aggregateIdentifier, sequence, testEvent );
            eventMessages.add( message );
        }
        return eventMessages;
    }

    public static String createEventMessagesOutput( List<DomainEventMessage<TestEvent>> eventMessages ) {
        ToStringBuilder builder = new ToStringBuilder( eventMessages );
        for( DomainEventMessage<TestEvent> eventMessage : eventMessages ) {
            builder.append( "eventMessage: " );
            builder.append( "aggregateIdentifier", eventMessage.getAggregateIdentifier().toString() );
            builder.append( "sequenceNumber", eventMessage.getSequenceNumber() );
            builder.append( "payload", eventMessage.getPayload().toString() );
        }
        return builder.toString();
    }

    public static class TestEvent implements Serializable {
        public UUID uuid = UUID.randomUUID();

        @Override
        public boolean equals( final Object o ) {
            if( this == o ) { return true; }
            if( !(o instanceof TestEvent) ) { return false; }
            final TestEvent testEvent = (TestEvent) o;
            if( !uuid.toString().equals( testEvent.uuid.toString() ) ) { return false; }
            return true;
        }

        @Override
        public int hashCode() {
            return uuid.hashCode();
        }

        @Override
        public String toString() {
            return "TestEvent{" +
                "uuid (event data, not aggregate or event uuid)=" + uuid +
                '}';
        }
    }
}
