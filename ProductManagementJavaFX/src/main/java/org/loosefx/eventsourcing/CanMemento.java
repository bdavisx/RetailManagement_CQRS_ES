package org.loosefx.eventsourcing;

public interface CanMemento {
    Memento createMemento();
    <T extends Memento> void setMemento( T memento );
}
