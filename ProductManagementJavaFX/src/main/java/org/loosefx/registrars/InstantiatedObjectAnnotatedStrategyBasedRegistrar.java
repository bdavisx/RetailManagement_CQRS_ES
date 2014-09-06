package org.loosefx.registrars;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/*
  we can't really register for each individual domain object, either for commands
  or events; technically we could, like Axon does, but not sure if we need that functionality.
  For events though, the Domain object will just use the apply to itself (which should put
  the event in the container somewhere in that call stack - then the save can save the container -
  if we go that route).

  so for commands, unless we hook into instances instead of singletons, something is going to
  have to handle putting the commands (and possibly events, if the domain object is interested
  in another aggregates events (can they be).

  I guess it might be better to create some kind of router that works with a repository to distribute
  the commands / events to the correct aggregate as needed.

  //====================================================

  Thoughts, the application is going to need a reference to the open presentations (or whatever),
  and in this app anyway, I don't see any commands needing to be routed to a closed presentation. Therefore
  we should be able to find the correct object to route the commands to. But I don't want to have to keep
  track of each individual object to send the commands to -- let's have an open aggregate cache that
  takes handles this automatically. This sort of takes out the need for the AnnotatedRegister class as
  far as aggregates go - but it could still be relevant for others.

*/
// TODO: figure out if we can hook into the creation of objects in the binder to connect


public class InstantiatedObjectAnnotatedStrategyBasedRegistrar {
    private final Class<? extends Annotation> annotationClass;

    public InstantiatedObjectAnnotatedStrategyBasedRegistrar( final Class<? extends Annotation> annotationClass ) {
        this.annotationClass = annotationClass;
    }

    public void registerAnnotatedHandlers( final Object objectToRegister,
        final BiConsumer<Class, Consumer> methodThatDoesRegistration ) {

        final Set<Method> methodsToRegister = findMethodsToRegister( objectToRegister );
        registerMethods( objectToRegister, methodThatDoesRegistration, methodsToRegister );
    }

    private Set<Method> findMethodsToRegister( final Object objectToRegister ) {
        final Set<Method> methodsToRegister = new HashSet<>();
        methodsToRegister.addAll( findAnnotatedMethods( objectToRegister ) );
        return methodsToRegister;
    }

    private Set<Method> findAnnotatedMethods( final Object objectToRegister ) {
        final Method[] declaredMethods = objectToRegister.getClass().getDeclaredMethods();
        Set<Method> methodsWithAnnotation = new HashSet<>();
        for( final Method method : declaredMethods ) {
            if( method.isAnnotationPresent( annotationClass ) ) {
                methodsWithAnnotation.add( method );
            }
        }
        return methodsWithAnnotation;
    }

    private void registerMethods( final Object objectToRegister,
        final BiConsumer<Class, Consumer> methodThatDoesRegistration, final Set<Method> methodsToRegister ) {

        for( final Method handlerMethod : methodsToRegister ) {
            registerMethod( objectToRegister, methodThatDoesRegistration, handlerMethod );
        }
    }

    private void registerMethod( final Object objectToRegister,
        final BiConsumer<Class, Consumer> methodThatDoesRegistration, final Method handlerMethod ) {

        final Class messageType = handlerMethod.getParameterTypes()[0];
        final HandlerHolder holder = new HandlerHolder( handlerMethod, objectToRegister );
        methodThatDoesRegistration.accept( messageType, holder.createSubscriber() );
    }

    private class HandlerHolder {
        private final Method handlerMethod;
        private final Object handlerObject;

        public HandlerHolder( final Method handlerMethod, final Object handlerObject ) {
            this.handlerMethod = handlerMethod;
            this.handlerObject = handlerObject;
        }

        private Consumer createSubscriber() {
            return o -> {
                try {
                    handlerMethod.setAccessible( true );
                    handlerMethod.invoke( handlerObject, o );
                } catch( IllegalAccessException | InvocationTargetException ex ) {
                    throw new UnableToInvokeAutoRegisteredMethodException( handlerMethod, handlerObject, ex );
                }
            };
        }
    }
}
