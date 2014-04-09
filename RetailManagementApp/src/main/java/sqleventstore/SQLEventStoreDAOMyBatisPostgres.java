package sqleventstore;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@Resource
public class SQLEventStoreDAOMyBatisPostgres implements SQLEventStoreDAO {
    @Inject
    private SqlSessionFactory sessionFactory;

    // SELECT 1 FROM AGGREGATE WHERE AGGREGATE_UUID=#{aggregateUUID}
    @Override
    public boolean doesAggregateExist( UUID aggregateUUID ) {
        final SqlSession session = sessionFactory.openSession();
        try {
            final int value = session.selectOne( "doesAggregateExist", aggregateUUID );
            return value == 1;
        } finally {
            session.close();
        }
    };

    // INSERT INTO AGGREGATE (AGGREGATE_UUID, TYPE, VERSION_NUMBER) VALUES (#{aggregateUUID}, #{type}, #{versionNumber})
    @Override
    public void insertAggregate( UUID aggregateUUID, String type, Long versionNumber ) {
        final SqlSession session = sessionFactory.openSession();
        try {
            session.insert( "insertAggregate", new AggregateTable( aggregateUUID, type, versionNumber ) );
        } finally {
            session.close();
        }
    };

    // INSERT INTO EVENT_STORE (EVENT_UUID, EVENT_DATA, VERSION_NUMBER, AGGREGATE_UUID)
    // VALUES (#{eventUUID}, #{eventData}, #{versionNumber}, #{aggregateUUID})
    @Override
    public void insertEvent( UUID eventUUID, String eventData, Long versionNumber, UUID aggregateUUID ) {
        final SqlSession session = sessionFactory.openSession();
        try {
            session.insert( "insertEvent", new EventTable( eventUUID, eventData, versionNumber, aggregateUUID ) );
        } finally {
            session.close();
        }
    }

    // UPDATE AGGREGATE SET VERSION_NUMBER=#{versionNumber} WHERE AGGREGATE_UUID=#{aggregateUUID}
    @Override
    public void updateAggregateVersionNumber( UUID aggregateUUID, Long versionNumber ) {
        final SqlSession session = sessionFactory.openSession();
        try {
            session.update( "updateAggregateVersionNumber", new AggregateTable( aggregateUUID, null, versionNumber ) );
        } finally {
            session.close();
        }
    }

    // SELECT EVENT_DATA FROM EVENT_STORE WHERE AGGREGATE_UUID=#{aggregateUUID} ORDER BY VERSION_NUMBER
    @Override
    public List<String> selectAggregateEventsData( UUID aggregateUUID ) {
        final SqlSession session = sessionFactory.openSession();
        try {
            return session.selectList( "selectAggregateEventsData", aggregateUUID );
        } finally {
            session.close();
        }
    }

    //SELECT EVENT_DATA FROM EVENT_STORE
    //WHERE AGGREGATE_UUID=#{aggregateUUID} and version_number > #{versionNumber}
    //ORDER BY VERSION_NUMBER
    public List<String> selectAggregateEventsDataAfterVersion( UUID aggregateUUID, long sequenceNumber ) {
        final SqlSession session = sessionFactory.openSession();
        try {
            return session.selectList( "selectAggregateEventsDataAfterVersion",
                new EventTable( null, null, sequenceNumber, aggregateUUID ) );
        } finally {
            session.close();
        }
    }

    //INSERT INTO SNAPSHOT_EVENT_STORE (EVENT_UUID, EVENT_DATA, VERSION_NUMBER, AGGREGATE_UUID)
    //VALUES (#{eventUUID}, #{eventData}, #{versionNumber}, #{aggregateUUID})
    @Override
    public void insertSnapshotEvent( UUID eventUUID, String eventData, Long versionNumber, UUID aggregateUUID ) {
        final SqlSession session = sessionFactory.openSession();
        try {
            session.insert( "insertSnapshotEvent", new EventTable( eventUUID, eventData, versionNumber, aggregateUUID ) );
        } finally {
            session.close();
        }
    }

    //SELECT EVENT_DATA, version_number
    //FROM SNAPSHOT_EVENT_STORE WHERE AGGREGATE_UUID=#{aggregateUUID}
    //ORDER BY VERSION_NUMBER desc
    //fetch first 1 row only
    @Override
    public SnapshotData selectLastSnapshotEventData( UUID aggregateUUID ) {
        final SqlSession session = sessionFactory.openSession();
        try {
            return session.selectOne( "selectLastSnapshotEventData", aggregateUUID );
        } finally {
            session.close();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    private class AggregateTable {
        public UUID aggregateUUID;
        public String type;
        public Long versionNumber;

        private AggregateTable( final UUID aggregateUUID, final String type, final Long versionNumber ) {
            this.aggregateUUID = aggregateUUID;
            this.type = type;
            this.versionNumber = versionNumber;
        }
    }

    private class EventTable {
        public UUID eventUUID;
        public String eventData;
        public Long versionNumber;
        public UUID aggregateUUID;

        private EventTable( final UUID eventUUID, final String eventData, final Long versionNumber,
            final UUID aggregateUUID ) {
            this.eventUUID = eventUUID;
            this.eventData = eventData;
            this.versionNumber = versionNumber;
            this.aggregateUUID = aggregateUUID;
        }
    }

}
