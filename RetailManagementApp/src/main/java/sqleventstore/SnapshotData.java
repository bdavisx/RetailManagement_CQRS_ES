package sqleventstore;

public class SnapshotData {
    private final String eventData;
    private final long sequenceNumber;

    public SnapshotData( final String eventData, final long sequenceNumber ) {
        this.eventData = eventData;
        this.sequenceNumber = sequenceNumber;
    }

    public String getEventData() { return eventData; }
    public long getSequenceNumber() { return sequenceNumber; }
}
