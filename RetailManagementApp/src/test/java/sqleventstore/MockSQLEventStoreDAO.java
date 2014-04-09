package sqleventstore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MockSQLEventStoreDAO implements SQLEventStoreDAO {
    public List<UUID> doesAggregateExistCalls = new ArrayList<>();
    public List<InsertAggregateCallValues> insertAggregateCalls = new ArrayList<>();
    public List<InsertEventCallValues> insertEventCalls = new ArrayList<>();
    public List<UpdateAggregateVersionNumberCallValues> updateAggregateVersionNumberCalls = new ArrayList<>();
    public List<UUID> selectAggregateEventsDataCalls = new ArrayList<>();
    public boolean doesAggregateExist;
    public SnapshotData selectLastSnapshotEventData;
    public List<String> selectAggregateEventsData;
    public List<String> aggregateEventsDataAfterVersion;

    @Override
    public List<String> selectAggregateEventsDataAfterVersion( final UUID aggregateIdentifier,
        final long sequenceNumber ) {
        return aggregateEventsDataAfterVersion;
    }

    @Override
    public boolean doesAggregateExist( final UUID uuid ) {
        doesAggregateExistCalls.add( uuid );
        return doesAggregateExist;
    }

    @Override
    public void insertAggregate( final UUID uuid, final String type, final Long versionNumber ) {
        insertAggregateCalls.add( new InsertAggregateCallValues( uuid, type, versionNumber ));
    }

    @Override
    public void insertEvent( final UUID eventUUID, final String eventData, final Long versionNumber,
        final UUID aggregateUUID ) {
        insertEventCalls.add( new InsertEventCallValues( eventUUID, eventData, versionNumber, aggregateUUID ));
    }

    @Override
    public void updateAggregateVersionNumber( final UUID uuid, final Long versionNumber ) {
        updateAggregateVersionNumberCalls.add( new UpdateAggregateVersionNumberCallValues( uuid, versionNumber ) );
    }

    @Override
    public List<String> selectAggregateEventsData( final UUID uuid ) {
        selectAggregateEventsDataCalls.add( uuid );
        return selectAggregateEventsData;
    }

    @Override
    public void insertSnapshotEvent( final UUID eventUUID, final String eventData, final Long versionNumber,
        final UUID aggregateUUID ) {
        insertEventCalls.add( new InsertEventCallValues( eventUUID, eventData, versionNumber, aggregateUUID ) );
    }

    @Override
    public SnapshotData selectLastSnapshotEventData( final UUID aggregateUUID ) {
        return selectLastSnapshotEventData;
    }

    public static class InsertAggregateCallValues {
        public final UUID uuid;
        public final String type;
        public final Long versionNumber;

        public InsertAggregateCallValues( final UUID uuid, final String type, final Long versionNumber ) {
            this.uuid = uuid;
            this.type = type;
            this.versionNumber = versionNumber;
        }

        @Override
        public String toString() {
            return "InsertAggregateCallValues{" +
                "uuid=" + uuid +
                ", type='" + type + '\'' +
                ", versionNumber=" + versionNumber +
                '}';
        }
    }

    public static class InsertEventCallValues {
        public final UUID eventUUID;
        public final String eventData;
        public final Long versionNumber;
        public final UUID aggregateUUID;

        public InsertEventCallValues( final UUID eventUUID, final String eventData, final Long versionNumber,
            final UUID aggregateUUID ) {
            this.eventUUID = eventUUID;
            this.eventData = eventData;
            this.versionNumber = versionNumber;
            this.aggregateUUID = aggregateUUID;
        }

        @Override
        public String toString() {
            return "InsertEventCallValues{" +
                "eventUUID=" + eventUUID +
                ", versionNumber=" + versionNumber +
                ", aggregateUUID=" + aggregateUUID +
                '}';
        }
    }

    public static class UpdateAggregateVersionNumberCallValues {
        public final UUID aggregateId;
        public final long versionNumber;

        public UpdateAggregateVersionNumberCallValues( final UUID aggregateId, final long versionNumber ) {
            this.aggregateId = aggregateId;
            this.versionNumber = versionNumber;
        }

        @Override
        public String toString() {
            return "UpdateAggregateVersionNumberCallValues{" +
                "aggregateId=" + aggregateId +
                ", versionNumber=" + versionNumber +
                '}';
        }
    }
}
