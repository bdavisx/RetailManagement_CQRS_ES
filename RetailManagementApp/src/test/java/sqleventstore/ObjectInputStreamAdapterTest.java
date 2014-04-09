package sqleventstore;

import org.axonframework.domain.DomainEventMessage;
import org.junit.Test;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ObjectInputStreamAdapterTest implements Serializable {
    @Test
    public void itShouldReturnObjectsCorrectly() throws Exception {
        List<DomainEventMessage<EventStoreTestUtilities.TestEvent>> initialEventMessages = EventStoreTestUtilities
            .createEventMessages( UUID.randomUUID() );
        final byte[] domainMessagesSerializedToBytes = createSerializedMessages( initialEventMessages );
        ObjectInputStreamAdapter adapter = createObjectInputStreamAdapter( domainMessagesSerializedToBytes );
        List<DomainEventMessage<EventStoreTestUtilities.TestEvent>> returnedEventMessages = EventStoreTestUtilities
            .extractReturnedEventMessages( adapter );

        final List<EventStoreTestUtilities.TestEvent> initialEvents = EventStoreTestUtilities
            .extractTestEvents( initialEventMessages );
        final List<EventStoreTestUtilities.TestEvent> returnedEvents = EventStoreTestUtilities
            .extractTestEvents( returnedEventMessages );

        for( EventStoreTestUtilities.TestEvent currentInitialEvent : initialEvents ) {
            Assert.isTrue( returnedEvents.contains( currentInitialEvent ) );
        }
    }

    @Test
    public void itShouldTheFirstObjectAsPeakBeforeNext() throws Exception {
        List<DomainEventMessage<EventStoreTestUtilities.TestEvent>> initialEventMessages = EventStoreTestUtilities
            .createEventMessages( UUID.randomUUID() );
        final byte[] domainMessagesSerializedToBytes = createSerializedMessages( initialEventMessages );
        ObjectInputStreamAdapter adapter = createObjectInputStreamAdapter( domainMessagesSerializedToBytes );

        assertThat( adapter.peek().getPayload(), equalTo( initialEventMessages.get(0).getPayload() ) );
    }

    private ObjectInputStreamAdapter createObjectInputStreamAdapter( final byte[] domainMessagesSerializedToBytes )
        throws IOException {ByteArrayInputStream byteInputStream = new ByteArrayInputStream( domainMessagesSerializedToBytes );
        ObjectInputStream objectInputStream = new ObjectInputStream( byteInputStream );
        return new ObjectInputStreamAdapter( objectInputStream );
    }

    private byte[] createSerializedMessages( final List<DomainEventMessage<EventStoreTestUtilities.TestEvent>> eventMessages ) throws IOException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream( byteOutputStream );
        for( DomainEventMessage<EventStoreTestUtilities.TestEvent> eventMessage : eventMessages ) {
            objectOutputStream.writeObject( eventMessage );
        }
        objectOutputStream.close();
        byteOutputStream.close();
        return byteOutputStream.toByteArray();
    }

}
