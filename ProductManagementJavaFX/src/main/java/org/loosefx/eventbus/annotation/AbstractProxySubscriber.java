package org.loosefx.eventbus.annotation;

import org.loosefx.eventbus.EventService;
import org.loosefx.eventbus.Prioritized;
import org.loosefx.eventbus.ProxySubscriber;

import java.lang.ref.WeakReference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 Common base class for EventService Proxies.
 <p/>
 Implementing Prioritized even when Priority is not used is always OK.  The default value of 0 retains the FIFO
 order.
 */
public abstract class AbstractProxySubscriber implements ProxySubscriber, Prioritized {
    private Object proxiedSubscriber;
    private final Method subscriptionMethod;
    private final ReferenceStrength referenceStrength;
    private final EventService eventService;
    private final int priority;
    protected boolean veto;

    protected AbstractProxySubscriber( final Object proxiedSubscriber, final Method subscriptionMethod,
        final ReferenceStrength referenceStrength, final EventService es, final boolean veto ) {
        this( proxiedSubscriber, subscriptionMethod, referenceStrength, 0, es, veto );
    }

    protected AbstractProxySubscriber( final Object proxiedSubscriber, final Method subscriptionMethod,
        final ReferenceStrength referenceStrength, final int priority, final EventService es,
        final boolean veto ) {
        this.referenceStrength = referenceStrength;
        this.priority = priority;
        eventService = es;
        this.veto = veto;
        if( proxiedSubscriber == null ) {
            throw new IllegalArgumentException(
                "The realSubscriber cannot be null when constructing a proxy subscriber." );
        }
        if( subscriptionMethod == null ) {
            throw new IllegalArgumentException(
                "The subscriptionMethod cannot be null when constructing a proxy subscriber." );
        }
        final Class<?> returnType = subscriptionMethod.getReturnType();
        if( veto && returnType != Boolean.TYPE ) {
            throw new IllegalArgumentException(
                "The subscriptionMethod must have the two parameters, the first one must be a String and the second a non-primitive (Object or derivative)." );
        }
        if( ReferenceStrength.WEAK.equals( referenceStrength ) ) {
            this.proxiedSubscriber = new WeakReference( proxiedSubscriber );
        } else {
            this.proxiedSubscriber = proxiedSubscriber;
        }
        this.subscriptionMethod = subscriptionMethod;
    }

    /** @return the object this proxy is subscribed on behalf of */
    @Override
    public Object getProxiedSubscriber() {
        if( proxiedSubscriber instanceof WeakReference ) {
            return ((WeakReference) proxiedSubscriber).get();
        }
        return proxiedSubscriber;
    }

    /** @return the subscriptionMethod passed in the constructor */
    public Method getSubscriptionMethod() {
        return subscriptionMethod;
    }

    /** @return the EventService passed in the constructor */
    public EventService getEventService() {
        return eventService;
    }

    /** @return the ReferenceStrength passed in the constructor */
    @Override
    public ReferenceStrength getReferenceStrength() {
        return referenceStrength;
    }

    /**
     @return the priority, no effect if priority is 0 (the default value)
     */
    @Override
    public int getPriority() {
        return priority;
    }

    /**
     Called by EventServices to inform the proxy that it is unsubscribed. The ProxySubscriber should perform any
     necessary cleanup.
     <p/>
     <b>Overriding classes must call super.proxyUnsubscribed() or risk things not being cleanup up properly.</b>
     */
    @Override
    public void proxyUnsubscribed() {
        proxiedSubscriber = null;
    }

    @Override
    public final int hashCode() {
        throw new RuntimeException( "Proxy subscribers are not allowed in Hash " +
            "Maps, since the underlying values use Weak References that" +
            "may disappear, the calculations may not be the same in" +
            "successive calls as required by hashCode." );
    }

    protected boolean retryReflectiveCallUsingAccessibleObject( final Object[] args,
        final Method subscriptionMethod, final Object obj,
        final IllegalAccessException e, String message ) {
        boolean accessibleTriedAndFailed = false;
        if( subscriptionMethod != null ) {
            final AccessibleObject[] accessibleMethod = { subscriptionMethod };
            try {
                AccessibleObject.setAccessible( accessibleMethod, true );
                final Object returnValue = subscriptionMethod.invoke( obj, args );
                return Boolean.valueOf( returnValue + "" );
            } catch( final SecurityException ex ) {
                accessibleTriedAndFailed = true;
            } catch( final InvocationTargetException e1 ) {
                throw new RuntimeException( message, e );
            } catch( final IllegalAccessException e1 ) {
                throw new RuntimeException( message, e );
            }
        }
        if( accessibleTriedAndFailed ) {
            message = message +
                ".  An attempt was made to make the method accessible, but the SecurityManager denied the attempt.";
        }
        throw new RuntimeException( message, e );
    }

    @Override
    public boolean equals( final Object obj ) {
        if( obj instanceof AbstractProxySubscriber ) {
            final AbstractProxySubscriber bps = (AbstractProxySubscriber) obj;
            if( referenceStrength != bps.referenceStrength ) {
                return false;
            }
            if( subscriptionMethod != bps.subscriptionMethod ) {
                return false;
            }
            if( ReferenceStrength.WEAK == referenceStrength ) {
                if( ((WeakReference) proxiedSubscriber).get() != ((WeakReference) bps.proxiedSubscriber).get() ) {
                    return false;
                }
            } else {
                if( proxiedSubscriber != bps.proxiedSubscriber ) {
                    return false;
                }
            }
            if( veto != bps.veto ) {
                return false;
            }
            if( eventService != bps.eventService ) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "AbstractProxySubscriber{" +
            "realSubscriber=" + (proxiedSubscriber instanceof WeakReference ?
            ((WeakReference) proxiedSubscriber).get() : proxiedSubscriber) +
            ", subscriptionMethod=" + subscriptionMethod +
            ", veto=" + veto +
            ", referenceStrength=" + referenceStrength +
            ", eventService=" + eventService +
            '}';
    }
}
