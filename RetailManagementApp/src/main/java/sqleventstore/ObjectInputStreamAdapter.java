package sqleventstore;

import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.UUID;

public class ObjectInputStreamAdapter implements DomainEventStream {
    private final ObjectInputStream objectInputStream;
    private final UUID aggregateIdentifier;
    private volatile DomainEventMessage nextEventMessage;

    @Override
    public DomainEventMessage peek() { return nextEventMessage; }

    /** Construct a EventStream using the provided ObjectInputStream as event provider. */
    public ObjectInputStreamAdapter(final ObjectInputStream objectInputStream) {
        this.objectInputStream = objectInputStream;
        try {
            nextEventMessage = readIfAvailable(objectInputStream);
        } catch (IOException e) {
            throw new IllegalStateException("The provided objectInputStream is not ready for reading");
        }
        aggregateIdentifier = nextEventMessage == null ? null : (UUID) nextEventMessage.getAggregateIdentifier();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() { return nextEventMessage != null; }

    /** {@inheritDoc} */
    @Override
    public DomainEventMessage next() {
        DomainEventMessage currentEvent = nextEventMessage;
        try {
            nextEventMessage = readIfAvailable(objectInputStream);
        } catch (IOException e) {
            throw new IllegalStateException("The provided objectInputStream is not ready for reading");
        }
        return currentEvent;
    }

    /**
     * Returns the Aggregate Identifier that the events in this stream apply to. May return null if no events are
     * available in the stream.
     *
     * @return the aggregate identifier of the events in this stream
     */
    public UUID getAggregateIdentifier() { return aggregateIdentifier; }

    private DomainEventMessage readIfAvailable(ObjectInputStream objectStream) throws IOException {
        try {
            return (DomainEventMessage) objectStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return readIfAvailable(objectStream);
        } catch (EOFException e) {
            // what an ugly way to escape a loop, but that's how the ObjectInputStream works
            return null;
        }
    }
}

