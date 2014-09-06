package org.loosefx.commands;

import org.loosefx.eventbus.EventSubscriber;
import org.loosefx.domain.commands.ApplicationCommandHandler;
import org.loosefx.mvvm.guicommands.GUICommandHandler;
import org.reflections.Reflections;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/** This class pretty much expects the annotated objects to be singletons in the injector. */
public class AnnotatedCommandHandlerRegistrar {
    private final BeanFactory beanFactory;
    private final CommandDistributor commandDistributor;
    private final Reflections reflections;

    public AnnotatedCommandHandlerRegistrar( BeanFactory beanFactory, CommandDistributor commandDistributor,
        Reflections reflections ) {
        this.beanFactory = beanFactory;
        this.commandDistributor = commandDistributor;
        this.reflections = reflections;
    }

    // TODO: figure out if we can hook into the creation of objects in the binder to connect

    public void registerAnnotatedHandlers() {
        Set<Method> methodsToRegister = findMethodsToRegister( reflections );
        registerMethods( methodsToRegister );
    }

    private Set<Method> findMethodsToRegister( Reflections reflections ) {
        Set<Method> methodsToRegister = new HashSet<>();
        methodsToRegister.addAll( findApplicationCommandHandlerMethods( reflections ) );
        methodsToRegister.addAll( findGUICommandHandlerMethods( reflections ) );
        return methodsToRegister;
    }

    private Set<Method> findApplicationCommandHandlerMethods( Reflections reflections ) {
        return reflections.getMethodsAnnotatedWith( ApplicationCommandHandler.class );
    }

    private Set<Method> findGUICommandHandlerMethods( Reflections reflections ) {
        return reflections.getMethodsAnnotatedWith( GUICommandHandler.class );
    }

    private void registerMethods( Set<Method> methodsToRegister ) {
        for( Method handlerMethod : methodsToRegister ) {
            registerMethod( handlerMethod );
        }
    }

    private void registerMethod( Method handlerMethod ) {
        Object handlerObject = beanFactory.getBean( handlerMethod.getDeclaringClass() );
        Class messageType = handlerMethod.getParameterTypes()[0];
        HandlerHolder holder = new HandlerHolder( handlerMethod, handlerObject );
        commandDistributor.register( messageType, holder.createSubscriber() );
    }

    private class HandlerHolder {
        private final Method handlerMethod;
        private final Object handlerObject;

        public HandlerHolder( Method handlerMethod, Object handlerObject ) {
            this.handlerMethod = handlerMethod;
            this.handlerObject = handlerObject;
        }

        private EventSubscriber createSubscriber() {
            return new EventSubscriber() {
                @Override
                public void onEvent( Object o ) {
                    try {
                        handlerMethod.invoke( handlerObject, o );
                    } catch( IllegalAccessException | InvocationTargetException ex ) {
                        throw new UnableToInvokeAutoRegisteredCommandHandlerException( handlerMethod,
                            handlerObject, ex );
                    }
                }
            };
        }
    }
}
