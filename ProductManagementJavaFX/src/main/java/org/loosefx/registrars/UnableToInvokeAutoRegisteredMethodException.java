package org.loosefx.registrars;

import java.lang.reflect.Method;

public class UnableToInvokeAutoRegisteredMethodException extends RuntimeException {
    private final Method handlerMethod;
    private final Object handlerObject;

    public UnableToInvokeAutoRegisteredMethodException( Method handlerMethod, Object handlerObject,
        Throwable cause ) {
        super( String.format( "Unable to Invoke Handler: Method: %s; Object: %s.", handlerMethod.toString(),
            handlerObject.toString() ), cause );
        this.handlerMethod = handlerMethod;
        this.handlerObject = handlerObject;
    }
}
