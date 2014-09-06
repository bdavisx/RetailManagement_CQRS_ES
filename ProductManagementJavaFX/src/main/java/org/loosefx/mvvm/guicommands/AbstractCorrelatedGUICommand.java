package org.loosefx.mvvm.guicommands;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class AbstractCorrelatedGUICommand {
    private final UUID correlationIdentifier;

    public AbstractCorrelatedGUICommand( final UUID identifier ) {
        checkNotNull( identifier );
        this.correlationIdentifier = identifier;
    }

    public UUID getCorrelationIdentifier() { return correlationIdentifier; }
}
