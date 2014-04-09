package sqleventstore;

import java.util.List;
import java.util.UUID;

public interface SQLEventStoreDAO {
    // SELECT 1 FROM AGGREGATE WHERE AGGREGATE_UUID=#{aggregateUUID}
    boolean doesAggregateExist( UUID aggregateUUID );

    // INSERT INTO AGGREGATE (AGGREGATE_UUID, TYPE, VERSION_NUMBER) VALUES (#{aggregateUUID}, #{type}, #{versionNumber})
    void insertAggregate( UUID aggregateUUID, String type, Long versionNumber );

    // INSERT INTO EVENT_STORE (EVENT_UUID, EVENT_DATA, VERSION_NUMBER, AGGREGATE_UUID)
    // VALUES (#{eventUUID}, #{eventData}, #{versionNumber}, #{aggregateUUID})
    void insertEvent( UUID eventUUID, String eventData, Long versionNumber, UUID aggregateUUID );

    // UPDATE AGGREGATE SET VERSION_NUMBER=#{versionNumber} WHERE AGGREGATE_UUID=#{aggregateUUID}
    void updateAggregateVersionNumber( UUID aggregateUUID, Long versionNumber );

    // SELECT EVENT_DATA FROM EVENT_STORE WHERE AGGREGATE_UUID=#{aggregateUUID} ORDER BY VERSION_NUMBER
    List<String> selectAggregateEventsData( UUID aggregateUUID );

    //SELECT EVENT_DATA FROM EVENT_STORE
    //WHERE AGGREGATE_UUID=#{aggregateUUID} and version_number > #{versionNumber}
    //ORDER BY VERSION_NUMBER
    List<String> selectAggregateEventsDataAfterVersion( UUID aggregateIdentifier, long sequenceNumber );

    //INSERT INTO SNAPSHOT_EVENT_STORE (EVENT_UUID, EVENT_DATA, VERSION_NUMBER, AGGREGATE_UUID)
    //VALUES (#{eventUUID}, #{eventData}, #{versionNumber}, #{aggregateUUID})
    void insertSnapshotEvent( UUID eventUUID, String eventData, Long versionNumber, UUID aggregateUUID );

    //SELECT EVENT_DATA, version_number
    //FROM SNAPSHOT_EVENT_STORE WHERE AGGREGATE_UUID=#{aggregateUUID}
    //ORDER BY VERSION_NUMBER desc
    //fetch first 1 row only
    SnapshotData selectLastSnapshotEventData( UUID aggregateUUID );

}
