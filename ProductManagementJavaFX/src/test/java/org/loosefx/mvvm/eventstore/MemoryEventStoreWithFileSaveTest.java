package org.loosefx.mvvm.eventstore;

import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.domain.GenericDomainEventMessage;
import org.axonframework.domain.SimpleDomainEventStream;
import static org.fest.assertions.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MemoryEventStoreWithFileSaveTest {
  private UUID aggregateIdentifier;
  private DomainEventStream eventStream;
  public static final String typeName = "TypeName";

  @Before
  public void setUp() {
    aggregateIdentifier = UUID.randomUUID();
  }

  @Test
  public void itStoresEventsInAnExistingList() throws Exception {
    final Map<String, List<DomainEventMessage>> typeToEventListMap = new HashMap<>();
    final Map<String, List<DomainEventMessage>> typeToSnapshotEventListMap = new HashMap<>();
    final List<DomainEventMessage> existingEventList = new ArrayList<>();
    typeToEventListMap.put( typeName, existingEventList );

    final DomainEventMessage<StubDomainEvent> existingEvent = new GenericDomainEventMessage<>(
      aggregateIdentifier, 0, new StubDomainEvent( 0 ));
    existingEventList.add( existingEvent );

    final List<DomainEventMessage> regularEvents = createRegularEvents( 1, 2 );
    createEventStream( regularEvents );

    final MemoryEventStoreWithFileSave eventStore = new MemoryEventStoreWithFileSave( typeToEventListMap,
      typeToSnapshotEventListMap) ;
    eventStore.appendEvents( typeName, eventStream );

    makeSureListContainsEvents( regularEvents, existingEventList );
  }

  @Test
  public void itStoresEventsInAnNewList() throws Exception {
    final Map<String, List<DomainEventMessage>> typeToEventListMap = new HashMap<>();
    final Map<String, List<DomainEventMessage>> typeToSnapshotEventListMap = new HashMap<>();

    final List<DomainEventMessage> regularEvents = createRegularEvents( 1, 2 );
    createEventStream( regularEvents );

    final MemoryEventStoreWithFileSave eventStore = new MemoryEventStoreWithFileSave( typeToEventListMap,
      typeToSnapshotEventListMap) ;
    eventStore.appendEvents( typeName, eventStream );

    final List<DomainEventMessage> existingEventList = typeToEventListMap.get( typeName );
    makeSureListContainsEvents( existingEventList, regularEvents );
  }

  @Test
  public void itStoresSnapshotEventInAnNewList() throws Exception {
    final Map<String, List<DomainEventMessage>> typeToEventListMap = new HashMap<>();
    final Map<String, List<DomainEventMessage>> typeToSnapshotEventListMap = new HashMap<>();

    final List<DomainEventMessage> regularEvents = createRegularEvents( 1, 2 );
    createEventStream( regularEvents );
    final DomainEventMessage snapshotEvent = createSnapshotEvent( 3 );

    final MemoryEventStoreWithFileSave eventStore = new MemoryEventStoreWithFileSave( typeToEventListMap,
      typeToSnapshotEventListMap) ;
    eventStore.appendEvents( typeName, eventStream );
    eventStore.appendSnapshotEvent( typeName, snapshotEvent );

    final List<DomainEventMessage> existingEventList = typeToSnapshotEventListMap.get( typeName );
    assertThat( existingEventList ).contains( snapshotEvent );
  }

  private List<DomainEventMessage> createRegularEvents( final long startingSequenceNumber,
    final int numberOfEventsToCreate ) {
    final List<DomainEventMessage> events = new ArrayList<>();
    final long endingSequenceNumber = numberOfEventsToCreate + startingSequenceNumber;
    for( long currentSequenceNumber = startingSequenceNumber; currentSequenceNumber < endingSequenceNumber;
         currentSequenceNumber++ ) {
      events.add( new GenericDomainEventMessage<>( aggregateIdentifier, currentSequenceNumber,
        new StubDomainEvent( currentSequenceNumber ) ) );
    }
    return events;
  }

  @Test
  public void itReturnsRegularEvents() throws Exception {
    final Map<String, List<DomainEventMessage>> typeToEventListMap = new HashMap<>();
    final Map<String, List<DomainEventMessage>> typeToSnapshotEventListMap = new HashMap<>();
    final List<DomainEventMessage> existingEventList = new ArrayList<>();
    typeToEventListMap.put( typeName, existingEventList );

    final DomainEventMessage<StubDomainEvent> existingEvent = new GenericDomainEventMessage<>(
      aggregateIdentifier, 0, new StubDomainEvent( 0 ));
    existingEventList.add( existingEvent );
    existingEventList.addAll( createRegularEvents( 1, 2 ) );

    final MemoryEventStoreWithFileSave eventStore = new MemoryEventStoreWithFileSave( typeToEventListMap,
      typeToSnapshotEventListMap) ;

    final DomainEventStream domainEventStream = eventStore.readEvents( typeName, aggregateIdentifier );

    makeSureListContainsEvents( existingEventList, domainEventStream );
  }

  @Test
  public void itReturnsNoEventsIfAggregateIdentifierIsDifferent() throws Exception {
    final Map<String, List<DomainEventMessage>> typeToEventListMap = new HashMap<>();
    final Map<String, List<DomainEventMessage>> typeToSnapshotEventListMap = new HashMap<>();
    final List<DomainEventMessage> existingEventList = new ArrayList<>();
    typeToEventListMap.put( typeName, existingEventList );

    final DomainEventMessage<StubDomainEvent> existingEvent = new GenericDomainEventMessage<>(
      aggregateIdentifier, 0, new StubDomainEvent( 0 ));
    existingEventList.add( existingEvent );
    existingEventList.addAll( createRegularEvents( 1, 2 ) );

    final MemoryEventStoreWithFileSave eventStore = new MemoryEventStoreWithFileSave( typeToEventListMap,
      typeToSnapshotEventListMap) ;

    final DomainEventStream domainEventStream = eventStore.readEvents( typeName, UUID.randomUUID() );
    assertThat( domainEventStream.hasNext() ).isFalse();
  }

  @Test
  public void itReturnsRegularEventsAndSnapshot() throws Exception {
    final Map<String, List<DomainEventMessage>> typeToEventListMap = new HashMap<>();
    final Map<String, List<DomainEventMessage>> typeToSnapshotEventListMap = new HashMap<>();
    final List<DomainEventMessage> existingEventList = new ArrayList<>();
    final List<DomainEventMessage> existingSnapshotEventList = new ArrayList<>();
    typeToEventListMap.put( typeName, existingEventList );
    typeToSnapshotEventListMap.put( typeName, existingSnapshotEventList );

    final DomainEventMessage<StubDomainEvent> existingEvent = new GenericDomainEventMessage<>(
      aggregateIdentifier, 0, new StubDomainEvent( 0 ));
    existingEventList.add( existingEvent );
    existingEventList.addAll( createRegularEvents( 1, 2 ) );
    final DomainEventMessage snapshotEvent = createSnapshotEvent( 3 );
    existingSnapshotEventList.add( snapshotEvent );
    final List<DomainEventMessage> regularEventsAfterSnapshot = createRegularEvents( 4, 5 );
    existingEventList.addAll( regularEventsAfterSnapshot );

    final MemoryEventStoreWithFileSave eventStore = new MemoryEventStoreWithFileSave( typeToEventListMap,
      typeToSnapshotEventListMap) ;

    final DomainEventStream domainEventStream = eventStore.readEvents( typeName, aggregateIdentifier );

    final List<DomainEventMessage> eventsAfterSnapshotIncludingSnapshot = new ArrayList<>();
    eventsAfterSnapshotIncludingSnapshot.add( snapshotEvent );
    eventsAfterSnapshotIncludingSnapshot.addAll( regularEventsAfterSnapshot );

    makeSureListContainsEvents( eventsAfterSnapshotIncludingSnapshot, domainEventStream );
  }

  @Test
  public void itShouldSerializeRegularAndSnapshotEvents() throws Exception {
    final Map<String, List<DomainEventMessage>> typeToEventListMap = new HashMap<>();
    final Map<String, List<DomainEventMessage>> typeToSnapshotEventListMap = new HashMap<>();
    final List<DomainEventMessage> existingEventList = new ArrayList<>();
    final List<DomainEventMessage> existingSnapshotEventList = new ArrayList<>();
    typeToEventListMap.put( typeName, existingEventList );
    typeToSnapshotEventListMap.put( typeName, existingSnapshotEventList );

    final DomainEventMessage<StubDomainEvent> existingEvent = new GenericDomainEventMessage<>(
      aggregateIdentifier, 0, new StubDomainEvent( 0 ));
    existingEventList.add( existingEvent );
    existingEventList.addAll( createRegularEvents( 1, 2 ) );
    final DomainEventMessage snapshotEvent = createSnapshotEvent( 3 );
    existingSnapshotEventList.add( snapshotEvent );
    final List<DomainEventMessage> regularEventsAfterSnapshot = createRegularEvents( 4, 5 );
    existingEventList.addAll( regularEventsAfterSnapshot );

    final MemoryEventStoreWithFileSave eventStore = new MemoryEventStoreWithFileSave( typeToEventListMap,
      typeToSnapshotEventListMap) ;

    final DomainEventStream domainEventStream = eventStore.readEvents( typeName, aggregateIdentifier );

    final List<DomainEventMessage> eventsAfterSnapshotIncludingSnapshot = new ArrayList<>();
    eventsAfterSnapshotIncludingSnapshot.add( snapshotEvent );
    eventsAfterSnapshotIncludingSnapshot.addAll( regularEventsAfterSnapshot );

    makeSureListContainsEvents( eventsAfterSnapshotIncludingSnapshot, domainEventStream );
  }

  private DomainEventMessage createSnapshotEvent( final long sequenceNumber ) {
    return new GenericDomainEventMessage( aggregateIdentifier, sequenceNumber,
      new StubSnapshotDomainEvent( sequenceNumber ) );
  }

  private void createEventStream( final List<DomainEventMessage> events ) {
    eventStream = new SimpleDomainEventStream( events );
  }

  private void makeSureListContainsEvents( final Iterable<DomainEventMessage> expectedEvents,
    final List<DomainEventMessage> listToCheck ) {
    assertThat( listToCheck ).containsAll( expectedEvents );
  }

  private void makeSureListContainsEvents( final Iterable<DomainEventMessage> expectedEvents,
    final DomainEventStream domainEventStream ) {

    final List<DomainEventMessage> listToCheck = new ArrayList<>();
    while( domainEventStream.hasNext() ) { listToCheck.add( domainEventStream.next() ); }

    makeSureListContainsEvents( expectedEvents, listToCheck );
  }

}


