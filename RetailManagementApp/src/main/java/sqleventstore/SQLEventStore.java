package sqleventstore;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.eventstore.SnapshotEventStore;
import org.axonframework.repository.AggregateNotFoundException;
import org.axonframework.repository.ConcurrencyException;
import org.axonframework.upcasting.UpcasterAware;
import org.axonframework.upcasting.UpcasterChain;
import org.joda.time.LocalDateTime;
import org.springframework.dao.DuplicateKeyException;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLEventStore implements SnapshotEventStore, UpcasterAware {
    @Inject
    private SqlSessionFactory sessionFactory;
    @Inject
    private SQLEventStoreDAO dao;
    private final EventStoreSerializer serializer;

    public SQLEventStore( EventStoreSerializer serializer ) {
        this.serializer = serializer;
        serializer.registerConverter( new LocalDateTimeConverter() );
    }

    public SQLEventStore() {
        this( new XStreamEventStoreSerializer() );
    }

    public SQLEventStore( SqlSessionFactory sessionFactory, SQLEventStoreDAO dao, EventStoreSerializer serializer ) {
        this( serializer );
        this.sessionFactory = sessionFactory;
        this.dao = dao;
    }

    public SQLEventStore( SqlSessionFactory sessionFactory, SQLEventStoreDAO dao ) {
        this();
        this.sessionFactory = sessionFactory;
        this.dao = dao;
    }

    @Override
    public void appendEvents( String type, DomainEventStream eventStream ) {
        if (!eventStream.hasNext()) { return; }
        List<DomainEventMessage> eventMessages = copyEventsToList( eventStream );
        Long maxSeqNumber = Long.valueOf( -1 );
        UUID aggregateIdentifier = (UUID) eventMessages.get(0).getAggregateIdentifier();

        try {
            makeSureAggregateExistsInStore( type, aggregateIdentifier );

            for( DomainEventMessage eventMessage : eventMessages ) {
                // TODO: what about a transaction?
                dao.insertEvent( UUID.fromString( eventMessage.getIdentifier() ), serializer.toXML( eventMessage ),
                    eventMessage.getSequenceNumber(), (UUID) eventMessage.getAggregateIdentifier() );

                if( eventMessage.getSequenceNumber() > maxSeqNumber ) { maxSeqNumber = eventMessage.getSequenceNumber(); }
            }

            if( maxSeqNumber > -1 ) {
                dao.updateAggregateVersionNumber( aggregateIdentifier, maxSeqNumber );
            }
        } catch( DuplicateKeyException e ) {
            convertDuplicateKeyException( type, aggregateIdentifier );
        }
    }

    @Override
    public void appendSnapshotEvent( final String type, final DomainEventMessage snapshotEvent ) {
        UUID aggregateIdentifier = (UUID) snapshotEvent.getAggregateIdentifier();

        try {
            makeSureAggregateExistsInStore( type, aggregateIdentifier );

            dao.insertSnapshotEvent( UUID.fromString( snapshotEvent.getIdentifier() ),
                serializer.toXML( snapshotEvent ),
                snapshotEvent.getSequenceNumber(), (UUID) snapshotEvent.getAggregateIdentifier() );

        } catch( DuplicateKeyException e ) {
            convertDuplicateKeyException( type, aggregateIdentifier );
        }
    }

    @Override
    public DomainEventStream readEvents( final String type, final Object identifierAsObject ) {
        UUID aggregateIdentifier = (UUID) identifierAsObject;
        try {
            errorIfAggregateDoesNotExist( type, aggregateIdentifier );

            List<String> eventsData = new ArrayList<>();

            final SnapshotData snapshotData = dao.selectLastSnapshotEventData( aggregateIdentifier );
            if( snapshotData != null ) {
                eventsData.add( snapshotData.getEventData() );
                eventsData.addAll(
                    dao.selectAggregateEventsDataAfterVersion( aggregateIdentifier, snapshotData.getSequenceNumber() ) );
            } else {
                eventsData.addAll( dao.selectAggregateEventsData( aggregateIdentifier ) );
            }

            InputStream stream = new ByteArrayInputStream( eventsData.get( 0 ).getBytes( "UTF-8" ) );
            for( int i = 1; i < eventsData.size(); i++ ) {
                stream = new SequenceInputStream( stream,
                    new ByteArrayInputStream( eventsData.get( i ).getBytes( "UTF-8" ) ) );
            }

            InputStream inputStream = surroundWithObjectStreamTag( stream );
            ObjectInputStream eventsStream = serializer.createObjectInputStream( inputStream );
            return new ObjectInputStreamAdapter( eventsStream );
        } catch( Exception e ) {
            throw new IllegalStateException( String.format(
                "An error occurred while trying to read eventStream for aggregate type [%s] with aggregateIdentifier [%s]",
                type, aggregateIdentifier.toString() ), e );
        }
    }

    @Override
    public void setUpcasterChain( final UpcasterChain upcasterChain ) {
        // TODO: implement upcasting
    }

    private void makeSureAggregateExistsInStore( final String type, final UUID aggregateIdentifier ) {
        if( !dao.doesAggregateExist( aggregateIdentifier ) ) {
            dao.insertAggregate( aggregateIdentifier, type, Long.valueOf( 0 ) );
        }
    }

    private void errorIfAggregateDoesNotExist( final String type, final UUID identifier ) {
        if( !dao.doesAggregateExist( identifier ) ) {
            throw new AggregateNotFoundException( identifier,
                String.format( "Aggregate of type [%s] with identifier [%s] cannot be found.", type,
                    identifier.toString() ) );
        }
    }

    private List<DomainEventMessage> copyEventsToList( final DomainEventStream events ) {
        List<DomainEventMessage> messages = new ArrayList<DomainEventMessage>();
        while (events.hasNext()) {
            messages.add(events.next());
        }
        return messages;
    }

    private InputStream surroundWithObjectStreamTag( InputStream stream ) throws UnsupportedEncodingException {
        InputStream prefix = new ByteArrayInputStream( "<object-stream>".getBytes( "UTF-8" ) );
        InputStream suffix = new ByteArrayInputStream( "</object-stream>".getBytes( "UTF-8" ) );
        return new SequenceInputStream( prefix, new SequenceInputStream( stream, suffix ) );
    }

    private void convertDuplicateKeyException( final String type, final UUID aggregateIdentifier ) {
        throw new ConcurrencyException( String.format(
            "Unable to store eventStream for the aggregate of type [%s] and identifier [%s]",
            type, aggregateIdentifier ) );
    }

    public static class LocalDateTimeConverter implements SingleValueConverter {
        @Override
        @SuppressWarnings("rawtypes")
        public boolean canConvert( Class type ) {
            return type.equals( LocalDateTime.class );
        }

        @Override
        public String toString( Object obj ) {
            return obj.toString();
        }

        @Override
        public Object fromString( String str ) {
            return new LocalDateTime( str );
        }
    }

}
