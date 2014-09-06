package org.loosefx.eventbus.annotation;

import org.loosefx.eventbus.EventService;
import org.loosefx.eventbus.VetoTopicEventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 A class that subscribes to an EventService on behalf of another object. This class is not used directly (though you
 could), but rather through the use of the {@link @org.bushe.swing.event.annotation.EventTopicSubscriber}.  Advanced
 EventBus users could use this class in Aspect-Oriented code.  Consider using the {@link AnnotationProcessor}
 instead, it may suit your needs and be easier.
 */
public class ProxyTopicSubscriber extends AbstractProxySubscriber
    implements org.loosefx.eventbus.EventTopicSubscriber, VetoTopicEventListener {
    private final String topic;

    /**
     Creates a proxy.  This does not subscribe it.

     @param proxiedSubscriber the subscriber that the proxy will call when an event is published
     @param subscriptionMethod the method the proxy will call, must have an Object as it's first and only parameter
     @param referenceStrength if the subscription is weak, the reference from the proxy to the real subscriber
     should be too
     @param es the EventService we will be subscribed to, since we may need to unsubscribe when weak refs no longer
     exist
     @param topic the topic to subscribe to, used for unsubscription only
     @param veto if this proxy is for a veto subscriber
     */
    public ProxyTopicSubscriber( final Object proxiedSubscriber, final Method subscriptionMethod,
        final ReferenceStrength referenceStrength,
        final EventService es, final String topic, final boolean veto ) {
        this( proxiedSubscriber, subscriptionMethod, referenceStrength, 0, es, topic, veto );
    }

    /**
     Creates a proxy.  This does not subscribe it.

     @param proxiedSubscriber the subscriber that the proxy will call when an event is published
     @param subscriptionMethod the method the proxy will call, must have an Object as it's first and only parameter
     @param referenceStrength if the subscription is weak, the reference from the proxy to the real subscriber
     should be too
     @param es the EventService we will be subscribed to, since we may need to unsubscribe when weak refs no longer
     exist
     @param topic the topic to subscribe to, used for unsubscription only
     @param veto if this proxy is for a veto subscriber
     */
    public ProxyTopicSubscriber( final Object proxiedSubscriber, final Method subscriptionMethod,
        final ReferenceStrength referenceStrength,
        final int priority, final EventService es, final String topic, final boolean veto ) {
        super( proxiedSubscriber, subscriptionMethod, referenceStrength, priority, es, veto );
        this.topic = topic;
        if( topic == null ) {
            throw new IllegalArgumentException( "Proxies for topic subscribers require a non-null topic." );
        }
        final Class[] params = subscriptionMethod.getParameterTypes();
        if( params == null || params.length != 2 || !String.class.equals( params[0] ) ||
            params[1].isPrimitive() ) {
            throw new IllegalArgumentException(
                "The subscriptionMethod must have the two parameters, the first one must be a String and the second a non-primitive (Object or derivative)." );
        }
    }

    /**
     Handles the event publication by pushing it to the real subscriber's subscription Method.

     @param topic the topic on which the object is being published
     @param data The Object that is being published on the topic.
     */
    @Override
    public void onEvent( final String topic, final Object data ) {
        final Object[] args = new Object[]{ topic, data };
        Object obj = null;
        Method subscriptionMethod = null;
        try {
            obj = getProxiedSubscriber();
            if( obj == null ) {
                return;
            }
            subscriptionMethod = getSubscriptionMethod();
            subscriptionMethod.invoke( obj, args );
        } catch( final IllegalAccessException e ) {
            final String message =
                "IllegalAccessException when invoking annotated method from EventService publication.  Topic:" +
                    topic + ", data:" + data + ", subscriber:" + getProxiedSubscriber() +
                    ", subscription Method=" + getSubscriptionMethod();
            retryReflectiveCallUsingAccessibleObject( args, subscriptionMethod, obj, e, message );
        } catch( final InvocationTargetException e ) {
            throw new RuntimeException(
                "InvocationTargetException when invoking annotated method from EventService publication.  Topic:" +
                    topic + ", data:" + data + ", subscriber:" + getProxiedSubscriber() +
                    ", subscription Method=" + getSubscriptionMethod(), e );
        }
    }


    @Override
    public boolean shouldVeto( final String topic, final Object data ) {
        final Object[] args = new Object[]{ topic, data };
        Object obj = null;
        Method subscriptionMethod = null;
        try {
            obj = getProxiedSubscriber();
            if( obj == null ) {
                return false;
            }
            subscriptionMethod = getSubscriptionMethod();
            return Boolean.valueOf( subscriptionMethod.invoke( obj, args ) + "" );
        } catch( final IllegalAccessException e ) {
            final String message =
                "IllegalAccessException when invoking annotated veto method from EventService publication.  Topic:" +
                    topic + ", data:" + data + ", subscriber:" + getProxiedSubscriber() +
                    ", subscription Method=" + getSubscriptionMethod();
            return retryReflectiveCallUsingAccessibleObject( args, subscriptionMethod, obj, e, message );
        } catch( final InvocationTargetException e ) {
            throw new RuntimeException(
                "InvocationTargetException when invoking annotated veto method from EventService publication.  Topic:" +
                    topic + ", data:" + data + ", subscriber:" + getProxiedSubscriber() +
                    ", subscription Method=" + getSubscriptionMethod(), e );
        }
    }

    protected void unsubscribe( final String topic ) {
        if( veto ) {
            getEventService().unsubscribeVetoListener( topic, this );
        } else {
            getEventService().unsubscribe( topic, this );
        }
    }

    @Override
    public boolean equals( final Object obj ) {
        if( obj instanceof ProxyTopicSubscriber ) {
            if( !super.equals( obj ) ) {
                return false;
            }
            final ProxyTopicSubscriber proxyTopicSubscriber = (ProxyTopicSubscriber) obj;
            if( topic.equals( proxyTopicSubscriber.topic ) ) {
                if( topic == null ) {
                    return false;
                } else {
                    if( !topic.equals( proxyTopicSubscriber.topic ) ) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }


    @Override
    public String toString() {
        return "ProxyTopicSubscriber{" +
            "topic='" + topic + '\'' +
            "veto='" + veto + '\'' +
            "realSubscriber=" + getProxiedSubscriber() +
            ", subscriptionMethod=" + getSubscriptionMethod() +
            ", referenceStrength=" + getReferenceStrength() +
            ", eventService=" + getEventService() +
            '}';
    }
}
