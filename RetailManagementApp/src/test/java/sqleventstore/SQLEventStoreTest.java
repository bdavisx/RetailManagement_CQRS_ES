package sqleventstore;

import org.apache.ibatis.session.SqlSessionFactory;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.domain.SimpleDomainEventStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SQLEventStoreTest {
    private UUID aggregateIdentifier;
    private List<DomainEventMessage<EventStoreTestUtilities.TestEvent>> domainEventMessages;
    private DomainEventStream eventStream;
    private SqlSessionFactory sessionFactory;
    private MockSQLEventStoreDAO mockEventStoreDAO;
    private SQLEventStore eventStore;

    @Before
    public void setUp() throws Exception {
        aggregateIdentifier = UUID.randomUUID();
        domainEventMessages = EventStoreTestUtilities.createEventMessages( aggregateIdentifier );
        eventStream = new SimpleDomainEventStream( domainEventMessages );
        sessionFactory = mock( SqlSessionFactory.class );
        mockEventStoreDAO = new MockSQLEventStoreDAO();
        eventStore = new SQLEventStore( sessionFactory, mockEventStoreDAO, new XStreamEventStoreSerializer() );
    }

    @Test
    public void appendEventsShouldCallMapperInsertEventWithCorrectValues() throws Exception {
        mockEventStoreDAO.doesAggregateExist = true;
        eventStore.appendEvents( EventStoreTestUtilities.TestEvent.class.getCanonicalName(), eventStream );

        for( DomainEventMessage<EventStoreTestUtilities.TestEvent> eventMessage : domainEventMessages ) {
            boolean foundMatchInInsertEventCalls = doesEventCallsContainEventMessage( eventMessage,
                mockEventStoreDAO.insertEventCalls );
            failIfMatchNotFound( foundMatchInInsertEventCalls );
        }
        assertThat( mockEventStoreDAO.insertEventCalls.size() ).isEqualTo( 10 );
        assertThat( mockEventStoreDAO.updateAggregateVersionNumberCalls.get(0).versionNumber ).isEqualTo( 9 );
    }

    @Test
    public void itShouldCallInsertAggregateIfAggregateDoesNotExist() throws Exception {
        mockEventStoreDAO.doesAggregateExist = false;
        final String typeName = EventStoreTestUtilities.TestEvent.class.getCanonicalName();
        eventStore.appendEvents( typeName, eventStream );

        final MockSQLEventStoreDAO.InsertAggregateCallValues insertAggregateCallValues =
            mockEventStoreDAO.insertAggregateCalls.get( 0 );
        assertThat( insertAggregateCallValues.uuid.toString() ).isEqualTo( aggregateIdentifier.toString() );
        assertThat( insertAggregateCallValues.type ).isEqualTo( typeName );
        assertThat( insertAggregateCallValues.versionNumber ).isEqualTo( 0 );
    }

    private boolean doesEventCallsContainEventMessage(
        final DomainEventMessage<EventStoreTestUtilities.TestEvent> eventMessage,
        final List<MockSQLEventStoreDAO.InsertEventCallValues> insertEventCalls ) {
        boolean foundMatchInInsertEventCalls = false;
        for( MockSQLEventStoreDAO.InsertEventCallValues insertValues : insertEventCalls ) {
            if( eventMessageEqualsInsertValues( eventMessage, insertValues ) ) {
                foundMatchInInsertEventCalls = true;
                break;
            }
        }
        return foundMatchInInsertEventCalls;
    }

    private boolean eventMessageEqualsInsertValues(
        final DomainEventMessage<EventStoreTestUtilities.TestEvent> eventMessage,
        final MockSQLEventStoreDAO.InsertEventCallValues insertValues ) {
        return eventMessage.getAggregateIdentifier().toString().equals( insertValues.aggregateUUID.toString() ) &&
            eventMessage.getSequenceNumber() == insertValues.versionNumber &&
            eventMessage.getIdentifier().equals( insertValues.eventUUID.toString() );
    }

    private void failIfMatchNotFound( final boolean foundMatchInInsertEventCalls ) {
        if( !foundMatchInInsertEventCalls ) {
            Assert.fail( "Didn't find a matching item in insertValues: " +
                EventStoreTestUtilities.createEventMessagesOutput( domainEventMessages ) + "; " +
                mockEventStoreDAO.insertEventCalls );
        }
    }

}



























