package org.loosefx.commands;

public class NoHandlerForCommandException extends RuntimeException {
    private final Object command;

    public NoHandlerForCommandException( Object command ) {
        super( String.format( "There was no handler for the following command class: %s. Command: %s",
            command.getClass().getName(), command.toString() ) );
        this.command = command;
    }

    public Object getCommand() {
        return command;
    }
}
