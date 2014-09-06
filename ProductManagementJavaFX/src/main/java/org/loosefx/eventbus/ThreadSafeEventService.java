/**
 * Copyright 2005-2007 Bushe Enterprises, Inc., Hopkinton, MA, USA, www.bushe.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.loosefx.eventbus;

import org.loosefx.eventbus.Logger.Level;
import org.loosefx.eventbus.annotation.ReferenceStrength;
import org.loosefx.eventbus.exception.EventBusException;

import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

/**
 A thread-safe EventService implementation. <h2>Multithreading</h2>
 <p/>
 This implementation is <b>not Swing thread-safe</b>.  If publication occurs on a thread other than the Swing
 EventDispatchThread, subscribers will receive the event on the calling thread, and not the EDT.  Swing components
 should use the SwingEventService instead, which is the implementation used by the EventBus.
 <p/>
 Two threads may be accessing the ThreadSafeEventService at the same time, one unsubscribing a listener for topic
 "A" and the other publishing on topic "A".  If the unsubscribing thread gets the lock first, then it is
 unsubscribed, end of story.  If the publisher gets the lock first, then a snapshot copy of the current subscribers
 is made during the publication, the lock is released and the subscribers are called.  Between the time the lock is
 released and the time that the listener is called, the unsubscribing thread can unsubscribe, resulting in an
 unsubscribed object receiving notification of the event after it was unsubscribed (but just once).
 <p/>
 On event publication, subscribers are called in the order in which they subscribed.
 <p/>
 Events and/or topic data can be cached, but are not by default.  To cache events or topic data, call {@link
#setDefaultCacheSizePerClassOrTopic(int)}, {@link #setCacheSizeForEventClass(Class, int)}, or {@link
#setCacheSizeForTopic(String, int)}, {@link #setCacheSizeForTopic(Pattern, int)}.  Retrieve cached values with
 {@link #getLastEvent(Class)}, {@link #getLastTopicData(String)}, {@link #getCachedEvents(Class)}, or {@link
#getCachedTopicData(String)}.  Using caching while subscribing is most likely to make sense only if you subscribe
 and publish on the same thread (so caching is very useful for Swing applications since both happen on the EDT in a
 single-threaded manner). In multithreaded applications, you never know if your subscriber has handled an event
 while it was being subscribed (before the subscribe() method returned) that is newer or older than the retrieved
 cached value (taken before or after subscribe() respectively).
 <p/>
 To deal with subscribers that take too long (a concern in Swing applications), the EventService can be made to
 issue {@link SubscriberTimingEvent}s when subscribers exceed a certain time.  This does not interrupt subscriber
 processing and is published after the subscriber finishes.  The service can log a warning for
 SubscriberTimingEvents, see the constructor {@link ThreadSafeEventService (long, boolean)}.  The timing is checked
 for veto subscribers too.
 <p/>
 <h2>Logging</h2>
 <p/>
 All logging goes through the {@link Logger}.  The Logger is configurable and supports multiple logging systems.
 <p/>
 Exceptions are logged by default, override {@link #handleException(String, Object, String, Object, Throwable,
    StackTraceElement[], String)} to handleException exceptions in another way.  Each call to a subscriber is wrapped in
 a try block to ensure one listener does not interfere with another.
 <p/>
 <h2>Cleanup of Stale WeakReferences and Stale Annotation Proxies</h2>
 <p/>
 The EventService may need to clean up stale WeakReferences and ProxySubscribers created for EventBus annotations.
 (Aside: EventBus Annotations are handled by the creation of proxies to the annotated objects.  Since the
 annotations create weak references by default, annotation proxies must held strongly by the EventService, otherwise
 the proxy is garbage collected.)  When a WeakReference's referent or an ProxySubscriber's proxiedObject (the
 annotated object) is claimed by the garbage collector, the EventService still holds onto the actual WeakReference
 or ProxySubscriber subscribed to the EventService (which are pretty tiny).
 <p/>
 There are two ways that these stale WeakReferences and ProxySubscribers are cleaned up. <ol> <li>On every publish,
 subscribe and unsubscribe, every subscriber and veto subscriber to a class or topic is checked to see if it is a
 stale WeakReference or a stale ProxySubscriber (one whose getProxySubscriber() returns null).  If the subscriber is
 stale, it is unsubscribed from the EventService  immediately.  If it is a ProxySubscriber, it's
 proxyUnsubscribed() method is called after it is unsubscribed.  (This isn't as expensive as it sounds, since checks
 to avoid double subscription is necessary anyway). <li>Another cleanup thread may get started to clean up remaining
 stale subscribers.  This cleanup thread only comes into play for subscribers to topic or classes that haven't been
 used (published/subscribed/unsibscribed to).  A detailed description of the cleanup thread follows. </ol> <h3>The
 Cleanup Thread</h3> If a topic or class is never published to again, WeakReferences and ProxySubscribers can be
 left behind if they are not cleaned up.  To prevent loitering stale subscribers, the ThreadSafeEventService may
 periodically run through all the EventSubscribers and VetoSubscribers for all topics and classes and clean up stale
 proxies.  Proxies for Annotations that have a ReferenceStrength.STRONG are never cleaned up in normal usage.   (By
 specifying ReferenceStrength.STRONG, the programmer is buying into unsubscribing annotated objects themselves.
 There is one caveat: If getProxiedSubscriber() returns null, even for a ProxySubscriber with a STRONG reference
 strength, that proxy is cleaned up as it is assumed it is stale or just wrong.  This would not occur normally in
 EventBus usage, but only if someone is implementing their own custom ProxySubscriber and/or AnnotationProcessor.)
 <p/>
 Cleanup is pretty rare in general.  Not only are stale subscribers cleaned up with regular usage, stale subscribers
 on abandoned topics and classes do not take up a lot of memory, hence, they are allowed to build up to a certain
 degree. Cleanup does not occur until the number of WeakReferences and SubscriptionsProxy's with WeakReference
 strength subscribed to an EventService for all the EventService's subscriptions in total exceed the
 <tt>cleanupStartThreshhold</tt>, which is set to <tt>CLEANUP_START_THRESHOLD_DEFAULT</tt> (500) by default.  The
 default is overridable in the constructor or via #setCleanupStartThreshhold(Integer).  If set to null, cleanup will
 never start.
 <p/>
 Once the cleanup start threshold is exceeded, a <tt>java.util.Timer</tt> is started to clean up stale subscribers
 periodically in another thread.  The timer will fire every <tt>cleanupPeriodMS</tt> milliseconds, which is set to
 the <tt>CLEANUP_PERIOD_MS_DEFAULT<tt> (20 minutes) by default.  The default is overridable in the constructor or
 via #setCleanupPeriodMS(Integer).  If set to null, cleanup will not start.  This is implemented with a
 <tt>java.util.Timer</tt>, so Timer's warnings apply - setting this too low will cause cleanups to bunch up and hog
 the cleanup thread.
 <p/>
 After a cleanup cycle completes, if the number of stale subscribers falls at or below the
 <tt>cleanupStopThreshhold</tt> cleanup stops until the <tt>cleanupStartThreshhold</tt> is exceeded again. The
 <tt>cleanupStopThreshhold</tt> is set to <tt>CLEANUP_STOP_THRESHOLD_DEFAULT</tt> (100) by default.  The default is
 overridable in the constructor or via #setCleanupStopThreshhold(Integer).  If set to null or 0, cleanup will not
 stop if it is ever started.
 <p/>
 Cleanup can be monitored by subscribing to the {@link CleanupEvent} class.
 <p/>
 All cleanup parameters are tunable "live" and checked after each subscription and after each cleanup cycle. To make
 cleanup never run, set cleanupStartThreshhold to Integer.MAX_VALUE and cleanupPeriodMS to null. To get cleanup to
 run continuously, set set cleanupStartThreshhold to 0 and cleanupPeriodMS to some reasonable value, perhaps 1000 (1
 second) or so (not recommended, cleanup is conducted with regular usage and the cleanup thread is rarely created or
 invoked).
 <p/>
 Cleanup is not run in a daemon thread, and thus will not stop the JVM from exiting.
 <p/>

 @author Michael Bushe michael@bushe.com
 @todo (param) a JMS-like selector (can be done in base classes by implements like a commons filter
 @see EventService for a complete description of the API */
@SuppressWarnings({ "unchecked", "ForLoopReplaceableByForEach" })
public class ThreadSafeEventService implements EventService {
    public static final Integer CLEANUP_START_THRESHOLD_DEFAULT = 250;
    public static final Integer CLEANUP_STOP_THRESHOLD_DEFAULT = 100;
    public static final Long CLEANUP_PERIOD_MS_DEFAULT = 20L * 60L * 1000L;

    protected static final Logger LOG = Logger.getLogger( EventService.class.getName() );

    //Making these generic collections is a bad idea, it doesn't compile since it's better to have all the maps
    //go through the same set of code to do all the real publish and subscribe work
    private final Map subscribersByEventType = new HashMap();
    private final Map subscribersByEventClass = new HashMap();
    private final Map subscribersByExactEventClass = new HashMap();
    private final Map subscribersByTopic = new HashMap();
    private final Map subscribersByTopicPattern = new HashMap();
    private final Map vetoListenersByClass = new HashMap();
    private final Map vetoListenersByExactClass = new HashMap();
    private final Map vetoListenersByTopic = new HashMap();
    private final Map vetoListenersByTopicPattern = new HashMap();
    private final Object listenerLock = new Object();
    private final Object cacheLock = new Object();
    private final Long timeThresholdForEventTimingEventPublication;
    private final Map<Class, List> cacheByEvent = new HashMap<Class, List>();
    private int defaultCacheSizePerClassOrTopic = 0;
    private Map<Class, Integer> cacheSizesForEventClass;
    private Map<Class, Integer> rawCacheSizesForEventClass;
    private boolean rawCacheSizesForEventClassChanged;
    private final Map<String, List> cacheByTopic = new HashMap<String, List>();
    private Map<String, Integer> cacheSizesForTopic;
    private Map<String, Integer> rawCacheSizesForTopic;
    private boolean rawCacheSizesForTopicChanged;
    private Map<PatternWrapper, Integer> rawCacheSizesForPattern;
    private boolean rawCacheSizesForPatternChanged;
    private Integer cleanupStartThreshhold;
    private Integer cleanupStopThreshold;
    private Long cleanupPeriodMS;
    private int weakRefPlusProxySubscriberCount;
    private Timer cleanupTimer;
    private TimerTask cleanupTimerTask;
    private static final Comparator PRIORITIZED_SUBSCRIBER_COMPARATOR = new PrioritizedSubscriberComparator();
    private boolean hasEverUsedPrioritized;

    /** Creates a ThreadSafeEventService that does not monitor timing of handlers. */
    public ThreadSafeEventService() {
        this( null, false, null, null, null );
    }

    /**
     Creates a ThreadSafeEventService while providing time monitoring options.

     @param timeThresholdForEventTimingEventPublication the longest time a subscriber should spend handling an
     event, The service will publish an SubscriberTimingEvent after listener processing if the time was exceeded.
     If null, no EventSubscriberTimingEvent will be issued.
     */
    public ThreadSafeEventService( final Long timeThresholdForEventTimingEventPublication ) {
        this( timeThresholdForEventTimingEventPublication, false, null, null, null );
    }

    /**
     Creates a ThreadSafeEventService while providing time monitoring options.

     @param timeThresholdForEventTimingEventPublication the longest time a subscriber should spend handling an
     event, The service will publish an SubscriberTimingEvent after listener processing if the time was exceeded.
     If null, no EventSubscriberTimingEvent will be issued.
     @param subscribeTimingEventsInternally add a subscriber to the SubscriberTimingEvent internally and call the
     protected subscribeTiming() method when they occur.  This logs a warning to the {@link Logger} by default.
     */
    public ThreadSafeEventService( final Long timeThresholdForEventTimingEventPublication,
        final boolean subscribeTimingEventsInternally ) {
        this( timeThresholdForEventTimingEventPublication, subscribeTimingEventsInternally, null, null, null );
    }

    /**
     Creates a ThreadSafeEventService while providing proxy cleanup customization. Proxies are used with
     Annotations.

     @param cleanupStartThreshold see class javadoc.
     @param cleanupStopThreshold see class javadoc.
     @param cleanupPeriodMS see class javadoc.
     */
    public ThreadSafeEventService( final Integer cleanupStartThreshold,
        final Integer cleanupStopThreshold, final Long cleanupPeriodMS ) {
        this( null, false, cleanupStartThreshold,
            cleanupStopThreshold, cleanupPeriodMS );
    }

    /**
     Creates a ThreadSafeEventService while providing time monitoring options.

     @param timeThresholdForEventTimingEventPublication the longest time a subscriber should spend handling an
     event. The service will publish an SubscriberTimingEvent after listener processing if the time was exceeded.
     If null, no SubscriberTimingEvent will be issued.
     @param subscribeTimingEventsInternally add a subscriber to the SubscriberTimingEvent internally and call the
     protected subscribeTiming() method when they occur.  This logs a warning to the {@link Logger} by default.
     @param cleanupStartThreshold see class javadoc.
     @param cleanupStopThreshold see class javadoc.
     @param cleanupPeriodMS see class javadoc.

     @throws IllegalArgumentException if timeThresholdForEventTimingEventPublication is null and
     subscribeTimingEventsInternally is true.
     */
    public ThreadSafeEventService( final Long timeThresholdForEventTimingEventPublication,
        final boolean subscribeTimingEventsInternally, final Integer cleanupStartThreshold,
        final Integer cleanupStopThreshold, final Long cleanupPeriodMS ) {
        if( timeThresholdForEventTimingEventPublication == null && subscribeTimingEventsInternally ) {
            throw new IllegalArgumentException(
                "null, true in constructor is not valid.  If you want to send timing messages for all events and subscribe them internally, pass 0, true" );
        }
        this.timeThresholdForEventTimingEventPublication = timeThresholdForEventTimingEventPublication;
        if( subscribeTimingEventsInternally ) {
            //Listen to timing events and log them
            subscribeStrongly( SubscriberTimingEvent.class, new EventSubscriber() {
                @Override
                public void onEvent( final Object event ) {
                    subscribeTiming( (SubscriberTimingEvent) event );
                }
            } );
        }
        if( cleanupStartThreshold == null ) {
            this.cleanupStartThreshhold = CLEANUP_START_THRESHOLD_DEFAULT;
        } else {
            this.cleanupStartThreshhold = cleanupStartThreshold;
        }
        if( cleanupStopThreshold == null ) {
            this.cleanupStopThreshold = CLEANUP_STOP_THRESHOLD_DEFAULT;
        } else {
            this.cleanupStopThreshold = cleanupStopThreshold;
        }
        if( cleanupPeriodMS == null ) {
            this.cleanupPeriodMS = CLEANUP_PERIOD_MS_DEFAULT;
        } else {
            this.cleanupPeriodMS = cleanupPeriodMS;
        }
    }

    /**
     Gets the threshold above which cleanup starts.  See the class javadoc on cleanup.

     @return the threshold at which cleanup starts
     */
    public Integer getCleanupStartThreshhold() {
        synchronized( listenerLock ) {
            return cleanupStartThreshhold;
        }
    }

    /**
     Sets the threshold above which cleanup starts.  See the class javadoc on cleanup.

     @param cleanupStartThreshhold threshold at which cleanup starts
     */
    public void setCleanupStartThreshhold( final Integer cleanupStartThreshhold ) {
        synchronized( listenerLock ) {
            this.cleanupStartThreshhold = cleanupStartThreshhold;
        }
    }

    /**
     Gets the threshold below which cleanup stops.  See the class javadoc on cleanup.

     @return threshold at which cleanup stops (it may start again)
     */
    public Integer getCleanupStopThreshold() {
        synchronized( listenerLock ) {
            return cleanupStopThreshold;
        }
    }

    /**
     Sets the threshold below which cleanup stops.  See the class javadoc on cleanup.

     @param cleanupStopThreshold threshold at which cleanup stops (it may start again).
     */
    public void setCleanupStopThreshold( final Integer cleanupStopThreshold ) {
        synchronized( listenerLock ) {
            this.cleanupStopThreshold = cleanupStopThreshold;
        }
    }

    /**
     Get the cleanup interval. See the class javadoc on cleanup.

     @return interval in milliseconds between cleanup runs.
     */
    public Long getCleanupPeriodMS() {
        synchronized( listenerLock ) {
            return cleanupPeriodMS;
        }
    }

    /**
     Sets the cleanup interval. See the class javadoc on cleanup.

     @param cleanupPeriodMS interval in milliseconds between cleanup runs.  Passing null stops cleanup.
     */
    public void setCleanupPeriodMS( final Long cleanupPeriodMS ) {
        synchronized( listenerLock ) {
            this.cleanupPeriodMS = cleanupPeriodMS;
        }
    }

    /** @see EventService#subscribe(Class, EventSubscriber) */
    @Override
    public boolean subscribe( final Class cl, final EventSubscriber eh ) {
        if( cl == null ) {
            throw new IllegalArgumentException( "Event class must not be null" );
        }
        if( eh == null ) {
            throw new IllegalArgumentException( "Event subscriber must not be null" );
        }
        if( LOG.isLoggable( Level.DEBUG ) ) {
            LOG.debug( "Subscribing by class, class:" + cl + ", subscriber:" + eh );
        }
        return subscribe( cl, subscribersByEventClass, new WeakReference<EventSubscriber>( eh ) );
    }

    /** @see EventService#subscribe(java.lang.reflect.Type, EventSubscriber) */
    @Override
    public boolean subscribe( final Type type, final EventSubscriber eh ) {
        return subscribe( type, subscribersByEventType, new WeakReference<EventSubscriber>( eh ) );
    }

    /** @see EventService#subscribeExactly(Class, EventSubscriber) */
    @Override
    public boolean subscribeExactly( final Class cl, final EventSubscriber eh ) {
        if( cl == null ) {
            throw new IllegalArgumentException( "Event class must not be null" );
        }
        if( eh == null ) {
            throw new IllegalArgumentException( "Event subscriber must not be null" );
        }
        if( LOG.isLoggable( Level.DEBUG ) ) {
            LOG.debug( "Subscribing by class, class:" + cl + ", subscriber:" + eh );
        }
        return subscribe( cl, subscribersByExactEventClass, new WeakReference<EventSubscriber>( eh ) );
    }

    /** @see EventService#subscribe(String, EventTopicSubscriber) */
    @Override
    public boolean subscribe( final String topic, final EventTopicSubscriber eh ) {
        if( topic == null ) {
            throw new IllegalArgumentException( "Topic must not be null" );
        }
        if( eh == null ) {
            throw new IllegalArgumentException( "Event topic subscriber must not be null" );
        }
        if( LOG.isLoggable( Level.DEBUG ) ) {
            LOG.debug( "Subscribing by topic name, name:" + topic + ", subscriber:" + eh );
        }
        return subscribe( topic, subscribersByTopic, new WeakReference<EventTopicSubscriber>( eh ) );
    }

    /** @see EventService#subscribe(Pattern, EventTopicSubscriber) */
    @Override
    public boolean subscribe( final Pattern pat, final EventTopicSubscriber eh ) {
        if( pat == null ) {
            throw new IllegalArgumentException( "Pattern must not be null" );
        }
        if( eh == null ) {
            throw new IllegalArgumentException( "Event subscriber must not be null" );
        }
        if( LOG.isLoggable( Level.DEBUG ) ) {
            LOG.debug( "Subscribing by pattern, pattern:" + pat + ", subscriber:" + eh );
        }
        final PatternWrapper patternWrapper = new PatternWrapper( pat );
        return subscribe( patternWrapper, subscribersByTopicPattern,
            new WeakReference<EventTopicSubscriber>( eh ) );
    }

    /** @see EventService#subscribeStrongly(Class, EventSubscriber) */
    @Override
    public boolean subscribeStrongly( final Class cl, final EventSubscriber eh ) {
        if( LOG.isLoggable( Level.DEBUG ) ) {
            LOG.debug( "Subscribing weakly by class, class:" + cl + ", subscriber:" + eh );
        }
        if( eh == null ) {
            throw new IllegalArgumentException( "Subscriber cannot be null." );
        }
        return subscribe( cl, subscribersByEventClass, eh );
    }

    /** @see EventService#subscribeExactlyStrongly(Class, EventSubscriber) */
    @Override
    public boolean subscribeExactlyStrongly( final Class cl, final EventSubscriber eh ) {
        if( cl == null ) {
            throw new IllegalArgumentException( "Event class must not be null" );
        }
        if( eh == null ) {
            throw new IllegalArgumentException( "Event subscriber must not be null" );
        }
        if( LOG.isLoggable( Level.DEBUG ) ) {
            LOG.debug( "Subscribing by class, class:" + cl + ", subscriber:" + eh );
        }
        return subscribe( cl, subscribersByExactEventClass, eh );
    }

    /** @see EventService#subscribeStrongly(String, EventTopicSubscriber) */
    @Override
    public boolean subscribeStrongly( final String name, final EventTopicSubscriber eh ) {
        if( LOG.isLoggable( Level.DEBUG ) ) {
            LOG.debug( "Subscribing weakly by topic name, name:" + name + ", subscriber:" + eh );
        }
        if( eh == null ) {
            throw new IllegalArgumentException( "Subscriber cannot be null." );
        }
        return subscribe( name, subscribersByTopic, eh );
    }

    /** @see EventService#subscribeStrongly(Pattern, EventTopicSubscriber) */
    @Override
    public boolean subscribeStrongly( final Pattern pat, final EventTopicSubscriber eh ) {
        if( pat == null ) {
            throw new IllegalArgumentException( "Pattern must not be null" );
        }
        if( eh == null ) {
            throw new IllegalArgumentException( "Event subscriber must not be null" );
        }
        if( LOG.isLoggable( Level.DEBUG ) ) {
            LOG.debug( "Subscribing by pattern, pattern:" + pat + ", subscriber:" + eh );
        }
        final PatternWrapper patternWrapper = new PatternWrapper( pat );
        return subscribe( patternWrapper, subscribersByTopicPattern, eh );
    }


    /** @see org.loosefx.eventbus.EventService#clearAllSubscribers() */
    @Override
    public void clearAllSubscribers() {
        synchronized( listenerLock ) {
            unsubscribeAllInMap( subscribersByEventType );
            unsubscribeAllInMap( subscribersByEventClass );
            unsubscribeAllInMap( subscribersByExactEventClass );
            unsubscribeAllInMap( subscribersByTopic );
            unsubscribeAllInMap( subscribersByTopicPattern );
            unsubscribeAllInMap( vetoListenersByClass );
            unsubscribeAllInMap( vetoListenersByExactClass );
            unsubscribeAllInMap( vetoListenersByTopic );
            unsubscribeAllInMap( vetoListenersByTopicPattern );
        }
    }

    private void unsubscribeAllInMap( final Map subscriberMap ) {
        synchronized( listenerLock ) {
            final Set subscriptionKeys = subscriberMap.keySet();
            for( final Object key : subscriptionKeys ) {
                final List subscribers = (List) subscriberMap.get( key );
                while( !subscribers.isEmpty() ) {
                    unsubscribe( key, subscriberMap, subscribers.get( 0 ) );
                }
            }
        }
    }

    /** @see EventService#subscribeVetoListener(Class, VetoEventListener) */
    @Override
    public boolean subscribeVetoListener( final Class eventClass, final VetoEventListener vetoListener ) {
        if( vetoListener == null ) {
            throw new IllegalArgumentException( "VetoEventListener cannot be null." );
        }
        if( eventClass == null ) {
            throw new IllegalArgumentException( "eventClass cannot be null." );
        }
        return subscribeVetoListener( eventClass, vetoListenersByClass,
            new WeakReference<VetoEventListener>( vetoListener ) );
    }

    /** @see EventService#subscribeVetoListenerExactly(Class, VetoEventListener) */
    @Override
    public boolean subscribeVetoListenerExactly( final Class eventClass, final VetoEventListener vetoListener ) {
        if( vetoListener == null ) {
            throw new IllegalArgumentException( "VetoEventListener cannot be null." );
        }
        if( eventClass == null ) {
            throw new IllegalArgumentException( "eventClass cannot be null." );
        }
        return subscribeVetoListener( eventClass, vetoListenersByExactClass,
            new WeakReference<VetoEventListener>( vetoListener ) );
    }

    /** @see EventService#subscribeVetoListener(String, VetoTopicEventListener) */
    @Override
    public boolean subscribeVetoListener( final String topic, final VetoTopicEventListener vetoListener ) {
        if( vetoListener == null ) {
            throw new IllegalArgumentException( "VetoEventListener cannot be null." );
        }
        if( topic == null ) {
            throw new IllegalArgumentException( "topic cannot be null." );
        }
        return subscribeVetoListener( topic, vetoListenersByTopic,
            new WeakReference<VetoTopicEventListener>( vetoListener ) );
    }

    /** @see EventService#subscribeVetoListener(Pattern, VetoTopicEventListener) */
    @Override
    public boolean subscribeVetoListener( final Pattern topicPattern, final VetoTopicEventListener vetoListener ) {
        if( vetoListener == null ) {
            throw new IllegalArgumentException( "VetoEventListener cannot be null." );
        }
        if( topicPattern == null ) {
            throw new IllegalArgumentException( "topicPattern cannot be null." );
        }
        final PatternWrapper patternWrapper = new PatternWrapper( topicPattern );
        return subscribeVetoListener( patternWrapper, vetoListenersByTopicPattern,
            new WeakReference<VetoTopicEventListener>( vetoListener ) );
    }

    /** @see EventService#subscribeVetoListenerStrongly(Class, VetoEventListener) */
    @Override
    public boolean subscribeVetoListenerStrongly( final Class eventClass, final VetoEventListener vetoListener ) {
        if( vetoListener == null ) {
            throw new IllegalArgumentException( "VetoEventListener cannot be null." );
        }
        if( eventClass == null ) {
            throw new IllegalArgumentException( "eventClass cannot be null." );
        }
        return subscribeVetoListener( eventClass, vetoListenersByClass, vetoListener );
    }

    /** @see EventService#subscribeVetoListenerExactlyStrongly(Class, VetoEventListener) */
    @Override
    public boolean subscribeVetoListenerExactlyStrongly( final Class eventClass,
        final VetoEventListener vetoListener ) {
        if( vetoListener == null ) {
            throw new IllegalArgumentException( "VetoEventListener cannot be null." );
        }
        if( eventClass == null ) {
            throw new IllegalArgumentException( "eventClass cannot be null." );
        }
        return subscribeVetoListener( eventClass, vetoListenersByExactClass, vetoListener );
    }

    /** @see EventService#subscribeVetoListenerStrongly(String, VetoTopicEventListener) */
    @Override
    public boolean subscribeVetoListenerStrongly( final String topic, final VetoTopicEventListener vetoListener ) {
        if( vetoListener == null ) {
            throw new IllegalArgumentException( "VetoListener cannot be null." );
        }
        if( topic == null ) {
            throw new IllegalArgumentException( "topic cannot be null." );
        }
        return subscribeVetoListener( topic, vetoListenersByTopic, vetoListener );
    }

    /** @see EventService#subscribeVetoListenerStrongly(Pattern, VetoTopicEventListener) */
    @Override
    public boolean subscribeVetoListenerStrongly( final Pattern topicPattern,
        final VetoTopicEventListener vetoListener ) {
        if( vetoListener == null ) {
            throw new IllegalArgumentException( "VetoTopicEventListener cannot be null." );
        }
        if( topicPattern == null ) {
            throw new IllegalArgumentException( "topicPattern cannot be null." );
        }
        final PatternWrapper patternWrapper = new PatternWrapper( topicPattern );
        return subscribeVetoListener( patternWrapper, vetoListenersByTopicPattern, vetoListener );
    }

    /**
     All veto subscriptions methods call this method.  Extending classes only have to override this method to
     subscribe all veto subscriptions.

     @param subscription the topic, Pattern, or event class to subscribe to
     @param vetoListenerMap the internal map of veto listeners to use (by topic of class)
     @param vetoListener the veto listener to subscribe, may be a VetoEventListener or a WeakReference to one

     @return boolean if the veto listener is subscribed (was not subscribed).

     @throws IllegalArgumentException if vl or o is null
     */
    protected boolean subscribeVetoListener( final Object subscription, final Map vetoListenerMap,
        final Object vetoListener ) {
        if( LOG.isLoggable( Level.DEBUG ) ) {
            LOG.debug( "subscribeVetoListener(" + subscription + "," + vetoListener + ")" );
        }
        if( vetoListener == null ) {
            throw new IllegalArgumentException( "Can't subscribe null veto listener to " + subscription );
        }
        if( subscription == null ) {
            throw new IllegalArgumentException( "Can't subscribe veto listener to null." );
        }
        return subscribe( subscription, vetoListenerMap, vetoListener );
    }

    /**
     All subscribe methods call this method, including veto subscriptions. Extending classes only have to override
     this method to subscribe all subscriber subscriptions.
     <p/>
     Overriding this method is only for the adventurous.  This basically gives you just enough rope to hang
     yourself.

     @param classTopicOrPatternWrapper the topic String, event Class, or PatternWrapper to subscribe to
     @param subscriberMap the internal map of subscribers to use (by topic or class)
     @param subscriber the EventSubscriber or EventTopicSubscriber to subscribe, or a WeakReference to either

     @return boolean if the subscriber is subscribed (was not subscribed).

     @throws IllegalArgumentException if subscriber or topicOrClass is null
     */
    protected boolean subscribe( final Object classTopicOrPatternWrapper, final Map<Object, Object> subscriberMap,
        final Object subscriber ) {
        if( classTopicOrPatternWrapper == null ) {
            throw new IllegalArgumentException( "Can't subscribe to null." );
        }
        if( subscriber == null ) {
            throw new IllegalArgumentException(
                "Can't subscribe null subscriber to " + classTopicOrPatternWrapper );
        }
        boolean alreadyExists = false;

        //Find the real subscriber underlying weak refs and proxies
        Object realSubscriber = subscriber;
        final boolean isWeakRef = subscriber instanceof WeakReference;
        if( isWeakRef ) {
            realSubscriber = ((WeakReference) subscriber).get();
        }
        if( realSubscriber instanceof Prioritized ) {
            hasEverUsedPrioritized = true;
        }
        boolean isWeakProxySubscriber = false;
        if( subscriber instanceof ProxySubscriber ) {
            final ProxySubscriber proxySubscriber = (ProxySubscriber) subscriber;
            if( proxySubscriber instanceof Prioritized ) {
                hasEverUsedPrioritized = true;
            }
            isWeakProxySubscriber = proxySubscriber.getReferenceStrength() == ReferenceStrength.WEAK;
            if( isWeakProxySubscriber ) {
                realSubscriber = ((ProxySubscriber) subscriber).getProxiedSubscriber();
            }
        }
        if( isWeakRef && isWeakProxySubscriber ) {
            throw new IllegalArgumentException( "ProxySubscribers should always be subscribed strongly." );
        }
        if( realSubscriber == null ) {
            return false;//already garbage collected?  Weird.
        }
        synchronized( listenerLock ) {
            List currentSubscribers = (List) subscriberMap.get( classTopicOrPatternWrapper );
            if( currentSubscribers == null ) {
                if( LOG.isLoggable( Level.DEBUG ) ) {
                    LOG.debug( "Creating new subscriber map for:" + classTopicOrPatternWrapper );
                }
                currentSubscribers = new ArrayList();
                subscriberMap.put( classTopicOrPatternWrapper, currentSubscribers );
            } else {
                //Double subscription check and stale subscriber cleanup
                //Need to compare the underlying referents for WeakReferences and ProxySubscribers
                //to make sure a weak ref and a hard ref aren't both subscribed
                //to the same topic and object.
                //Use the proxied subscriber for comparison if a ProxySubscribers is used
                //Subscribing the same object by proxy and subscribing explicitly should
                //not subscribe the same object twice
                for( final Iterator iterator = currentSubscribers.iterator(); iterator.hasNext(); ) {
                    final Object currentSubscriber = iterator.next();
                    final Object realCurrentSubscriber =
                        getRealSubscriberAndCleanStaleSubscriberIfNecessary( iterator, currentSubscriber );
                    if( realSubscriber.equals( realCurrentSubscriber ) ) {
                        //Already subscribed.
                        //Remove temporarily, to add to the end of the calling list
                        iterator.remove();
                        alreadyExists = true;
                    }
                }
            }
            currentSubscribers.add( subscriber );
            if( isWeakProxySubscriber || isWeakRef ) {
                incWeakRefPlusProxySubscriberCount();
            }
            return !alreadyExists;
        }
    }

    /** @see EventService#unsubscribe(Class, EventSubscriber) */
    @Override
    public boolean unsubscribe( final Class cl, final EventSubscriber eh ) {
        return unsubscribe( cl, subscribersByEventClass, eh );
    }

    /** @see EventService#unsubscribeExactly(Class, EventSubscriber) */
    @Override
    public boolean unsubscribeExactly( final Class cl, final EventSubscriber eh ) {
        return unsubscribe( cl, subscribersByExactEventClass, eh );
    }

    /** @see EventService#unsubscribe(String, EventTopicSubscriber) */
    @Override
    public boolean unsubscribe( final String name, final EventTopicSubscriber eh ) {
        return unsubscribe( name, subscribersByTopic, eh );
    }

    /** @see EventService#unsubscribe(String, EventTopicSubscriber) */
    @Override
    public boolean unsubscribe( final Pattern topicPattern, final EventTopicSubscriber eh ) {
        final PatternWrapper patternWrapper = new PatternWrapper( topicPattern );
        return unsubscribe( patternWrapper, subscribersByTopicPattern, eh );
    }

    /** @see EventService#unsubscribe(Class, Object) */
    @Override
    public boolean unsubscribe( final Class eventClass, final Object subscribedByProxy ) {
        final EventSubscriber subscriber = (EventSubscriber) getProxySubscriber( eventClass, subscribedByProxy );
        if( subscriber == null ) {
            return false;
        } else {
            return unsubscribe( eventClass, subscriber );
        }
    }

    /** @see EventService#unsubscribeExactly(Class, Object) */
    @Override
    public boolean unsubscribeExactly( final Class eventClass, final Object subscribedByProxy ) {
        final EventSubscriber subscriber = (EventSubscriber) getProxySubscriber( eventClass, subscribedByProxy );
        if( subscriber == null ) {
            return false;
        } else {
            return unsubscribeExactly( eventClass, subscriber );
        }
    }

    /** @see EventService#unsubscribe(String, Object) */
    @Override
    public boolean unsubscribe( final String topic, final Object subscribedByProxy ) {
        final EventTopicSubscriber subscriber =
            (EventTopicSubscriber) getProxySubscriber( topic, subscribedByProxy );
        if( subscriber == null ) {
            return false;
        } else {
            return unsubscribe( topic, subscriber );
        }
    }

    /** @see EventService#unsubscribe(java.util.regex.Pattern, Object) */
    @Override
    public boolean unsubscribe( final Pattern pattern, final Object subscribedByProxy ) {
        final EventTopicSubscriber subscriber =
            (EventTopicSubscriber) getProxySubscriber( pattern, subscribedByProxy );
        if( subscriber == null ) {
            return false;
        } else {
            return unsubscribe( pattern, subscriber );
        }
    }

    /**
     All event subscriber unsubscriptions call this method.  Extending classes only have to override this method to
     subscribe all subscriber unsubscriptions.

     @param o the topic or event class to unsubscribe from
     @param subscriberMap the map of subscribers to use (by topic of class)
     @param subscriber the subscriber to unsubscribe, either an EventSubscriber or an EventTopicSubscriber, or a
     WeakReference to either

     @return boolean if the subscriber is unsubscribed (was subscribed).
     */
    protected boolean unsubscribe( final Object o, final Map subscriberMap, final Object subscriber ) {
        if( LOG.isLoggable( Level.DEBUG ) ) {
            LOG.debug( "unsubscribe(" + o + "," + subscriber + ")" );
        }
        if( o == null ) {
            throw new IllegalArgumentException( "Can't unsubscribe to null." );
        }
        if( subscriber == null ) {
            throw new IllegalArgumentException( "Can't unsubscribe null subscriber to " + o );
        }
        synchronized( listenerLock ) {
            return removeFromSetResolveWeakReferences( subscriberMap, o, subscriber );
        }
    }

    /** @see EventService#unsubscribeVeto(Class, Object) */
    @Override
    public boolean unsubscribeVeto( final Class eventClass, final Object subscribedByProxy ) {
        final VetoEventListener subscriber =
            (VetoEventListener) getVetoProxySubscriber( eventClass, subscribedByProxy );
        if( subscriber == null ) {
            return false;
        } else {
            return unsubscribeVetoListener( eventClass, subscriber );
        }
    }

    /** @see EventService#unsubscribeVetoExactly(Class, Object) */
    @Override
    public boolean unsubscribeVetoExactly( final Class eventClass, final Object subscribedByProxy ) {
        final VetoEventListener subscriber =
            (VetoEventListener) getVetoProxySubscriber( eventClass, subscribedByProxy );
        if( subscriber == null ) {
            return false;
        } else {
            return unsubscribeVetoListenerExactly( eventClass, subscriber );
        }
    }

    /** @see EventService#unsubscribeVeto(String, Object) */
    @Override
    public boolean unsubscribeVeto( final String topic, final Object subscribedByProxy ) {
        final VetoTopicEventListener subscriber =
            (VetoTopicEventListener) getVetoProxySubscriber( topic, subscribedByProxy );
        if( subscriber == null ) {
            return false;
        } else {
            return unsubscribeVetoListener( topic, subscriber );
        }
    }

    /** @see EventService#unsubscribeVeto(java.util.regex.Pattern, Object) */
    @Override
    public boolean unsubscribeVeto( final Pattern pattern, final Object subscribedByProxy ) {
        final VetoTopicEventListener subscriber =
            (VetoTopicEventListener) getVetoProxySubscriber( pattern, subscribedByProxy );
        if( subscriber == null ) {
            return false;
        } else {
            return unsubscribeVetoListener( pattern, subscriber );
        }
    }

    /** @see EventService#unsubscribeVetoListener(Class, VetoEventListener) */
    @Override
    public boolean unsubscribeVetoListener( final Class eventClass, final VetoEventListener vetoListener ) {
        return unsubscribeVetoListener( eventClass, vetoListenersByClass, vetoListener );
    }

    /** @see EventService#unsubscribeVetoListenerExactly(Class, VetoEventListener) */
    @Override
    public boolean unsubscribeVetoListenerExactly( final Class eventClass, final VetoEventListener vetoListener ) {
        return unsubscribeVetoListener( eventClass, vetoListenersByExactClass, vetoListener );
    }

    /** @see EventService#unsubscribeVetoListener(String, VetoTopicEventListener) */
    @Override
    public boolean unsubscribeVetoListener( final String topic, final VetoTopicEventListener vetoListener ) {
        return unsubscribeVetoListener( topic, vetoListenersByTopic, vetoListener );
    }

    /** @see EventService#unsubscribeVetoListener(Pattern, VetoTopicEventListener) */
    @Override
    public boolean unsubscribeVetoListener( final Pattern topicPattern,
        final VetoTopicEventListener vetoListener ) {
        final PatternWrapper patternWrapper = new PatternWrapper( topicPattern );
        return unsubscribeVetoListener( patternWrapper, vetoListenersByTopicPattern, vetoListener );
    }

    /**
     All veto unsubscriptions methods call this method.  Extending classes only have to override this method to
     subscribe all veto unsubscriptions.

     @param o the topic or event class to unsubscribe from
     @param vetoListenerMap the map of veto listeners to use (by topic or class)
     @param vl the veto listener to unsubscribe, or a WeakReference to one

     @return boolean if the veto listener is unsubscribed (was subscribed).
     */
    protected boolean unsubscribeVetoListener( final Object o, final Map vetoListenerMap, final Object vl ) {
        if( LOG.isLoggable( Level.DEBUG ) ) {
            LOG.debug( "unsubscribeVetoListener(" + o + "," + vl + ")" );
        }
        if( o == null ) {
            throw new IllegalArgumentException( "Can't unsubscribe veto listener to null." );
        }
        if( vl == null ) {
            throw new IllegalArgumentException( "Can't unsubscribe null veto listener to " + o );
        }
        synchronized( listenerLock ) {
            return removeFromSetResolveWeakReferences( vetoListenerMap, o, vl );
        }
    }

    private ProxySubscriber getProxySubscriber( final Class eventClass, final Object subscribedByProxy ) {
        final List subscribers = getSubscribers( eventClass );
        return getProxySubscriber( subscribers, subscribedByProxy );
    }

    private ProxySubscriber getProxySubscriber( final String topic, final Object subscribedByProxy ) {
        final List subscribers = getSubscribers( topic );
        return getProxySubscriber( subscribers, subscribedByProxy );
    }

    private ProxySubscriber getProxySubscriber( final Pattern pattern, final Object subscribedByProxy ) {
        final List subscribers = getSubscribersToPattern( pattern );
        return getProxySubscriber( subscribers, subscribedByProxy );
    }

    private ProxySubscriber getVetoProxySubscriber( final Class eventClass, final Object subscribedByProxy ) {
        final List subscribers = getVetoSubscribers( eventClass );
        return getProxySubscriber( subscribers, subscribedByProxy );
    }

    private ProxySubscriber getVetoProxySubscriber( final String topic, final Object subscribedByProxy ) {
        final List subscribers = getVetoSubscribers( topic );
        return getProxySubscriber( subscribers, subscribedByProxy );
    }

    private ProxySubscriber getVetoProxySubscriber( final Pattern pattern, final Object subscribedByProxy ) {
        final List subscribers = getVetoSubscribers( pattern );
        return getProxySubscriber( subscribers, subscribedByProxy );
    }

    private ProxySubscriber getProxySubscriber( final List subscribers, final Object subscribedByProxy ) {
        for( final Iterator iter = subscribers.iterator(); iter.hasNext(); ) {
            Object subscriber = iter.next();
            if( subscriber instanceof WeakReference ) {
                final WeakReference wr = (WeakReference) subscriber;
                subscriber = wr.get();
            }
            if( subscriber instanceof ProxySubscriber ) {
                final ProxySubscriber proxy = (ProxySubscriber) subscriber;
                subscriber = proxy.getProxiedSubscriber();
                if( subscriber == subscribedByProxy ) {
                    return proxy;
                }
            }
        }
        return null;
    }

    /** @see EventService#publish(Object) */
    @Override
    public void publish( final Object event ) {
        if( event == null ) {
            throw new IllegalArgumentException( "Cannot publish null event." );
        }
        publish( event, null, null, getSubscribers( event.getClass() ), getVetoSubscribers( event.getClass() ),
            null );
    }

    /** @see EventService#publish(java.lang.reflect.Type, Object) */
    @Override
    public void publish( final Type genericType, final Object event ) {
        if( genericType == null ) {
            throw new IllegalArgumentException( "genericType must not be null." );
        }
        if( event == null ) {
            throw new IllegalArgumentException( "Cannot publish null event." );
        }
        publish( event, null, null, getSubscribers( genericType ), null/*getVetoSubscribers(genericType)*/, null );
    }

    /** @see EventService#publish(String, Object) */
    @Override
    public void publish( final String topicName, final Object eventObj ) {
        publish( null, topicName, eventObj, getSubscribers( topicName ), getVetoEventListeners( topicName ),
            null );
    }

    /**
     All publish methods call this method.  Extending classes only have to override this method to handle all
     publishing cases.

     @param event the event to publish, null if publishing on a topic
     @param topic if publishing on a topic, the topic to publish on, else null
     @param eventObj if publishing on a topic, the eventObj to publish, else null
     @param subscribers the subscribers to publish to - must be a snapshot copy
     @param vetoSubscribers the veto subscribers to publish to - must be a snapshot copy.
     @param callingStack the stack that called this publication, helpful for reporting errors on other threads

     @throws IllegalArgumentException if eh or o is null
     */
    protected void publish( final Object event, final String topic, final Object eventObj,
        final List subscribers, final List vetoSubscribers, final StackTraceElement[] callingStack ) {

        if( event == null && topic == null ) {
            throw new IllegalArgumentException( "Can't publish to null topic/event." );
        }

        setStatus( PublicationStatus.Initiated, event, topic, eventObj );
        //topic or event
        logEvent( event, topic, eventObj );

        //Check all veto subscribers, if any veto, then don't publish or cache
        if( checkVetoSubscribers( event, topic, eventObj, vetoSubscribers, callingStack ) ) {
            setStatus( PublicationStatus.Vetoed, event, topic, eventObj );
            return;
        } else {
            setStatus( PublicationStatus.Queued, event, topic, eventObj );
        }

        addEventToCache( event, topic, eventObj );

        if( subscribers == null || subscribers.isEmpty() ) {
            if( LOG.isLoggable( Level.DEBUG ) ) {
                LOG.debug( "No subscribers for event or topic. Event:" + event + ", Topic:" + topic );
            }
        } else {
            if( LOG.isLoggable( Level.DEBUG ) ) {
                LOG.debug( "Publishing to subscribers:" + subscribers );
            }
            setStatus( PublicationStatus.Publishing, event, topic, eventObj );
            for( int i = 0; i < subscribers.size(); i++ ) {
                final Object eh = subscribers.get( i );
                if( event != null ) {
                    final EventSubscriber eventSubscriber = (EventSubscriber) eh;
                    final long start = System.currentTimeMillis();
                    try {
                        eventSubscriber.onEvent( event );
                        checkTimeLimit( start, event, eventSubscriber, null );
                    } catch( final Throwable e ) {
                        checkTimeLimit( start, event, eventSubscriber, null );
                        handleException( event, e, callingStack, eventSubscriber );
                    }
                } else {
                    final EventTopicSubscriber eventTopicSubscriber = (EventTopicSubscriber) eh;
                    try {
                        eventTopicSubscriber.onEvent( topic, eventObj );
                    } catch( final Throwable e ) {
                        onEventException( topic, eventObj, e, callingStack, eventTopicSubscriber );
                    }
                }
            }
        }
        setStatus( PublicationStatus.Completed, event, topic, eventObj );
    }

    /**
     Called during publication to set the status on an event.  Can be used by subclasses to be notified when an
     event transitions from one state to another.  Implementers are required to call setPublicationStatus

     @param status the status to set on the object
     @param event the event being published, will be null if topic is not null
     @param topic the topic eventObj is being published on, will be null if event is not null
     @param eventObj the payload being published on the topic , will be null if event is not null
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    protected void setStatus( final PublicationStatus status, final Object event, final String topic,
        final Object eventObj ) {
        if( event instanceof PublicationStatusTracker ) {
            ((PublicationStatusTracker) event).setPublicationStatus( status );
        }
        if( eventObj instanceof PublicationStatusTracker ) {
            ((PublicationStatusTracker) eventObj).setPublicationStatus( status );
        }
    }

    /**
     Handles subscribers that are Prioritized by putting the most negative prioritized subscribers first, the most
     positive prioritized subscribers last, and leaving non-Prioritized in their original FIFO order.

     @param subscribers the subscribers to sort

     @return the same list if there are no prioritized subscribers in the list, otherwise a new sorted result
     */
    private List sortSubscribers( final List subscribers ) {
        if( subscribers == null ) {
            return null;
        }
        List<Prioritized> prioritizedSubscribers = null;
        final Iterator iterator = subscribers.iterator();
        while( iterator.hasNext() ) {
            final Object subscriber = iterator.next();
            if( subscriber instanceof Prioritized ) {
                final Prioritized prioritized = ((Prioritized) subscriber);
                if( prioritized.getPriority() != 0 ) {
                    iterator.remove();
                    if( prioritizedSubscribers == null ) {
                        prioritizedSubscribers = new ArrayList<Prioritized>();
                    }
                    prioritizedSubscribers.add( prioritized );
                }
            }
        }
        if( prioritizedSubscribers == null ) {
            return subscribers;
        } else {
            final List result = new ArrayList( prioritizedSubscribers.size() + subscribers.size() );
            Collections.sort( prioritizedSubscribers, PRIORITIZED_SUBSCRIBER_COMPARATOR );
            boolean haveAddedFIFOSubscribers = false;
            for( final Prioritized prioritizedSubscriber : prioritizedSubscribers ) {
                if( prioritizedSubscriber.getPriority() > 0 && !haveAddedFIFOSubscribers ) {
                    for( final Object subscriber : subscribers ) {
                        result.add( subscriber );
                    }
                    haveAddedFIFOSubscribers = true;
                }
                result.add( prioritizedSubscriber );
            }
            //Issue 26 - of all priorities are negative, then add the FIFO after processing all of them
            if( !haveAddedFIFOSubscribers ) {
                for( final Object subscriber : subscribers ) {
                    result.add( subscriber );
                }
            }
            return result;
        }
    }

    private boolean checkVetoSubscribers( final Object event, final String topic, final Object eventObj,
        final List vetoSubscribers,
        final StackTraceElement[] callingStack ) {
        if( vetoSubscribers != null && !vetoSubscribers.isEmpty() ) {
            for( final Iterator vlIter = vetoSubscribers.iterator(); vlIter.hasNext(); ) {
                final Object vetoer = vlIter.next();
                VetoEventListener vl = null;
                VetoTopicEventListener vtl = null;
                if( event == null ) {
                    vtl = (VetoTopicEventListener) vetoer;
                } else {
                    vl = (VetoEventListener) vetoer;
                }
                final long start = System.currentTimeMillis();
                try {
                    boolean shouldVeto = false;
                    if( event == null ) {
                        shouldVeto = vtl.shouldVeto( topic, eventObj );
                    } else {
                        shouldVeto = vl.shouldVeto( event );
                    }
                    if( shouldVeto ) {
                        handleVeto( vl, event, vtl, topic, eventObj );
                        checkTimeLimit( start, event, null, vl );
                        if( LOG.isLoggable( Level.DEBUG ) ) {
                            LOG.debug(
                                "Publication vetoed. Event:" + event + ", Topic:" + topic + ", veto subscriber:" +
                                    vl );
                        }
                        return true;
                    }
                } catch( final Throwable ex ) {
                    checkTimeLimit( start, event, null, vl );
                    subscribeVetoException( event, topic, eventObj, ex, callingStack, vl );
                }
            }
        }
        return false;
    }

    private void logEvent( final Object event, final String topic, final Object eventObj ) {
        if( LOG.isLoggable( Level.DEBUG ) ) {
            if( event != null ) {
                LOG.debug( "Publishing event: class=" + event.getClass() + ", event=" + event );
            } else if( topic != null ) {
                LOG.debug( "Publishing event: topic=" + topic + ", eventObj=" + eventObj );
            }
        }
    }

    /**
     Adds an event to the event cache, if appropriate.  This method is called just before publication to listeners,
     after the event passes any veto listeners.
     <p/>
     Using protected visibility to open the caching to other implementations.

     @param event the event about to be published, null if topic is non-null
     @param topic the topic about to be published to, null if the event is non-null
     @param eventObj the eventObj about to be published on a topic, null if the event is non-null
     */
    protected void addEventToCache( final Object event, final String topic, final Object eventObj ) {
        //Taking the listener lock here, since a listener that is now subscribing will want
        //this event since they are not in this subscriber list.
        synchronized( listenerLock ) {
            if( event != null ) {
                final int cacheSizeForEventClass = getCacheSizeForEventClass( event.getClass() );
                List eventClassCache = cacheByEvent.get( event.getClass() );
                if( cacheSizeForEventClass <= 0 ) {
                    if( eventClassCache != null ) {
                        //the cache threshold was lowered to 0
                        cacheByEvent.remove( event.getClass() );
                    }
                } else {
                    if( eventClassCache == null ) {
                        eventClassCache = new LinkedList();
                        cacheByEvent.put( event.getClass(), eventClassCache );
                    }
                    eventClassCache.add( 0, event );
                    while( eventClassCache.size() > cacheSizeForEventClass ) {
                        eventClassCache.remove( eventClassCache.size() - 1 );
                    }
                }
            } else {
                //topic
                final int cacheSizeForTopic = getCacheSizeForTopic( topic );
                List topicCache = cacheByTopic.get( topic );
                if( cacheSizeForTopic <= 0 ) {
                    if( topicCache != null ) {
                        //the cache threshold was lowered to 0
                        topicCache.remove( topic );
                    }
                } else {
                    if( topicCache == null ) {
                        topicCache = new LinkedList();
                        cacheByTopic.put( topic, topicCache );
                    }
                    topicCache.add( 0, eventObj );
                    while( topicCache.size() > cacheSizeForTopic ) {
                        topicCache.remove( topicCache.size() - 1 );
                    }
                }
            }
        }
    }

    /** @see EventService#getSubscribers(Class) */
    @Override
    public <T> List<T> getSubscribers( final Class<T> eventClass ) {
        List hierarchyMatches;
        List exactMatches;
        synchronized( listenerLock ) {
            hierarchyMatches = getSubscribersToClass( eventClass );
            exactMatches = getSubscribersToExactClass( eventClass );
        }
        List result = new ArrayList();
        if( exactMatches != null ) {
            result.addAll( exactMatches );
        }
        if( hierarchyMatches != null ) {
            result.addAll( hierarchyMatches );
        }
        if( hasEverUsedPrioritized ) {
            result = sortSubscribers( result );
        }
        return result;

    }

    /** @see EventService#getSubscribersToClass(Class) */
    @Override
    public <T> List<T> getSubscribersToClass( final Class<T> eventClass ) {
        synchronized( listenerLock ) {
            final Map classMap = subscribersByEventClass;
            List result = getEventOrVetoSubscribersToClass( classMap, eventClass );
            if( hasEverUsedPrioritized ) {
                result = sortSubscribers( result );
            }
            return result;
        }
    }

    /** @see EventService#getSubscribersToExactClass(Class) */
    @Override
    public <T> List<T> getSubscribersToExactClass( final Class<T> eventClass ) {
        synchronized( listenerLock ) {
            return getSubscribers( eventClass, subscribersByExactEventClass );
        }
    }

    /** @see EventService#getSubscribers(Type) */
    @Override
    public <T> List<T> getSubscribers( final Type eventType ) {
        List result;
        synchronized( listenerLock ) {
            result = getEventOrVetoSubscribersToType( subscribersByEventType, eventType );
        }
        if( hasEverUsedPrioritized ) {
            result = sortSubscribers( result );
        }
        return result;
    }

    /** @see EventService#getSubscribers(String) */
    @Override
    public <T> List<T> getSubscribers( final String topic ) {
        List result = new ArrayList();
        List exactMatches;
        List patternMatches;
        synchronized( listenerLock ) {
            exactMatches = getSubscribersToTopic( topic );
            patternMatches = getSubscribersByPattern( topic );
        }
        if( exactMatches != null ) {
            result.addAll( exactMatches );
        }
        if( patternMatches != null ) {
            result.addAll( patternMatches );
        }
        if( hasEverUsedPrioritized ) {
            result = sortSubscribers( result );
        }
        return result;
    }

    /** @see EventService#getSubscribersToTopic(String) */
    @Override
    public <T> List<T> getSubscribersToTopic( final String topic ) {
        synchronized( listenerLock ) {
            return getSubscribers( topic, subscribersByTopic );
        }
    }

    /** @see EventService#getSubscribers(Pattern) */
    @Override
    public <T> List<T> getSubscribers( final Pattern pattern ) {
        synchronized( listenerLock ) {
            return getSubscribers( pattern, subscribersByTopicPattern );
        }
    }

    /** @see EventService#getSubscribersByPattern(String) */
    @Override
    public <T> List<T> getSubscribersByPattern( final String topic ) {
        return getSubscribersByPattern( topic, subscribersByTopicPattern );
    }

    /** @see EventService#getVetoSubscribers(Class) */
    @Override
    public <T> List<T> getVetoSubscribers( final Class<T> eventClass ) {
        List result = new ArrayList();
        List exactMatches;
        List hierarchyMatches;
        synchronized( listenerLock ) {
            exactMatches = getVetoSubscribersToClass( eventClass );
            hierarchyMatches = getVetoSubscribersToExactClass( eventClass );
        }
        if( exactMatches != null ) {
            result.addAll( exactMatches );
        }
        if( hierarchyMatches != null ) {
            result.addAll( hierarchyMatches );
        }
        if( hasEverUsedPrioritized ) {
            result = sortSubscribers( result );
        }
        return result;
    }

    /** @see EventService#getVetoSubscribersToClass(Class) */
    @Override
    public <T> List<T> getVetoSubscribersToClass( final Class<T> eventClass ) {
        List result;
        synchronized( listenerLock ) {
            final Map classMap = vetoListenersByClass;
            result = getEventOrVetoSubscribersToClass( classMap, eventClass );
        }
        if( hasEverUsedPrioritized ) {
            result = sortSubscribers( result );
        }
        return result;
    }

    /** @see EventService#getVetoSubscribersToExactClass(Class) */
    @Override
    public <T> List<T> getVetoSubscribersToExactClass( final Class<T> eventClass ) {
        synchronized( listenerLock ) {
            return getSubscribers( eventClass, vetoListenersByExactClass );
        }
    }

    /** @see EventService#getVetoEventListeners(String) */
    @Override
    public <T> List<T> getVetoEventListeners( final String topicOrPattern ) {
        List result = new ArrayList();
        List exactMatches;
        List patternMatches;
        synchronized( listenerLock ) {
            exactMatches = getVetoSubscribersToTopic( topicOrPattern );
            patternMatches = getVetoSubscribersByPattern( topicOrPattern );
        }
        if( exactMatches != null ) {
            result.addAll( exactMatches );
        }
        if( patternMatches != null ) {
            result.addAll( patternMatches );
        }
        if( hasEverUsedPrioritized ) {
            result = sortSubscribers( result );
        }
        return result;
    }

    /** @see EventService#getVetoSubscribersToTopic(String) */
    @Override
    public <T> List<T> getVetoSubscribersToTopic( final String topic ) {
        synchronized( listenerLock ) {
            return getSubscribers( topic, vetoListenersByTopic );
        }
    }

    /**
     Note: this is inconsistent with getSubscribers(String)

     @see EventService#getVetoSubscribersToTopic(String)
     @deprecated use getVetoSubscribersToTopic instead for direct replacement, or use getVetoEventListeners to get
     topic and pattern matchers. In EventBus 2.0 this name will replace getVetoEventListeners() and have it's union
     functionality
     */
    @Deprecated
    @Override
    public <T> List<T> getVetoSubscribers( final String topic ) {
        synchronized( listenerLock ) {
            return getVetoSubscribersToTopic( topic );
        }
    }

    /** @see EventService#getVetoSubscribers(Pattern) */
    @Override
    public <T> List<T> getVetoSubscribers( final Pattern topicPattern ) {
        synchronized( listenerLock ) {
            final PatternWrapper patternWrapper = new PatternWrapper( topicPattern );
            return getSubscribers( patternWrapper, vetoListenersByTopicPattern );
        }
    }

    /** @see EventService#getVetoSubscribersByPattern(String) */
    @Override
    public <T> List<T> getVetoSubscribersByPattern( final String pattern ) {
        return getSubscribersByPattern( pattern, vetoListenersByTopicPattern );
    }

    /** Used for subscribers and veto subscribers */
    private <T> List<T> getSubscribersByPattern( final String topic, final Map subscribersByTopicPattern ) {
        List result = new ArrayList();
        synchronized( listenerLock ) {
            final Set keys = subscribersByTopicPattern.keySet();
            for( final Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
                final PatternWrapper patternKey = (PatternWrapper) iterator.next();
                if( patternKey.matches( topic ) ) {
                    if( LOG.isLoggable( Level.DEBUG ) ) {
                        LOG.debug( "Pattern " + patternKey + " matched topic name " + topic );
                    }
                    final Collection subscribers = (Collection) subscribersByTopicPattern.get( patternKey );
                    result.addAll( createCopyOfContentsRemoveWeakRefs( subscribers ) );
                }
            }
            if( hasEverUsedPrioritized ) {
                result = sortSubscribers( result );
            }
            return result;
        }
    }

    protected <T> List<T> getSubscribersToPattern( final Pattern topicPattern ) {
        synchronized( listenerLock ) {
            final PatternWrapper patternWrapper = new PatternWrapper( topicPattern );
            return getSubscribers( patternWrapper, subscribersByTopicPattern );
        }
    }

    private List getSubscribers( final Object classOrTopic, final Map subscriberMap ) {
        List result;
        synchronized( listenerLock ) {
            final List subscribers = (List) subscriberMap.get( classOrTopic );
            //Make a defensive copy of subscribers and veto listeners so listeners
            //can change the listener list while the listeners are being called
            //Resolve WeakReferences and unsubscribe if necessary.
            result = createCopyOfContentsRemoveWeakRefs( subscribers );
        }
        if( hasEverUsedPrioritized ) {
            result = sortSubscribers( result );
        }
        return result;
    }

    private List getEventOrVetoSubscribersToClass( final Map classMap, final Class eventClass ) {
        final List result = new ArrayList();
        final Set keys = classMap.keySet();
        for( final Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            final Class cl = (Class) iterator.next();
            if( cl.isAssignableFrom( eventClass ) ) {
                if( LOG.isLoggable( Level.DEBUG ) ) {
                    LOG.debug( "Hierarchical match " + cl + " matched event of class " + eventClass );
                }
                final Collection subscribers = (Collection) classMap.get( cl );
                result.addAll( createCopyOfContentsRemoveWeakRefs( subscribers ) );
            }
        }
        return result;
    }

    private List getEventOrVetoSubscribersToType( final Map typeMap, final Type eventType ) {
        final List result = new ArrayList();
        final Set mapKeySet = typeMap.keySet();
        for( final Object mapKey : mapKeySet ) {
            final Type subscriberType = (Type) mapKey;
            if( eventType instanceof ParameterizedType && subscriberType instanceof ParameterizedType ) {
                final ParameterizedType subscriberPT = (ParameterizedType) subscriberType;
                final ParameterizedType eventPT = (ParameterizedType) eventType;
                if( eventPT.getRawType().equals( subscriberPT.getRawType() ) ) {
                    final Type[] mapTypeArgs = subscriberPT.getActualTypeArguments();
                    final Type[] eventTypeArgs = eventPT.getActualTypeArguments();
                    if( mapTypeArgs == null || eventTypeArgs == null ||
                        mapTypeArgs.length != eventTypeArgs.length ) {
                        continue;
                    }
                    boolean parameterArgsMatch = true;
                    for( int argCount = 0; argCount < mapTypeArgs.length; argCount++ ) {
                        final Type eventTypeArg = eventTypeArgs[argCount];
                        if( eventTypeArg instanceof WildcardType ) {
                            throw new IllegalArgumentException(
                                "Only simple Class parameterized types can be published, not wildcards, etc.  Published attempt made for:" +
                                    eventTypeArg );
                        }
                        final Type subscriberTypeArg = mapTypeArgs[argCount];
                        if( subscriberTypeArg instanceof WildcardType ) {
                            final WildcardType wildcardSubscriberTypeArg = (WildcardType) subscriberTypeArg;
                            final Type[] upperBound = wildcardSubscriberTypeArg.getUpperBounds();
                            final Type[] lowerBound = wildcardSubscriberTypeArg.getLowerBounds();
                            if( upperBound != null && upperBound.length > 0 ) {
                                if( upperBound[0] instanceof Class ) {
                                    final Class upper = (Class) upperBound[0];
                                    if( eventTypeArg instanceof Class ) {
                                        if( !upper.isAssignableFrom( (Class) eventTypeArg ) ) {
                                            parameterArgsMatch = false;
                                            break;
                                        }
                                    } else {
                                        parameterArgsMatch = false;
                                        break;
                                    }
                                } else {
                                    throw new IllegalArgumentException(
                                        "Only Class and Interface types are supported as types of wildcard subscriptions.  Type:" +
                                            upperBound[0] );
                                }
                            }
                            if( lowerBound != null && lowerBound.length > 0 ) {
                                if( lowerBound[0] instanceof Class ) {
                                    final Class lower = (Class) lowerBound[0];
                                    if( eventTypeArg instanceof Class ) {
                                        if( !((Class) eventTypeArg).isAssignableFrom( lower ) ) {
                                            parameterArgsMatch = false;
                                            break;
                                        }
                                    } else {
                                        parameterArgsMatch = false;
                                        break;
                                    }
                                } else {
                                    throw new IllegalArgumentException(
                                        "Only Class and Interface types are supported as types of wildcard subscriptions.  Type:" +
                                            upperBound[0] );
                                }
                            }
                        } else if( !subscriberTypeArg.equals( eventTypeArg ) ) {
                            parameterArgsMatch = false;
                            break;
                        }
                    }
                    if( parameterArgsMatch ) {
                        if( LOG.isLoggable( Level.DEBUG ) ) {
                            LOG.debug(
                                "Exact parameterized subscriberType match for event subscriberType " + eventType );
                        }
                        final Collection subscribers = (Collection) typeMap.get( subscriberType );
                        if( subscribers != null ) {
                            result.addAll( createCopyOfContentsRemoveWeakRefs( subscribers ) );
                        }
                    }
                }
            }
        }
        return result;
//            Type o = p.getOwnerType();
//            if (o != null) {
//
//            }
//            p.getActualTypeArguments();
//         }
         /*
    } else if (type instanceof TypeVariable<?>) {
	    TypeVariable<?> v = (TypeVariable<?>)type;
	    out.print(v.getName());
	} else if (type instanceof GenericArrayType) {
	    GenericArrayType a = (GenericArrayType)type;
	    printType(a.getGenericComponentType());
	    out.print("[]");
	} else if (type instanceof WildcardType) {
	    WildcardType w = (WildcardType)type;
	    Type[] upper = w.getUpperBounds();
	    Type[] lower = w.getLowerBounds();
	    if (upper.length==1 && lower.length==0) {
		out.print("? extends ");
		printType(upper[0]);
	    } else if (upper.length==0 && lower.length==1) {
		out.print("? super ");
		printType(lower[0]);
	    } else assert false;
	}
          */
    }

    private void checkTimeLimit( final long start, final Object event, final EventSubscriber subscriber,
        final VetoEventListener l ) {
        if( timeThresholdForEventTimingEventPublication == null ) {
            return;
        }
        final long end = System.currentTimeMillis();
        if( end - start > timeThresholdForEventTimingEventPublication.longValue() ) {
            publish( new SubscriberTimingEvent( this, new Long( start ), new Long( end ),
                timeThresholdForEventTimingEventPublication, event, subscriber, l ) );
        }
    }

    protected void subscribeTiming( final SubscriberTimingEvent event ) {
        LOG.log( Level.INFO, event + "" );
    }

    /**
     Handle vetos of an event or topic, by default logs finely.

     @param vl the veto listener for an event
     @param event the event, can be null if topic is not
     @param vtl the veto listener for a topic
     @param topic can be null if event is not
     @param eventObj the object published with the topic
     */
    protected void handleVeto( final VetoEventListener vl, final Object event,
        final VetoTopicEventListener vtl, final String topic, final Object eventObj ) {
        if( LOG.isLoggable( Level.DEBUG ) ) {
            if( event != null ) {
                LOG.debug( "Vetoing event: class=" + event.getClass() + ", event=" + event + ", vetoer:" + vl );
            } else {
                LOG.debug( "Vetoing event: topic=" + topic + ", eventObj=" + eventObj + ", vetoer:" + vtl );
            }
        }
    }

    /**
     Given a Map (of Lists of subscribers or veto listeners), removes the toRemove element from the List in the map
     for the given key.  The entire map is checked for WeakReferences and ProxySubscribers and they are all
     unsubscribed if stale.

     @param map map of lists
     @param key key for a List in the map
     @param toRemove the object to remove form the list with the key of the map

     @return true if toRemove was unsubscribed
     */
    private boolean removeFromSetResolveWeakReferences( final Map map, final Object key, final Object toRemove ) {
        final List subscribers = (List) map.get( key );
        if( subscribers == null ) {
            return false;
        }
        if( subscribers.remove( toRemove ) ) {
            if( toRemove instanceof WeakReference ) {
                decWeakRefPlusProxySubscriberCount();
            }
            if( toRemove instanceof ProxySubscriber ) {
                ((ProxySubscriber) toRemove).proxyUnsubscribed();
                decWeakRefPlusProxySubscriberCount();
            }
            return true;
        }

        //search for WeakReferences and ProxySubscribers
        for( final Iterator iter = subscribers.iterator(); iter.hasNext(); ) {
            Object existingSubscriber = iter.next();
            if( existingSubscriber instanceof ProxySubscriber ) {
                final ProxySubscriber proxy = (ProxySubscriber) existingSubscriber;
                existingSubscriber = proxy.getProxiedSubscriber();
                if( existingSubscriber == toRemove ) {
                    removeProxySubscriber( proxy, iter );
                    return true;
                }
            }
            if( existingSubscriber instanceof WeakReference ) {
                final WeakReference wr = (WeakReference) existingSubscriber;
                final Object realRef = wr.get();
                if( realRef == null ) {
                    //clean up a garbage collected reference
                    iter.remove();
                    decWeakRefPlusProxySubscriberCount();
                    return true;
                } else if( realRef == toRemove ) {
                    iter.remove();
                    decWeakRefPlusProxySubscriberCount();
                    return true;
                } else if( realRef instanceof ProxySubscriber ) {
                    final ProxySubscriber proxy = (ProxySubscriber) realRef;
                    existingSubscriber = proxy.getProxiedSubscriber();
                    if( existingSubscriber == toRemove ) {
                        removeProxySubscriber( proxy, iter );
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     Given a set (or subscribers or veto listeners), makes a copy of the set, resolving WeakReferences to hard
     references, and removing garbage collected references from the original set.

     @param subscribersOrVetoListeners

     @return a copy of the set
     */
    private List createCopyOfContentsRemoveWeakRefs( final Collection subscribersOrVetoListeners ) {
        if( subscribersOrVetoListeners == null ) {
            return null;
        }
        final List copyOfSubscribersOrVetolisteners = new ArrayList( subscribersOrVetoListeners.size() );
        for( final Iterator iter = subscribersOrVetoListeners.iterator(); iter.hasNext(); ) {
            Object elem = iter.next();
            if( elem instanceof ProxySubscriber ) {
                final ProxySubscriber proxy = (ProxySubscriber) elem;
                elem = proxy.getProxiedSubscriber();
                if( elem == null ) {
                    removeProxySubscriber( proxy, iter );
                } else {
                    copyOfSubscribersOrVetolisteners.add( proxy );
                }
            } else if( elem instanceof WeakReference ) {
                final Object hardRef = ((WeakReference) elem).get();
                if( hardRef == null ) {
                    //Was reclaimed, unsubscribe
                    iter.remove();
                    decWeakRefPlusProxySubscriberCount();
                } else {
                    copyOfSubscribersOrVetolisteners.add( hardRef );
                }
            } else {
                copyOfSubscribersOrVetolisteners.add( elem );
            }
        }
        return copyOfSubscribersOrVetolisteners;
    }

    /**
     Sets the default cache size for each kind of event, default is 0 (no caching).
     <p/>
     If this value is set to a positive number, then when an event is published, the EventService caches the event
     or topic payload data for later retrieval.  This allows subscribers to find out what has most recently happened
     before they subscribed.  The cached event(s) are returned from #getLastEvent(Class), #getLastTopicData(String),
     #getCachedEvents(Class), or #getCachedTopicData(String)
     <p/>
     The default can be overridden on a by-event-class or by-topic basis.

     @param defaultCacheSizePerClassOrTopic
     */
    @Override
    public void setDefaultCacheSizePerClassOrTopic( final int defaultCacheSizePerClassOrTopic ) {
        synchronized( cacheLock ) {
            this.defaultCacheSizePerClassOrTopic = defaultCacheSizePerClassOrTopic;
        }
    }

    /** @return the default number of event payloads kept per event class or topic */
    @Override
    public int getDefaultCacheSizePerClassOrTopic() {
        synchronized( cacheLock ) {
            return defaultCacheSizePerClassOrTopic;
        }
    }

    /**
     Set the number of events cached for a particular class of event.  By default, no events are cached.
     <p/>
     This overrides any setting for the DefaultCacheSizePerClassOrTopic.
     <p/>
     Class hierarchy semantics are respected.  That is, if there are three events, A, X and Y, and X and Y are both
     derived from A, then setting the cache size for A applies the cache size for all three.  Setting the cache size
     for X applies to X and leaves the settings for A and Y in tact.  Interfaces can be passed to this method, but
     they only take effect if the cache size of a class or it's superclasses has been set. Just like
     Class.getInterfaces(), if multiple cache sizes are set, the interface names declared earliest in the implements
     clause of the eventClass takes effect.
     <p/>
     The cache for an event is not adjusted until the next event of that class is published.

     @param eventClass the class of event
     @param cacheSize the number of published events to cache for this event
     */
    @Override
    public void setCacheSizeForEventClass( final Class eventClass, final int cacheSize ) {
        synchronized( cacheLock ) {
            if( rawCacheSizesForEventClass == null ) {
                rawCacheSizesForEventClass = new HashMap();
            }
            rawCacheSizesForEventClass.put( eventClass, new Integer( cacheSize ) );
            rawCacheSizesForEventClassChanged = true;
        }
    }

    /**
     Returns the number of events cached for a particular class of event.  By default, no events are cached.
     <p/>
     This result is computed for a particular class from the values passed to #setCacheSizeForEventClass(Class,
     int), and respects the class hierarchy.

     @param eventClass the class of event

     @return the maximum size of the event cache for the given event class

     @see #setCacheSizeForEventClass(Class, int)
     */
    @Override
    public int getCacheSizeForEventClass( final Class eventClass ) {
        if( eventClass == null ) {
            throw new IllegalArgumentException( "eventClass must not be null." );
        }
        synchronized( cacheLock ) {
            if( rawCacheSizesForEventClass == null || rawCacheSizesForEventClass.size() == 0 ) {
                return getDefaultCacheSizePerClassOrTopic();
            }
            if( cacheSizesForEventClass == null ) {
                cacheSizesForEventClass = new HashMap();
            }
            if( rawCacheSizesForEventClassChanged ) {
                cacheSizesForEventClass.clear();
                cacheSizesForEventClass.putAll( rawCacheSizesForEventClass );
                rawCacheSizesForEventClassChanged = false;
            }

            //Has this been computed yet or set directly?
            final Integer size = cacheSizesForEventClass.get( eventClass );
            if( size != null ) {
                return size.intValue();
            } else {
                //must be computed
                Class parent = eventClass.getSuperclass();
                while( parent != null ) {
                    final Integer parentSize = cacheSizesForEventClass.get( parent );
                    if( parentSize != null ) {
                        cacheSizesForEventClass.put( eventClass, parentSize );
                        return parentSize.intValue();
                    }
                    parent = parent.getSuperclass();
                }
                //try interfaces
                final Class[] interfaces = eventClass.getInterfaces();
                for( int i = 0; i < interfaces.length; i++ ) {
                    final Class anInterface = interfaces[i];
                    final Integer interfaceSize = cacheSizesForEventClass.get( anInterface );
                    if( interfaceSize != null ) {
                        cacheSizesForEventClass.put( eventClass, interfaceSize );
                        return interfaceSize.intValue();
                    }
                }
            }
            return getDefaultCacheSizePerClassOrTopic();
        }
    }

    /**
     Set the number of published data objects cached for a particular event topic.  By default, no caching is done.
     <p/>
     This overrides any setting for the DefaultCacheSizePerClassOrTopic.
     <p/>
     Settings for exact topic names take precedence over pattern matching.
     <p/>
     The cache for a topic is not adjusted until the next publication on that topic.

     @param topicName the topic name
     @param cacheSize the number of published data Objects to cache for this topic
     */
    @Override
    public void setCacheSizeForTopic( final String topicName, final int cacheSize ) {
        synchronized( cacheLock ) {
            if( rawCacheSizesForTopic == null ) {
                rawCacheSizesForTopic = new HashMap();
            }
            rawCacheSizesForTopic.put( topicName, new Integer( cacheSize ) );
            rawCacheSizesForTopicChanged = true;
        }
    }

    /**
     Set the number of published data objects cached for topics matching a pattern.  By default, caching is done.
     <p/>
     This overrides any setting for the DefaultCacheSizePerClassOrTopic.
     <p/>
     Settings for exact topic names take precedence over pattern matching.  If a topic matches the cache settings
     for more than one pattern, the cache size chosen is an undetermined one from one of the matched pattern
     settings.
     <p/>
     The cache for a topic is not adjusted until the next publication on that topic.

     @param pattern the pattern matching topic names
     @param cacheSize the number of data Objects to cache for this topic
     */
    @Override
    public void setCacheSizeForTopic( final Pattern pattern, final int cacheSize ) {
        synchronized( cacheLock ) {
            if( rawCacheSizesForPattern == null ) {
                rawCacheSizesForPattern = new HashMap();
            }
            final PatternWrapper patternWrapper = new PatternWrapper( pattern );
            rawCacheSizesForPattern.put( patternWrapper, new Integer( cacheSize ) );
            rawCacheSizesForPatternChanged = true;
        }
    }

    /**
     Returns the number of cached data objects published on a particular topic.  By default, no caching is
     performed.
     <p/>
     This result is computed for a particular topic from the values passed to #setCacheSizeForTopic(String, int) and
     #setCacheSizeForTopic(Pattern, int).

     @param topic the topic name

     @return the maximum size of the data Object cache for the given topic

     @see #setCacheSizeForTopic(String, int)
     @see #setCacheSizeForTopic(java.util.regex.Pattern, int)
     */
    @Override
    public int getCacheSizeForTopic( final String topic ) {
        if( topic == null ) {
            throw new IllegalArgumentException( "topic must not be null." );
        }
        synchronized( cacheLock ) {
            if( (rawCacheSizesForTopic == null ||
                (rawCacheSizesForTopic != null && rawCacheSizesForTopic.size() == 0)) &&
                (rawCacheSizesForPattern == null ||
                    (rawCacheSizesForPattern != null && rawCacheSizesForPattern.size() == 0)) ) {
                return getDefaultCacheSizePerClassOrTopic();
            }
            if( cacheSizesForTopic == null ) {
                cacheSizesForTopic = new HashMap();
            }
            if( rawCacheSizesForTopicChanged || rawCacheSizesForPatternChanged ) {
                cacheSizesForTopic.clear();
                cacheSizesForTopic.putAll( rawCacheSizesForTopic );
                rawCacheSizesForTopicChanged = false;
                rawCacheSizesForPatternChanged = false;
            }

            //Is this an exact match or has it been matched to a pattern yet?
            Integer size = cacheSizesForTopic.get( topic );
            if( size != null ) {
                return size;
            } else {
                //try matching patterns
                if( rawCacheSizesForPattern != null ) {
                    final Set patterns = rawCacheSizesForPattern.keySet();
                    for( final Iterator iterator = patterns.iterator(); iterator.hasNext(); ) {
                        final PatternWrapper pattern = (PatternWrapper) iterator.next();
                        if( pattern.matches( topic ) ) {
                            size = rawCacheSizesForPattern.get( pattern );
                            cacheSizesForTopic.put( topic, size );
                            return size;
                        }
                    }
                }
            }
            return getDefaultCacheSizePerClassOrTopic();
        }
    }

    /**
     @param eventClass an index into the cache, cannot be an interface

     @return the last event published for this event class, or null if caching is turned off (the default)
     */
    @Override
    public Object getLastEvent( final Class eventClass ) {
        if( eventClass.isInterface() ) {
            throw new IllegalArgumentException(
                "Interfaces are not accepted in get last event, use a specific event class." );
        }
        synchronized( cacheLock ) {
            final List eventCache = cacheByEvent.get( eventClass );
            if( eventCache == null || eventCache.size() == 0 ) {
                return null;
            }
            return eventCache.get( 0 );
        }
    }

    /**
     @param eventClass an index into the cache, cannot be an interface

     @return the last events published for this event class, or null if caching is turned off (the default)
     */
    @Override
    public List getCachedEvents( final Class eventClass ) {
        if( eventClass.isInterface() ) {
            throw new IllegalArgumentException(
                "Interfaces are not accepted in get last event, use a specific event class." );
        }
        synchronized( cacheLock ) {
            final List eventCache = cacheByEvent.get( eventClass );
            if( eventCache == null || eventCache.size() == 0 ) {
                return null;
            }
            return eventCache;
        }
    }

    /**
     @param topic an index into the cache

     @return the last data Object published on this topic, or null if caching is turned off (the default)
     */
    @Override
    public Object getLastTopicData( final String topic ) {
        synchronized( cacheLock ) {
            final List topicCache = cacheByTopic.get( topic );
            if( topicCache == null || topicCache.size() == 0 ) {
                return null;
            }
            return topicCache.get( 0 );
        }
    }

    /**
     @param topic an index into the cache

     @return the last data Objects published on this topic, or null if caching is turned off (the default)
     */
    @Override
    public List getCachedTopicData( final String topic ) {
        synchronized( cacheLock ) {
            final List topicCache = cacheByTopic.get( topic );
            if( topicCache == null || topicCache.size() == 0 ) {
                return null;
            }
            return topicCache;
        }
    }

    /**
     Clears the event cache for a specific event class or interface and it's any of it's subclasses or implementing
     classes.

     @param eventClassToClear the event class to clear the cache for
     */
    @Override
    public void clearCache( final Class eventClassToClear ) {
        synchronized( cacheLock ) {
            final Set classes = cacheByEvent.keySet();
            for( final Iterator iterator = classes.iterator(); iterator.hasNext(); ) {
                final Class cachedClass = (Class) iterator.next();
                if( eventClassToClear.isAssignableFrom( cachedClass ) ) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     Clears the topic data cache for a specific topic name.

     @param topic the topic name to clear the cache for
     */
    @Override
    public void clearCache( final String topic ) {
        synchronized( cacheLock ) {
            cacheByTopic.remove( topic );
        }
    }

    /**
     Clears the topic data cache for all topics that match a particular pattern.

     @param pattern the pattern to match topic caches to
     */
    @Override
    public void clearCache( final Pattern pattern ) {
        synchronized( cacheLock ) {
            final Set classes = cacheByTopic.keySet();
            for( final Iterator iterator = classes.iterator(); iterator.hasNext(); ) {
                final String cachedTopic = (String) iterator.next();
                if( pattern.matcher( cachedTopic ).matches() ) {
                    iterator.remove();
                }
            }
        }
    }

    /** Clear all event caches for all topics and event. */
    @Override
    public void clearCache() {
        synchronized( cacheLock ) {
            cacheByEvent.clear();
            cacheByTopic.clear();
        }
    }

    /** Called during veto exceptions, calls handleException */
    protected void subscribeVetoException( final Object event, final String topic, final Object eventObj,
        final Throwable e, final StackTraceElement[] callingStack, final VetoEventListener vetoer ) {
        String str = "EventService veto event listener r:" + vetoer;
        if( vetoer != null ) {
            str = str + ".  Vetoer class:" + vetoer.getClass();
        }
        handleException( "vetoing", event, topic, eventObj, e, callingStack, str );
    }

    /** Called during event handling exceptions, calls handleException */
    protected void onEventException( final String topic, final Object eventObj, final Throwable e,
        final StackTraceElement[] callingStack, final EventTopicSubscriber eventTopicSubscriber ) {
        String str = "EventService topic subscriber:" + eventTopicSubscriber;
        if( eventTopicSubscriber != null ) {
            str = str + ".  Subscriber class:" + eventTopicSubscriber.getClass();
        }
        handleException( "handling event", null, topic, eventObj, e, callingStack, str );
    }

    /** Called during event handling exceptions, calls handleException */
    protected void handleException( final Object event, final Throwable e,
        final StackTraceElement[] callingStack, final EventSubscriber eventSubscriber ) {
        String str = "EventService subscriber:" + eventSubscriber;
        if( eventSubscriber != null ) {
            str = str + ".  Subscriber class:" + eventSubscriber.getClass();
        }
        handleException( "handling event topic", event, null, null, e, callingStack, str );
    }

    /**
     All exception handling goes through this method.  Logs a warning by default.
     */
    protected void handleException( final String action, final Object event, final String topic,
        final Object eventObj, final Throwable e, final StackTraceElement[] callingStack,
        final String sourceString ) {
        final String eventClassString = (event == null ? "none" : event.getClass().getName());
        final String eventString = event + "";
        final String contextMsg = "Exception " + action + " event class=" + eventClassString
            + ", event=" + eventString + ", topic=" + topic + ", eventObj=" + eventObj;
        final EventBusException clientEx = new EventBusException( contextMsg, e, callingStack );
        final String msg = "Exception thrown by;" + sourceString;
        LOG.log( Level.WARN, msg, clientEx );
        throw clientEx;
    }

    /**
     Unsubscribe a subscriber if it is a stale ProxySubscriber. Used during subscribe() and in the cleanup Timer.
     See the class javadoc.
     <p/>
     Not private since I don't claim I'm smart enough to anticipate all needs, but I am smart enough to doc the
     rules you must follow to override this method.  Those rules may change (changes will be doc'ed), override at
     your own risk.
     <p/>
     Overriders MUST call iterator.remove() to unsubscribe the proxy if the subscriber is a ProxySubscriber and is
     stale and should be cleaned up.  If the ProxySubscriber is unsubscribed, then implementers MUST also call
     proxyUnsubscribed() on the subscriber. Overriders MUST also remove the proxy from the weakProxySubscriber list
     by calling removeStaleProxyFromList.  Method assumes caller is holding the listenerList lock (else how can you
     pass the iterator?).

     @param iterator current iterator
     @param existingSubscriber the current value of the iterator

     @return the real value of the param, or the proxied subscriber of the param if the param is a a ProxySubscriber
     */
    protected Object getRealSubscriberAndCleanStaleSubscriberIfNecessary( final Iterator iterator,
        Object existingSubscriber ) {
        ProxySubscriber existingProxySubscriber = null;
        if( existingSubscriber instanceof WeakReference ) {
            existingSubscriber = ((WeakReference) existingSubscriber).get();
            if( existingSubscriber == null ) {
                iterator.remove();
                decWeakRefPlusProxySubscriberCount();
            }
        }
        if( existingSubscriber instanceof ProxySubscriber ) {
            existingProxySubscriber = (ProxySubscriber) existingSubscriber;
            existingSubscriber = existingProxySubscriber.getProxiedSubscriber();
            if( existingProxySubscriber == null ) {
                removeProxySubscriber( existingProxySubscriber, iterator );
            }
        }
        return existingSubscriber;
    }

    protected void removeProxySubscriber( final ProxySubscriber proxy, final Iterator iter ) {
        iter.remove();
        proxy.proxyUnsubscribed();
        decWeakRefPlusProxySubscriberCount();
    }

    /**
     Increment the count of stale proxies and start a cleanup task if necessary
     */
    protected void incWeakRefPlusProxySubscriberCount() {
        synchronized( listenerLock ) {
            weakRefPlusProxySubscriberCount++;
            if( cleanupStartThreshhold == null || cleanupPeriodMS == null ) {
                return;
            }
            if( weakRefPlusProxySubscriberCount >= cleanupStartThreshhold ) {
                startCleanup();
            }
        }
    }

    /**
     Decrement the count of stale proxies
     */
    protected void decWeakRefPlusProxySubscriberCount() {
        synchronized( listenerLock ) {
            weakRefPlusProxySubscriberCount--;
            if( weakRefPlusProxySubscriberCount < 0 ) {
                weakRefPlusProxySubscriberCount = 0;
            }
        }
    }

    private void startCleanup() {
        synchronized( listenerLock ) {
            if( cleanupTimer == null ) {
                cleanupTimer = new Timer();
            }
            if( cleanupTimerTask == null ) {
                cleanupTimerTask = new CleanupTimerTask();
                cleanupTimer.schedule( cleanupTimerTask, 0L, cleanupPeriodMS );
            }
        }
    }

    class CleanupTimerTask extends TimerTask {
        @Override
        public void run() {
            synchronized( listenerLock ) {
                ThreadSafeEventService.this.publish(
                    new CleanupEvent( CleanupEvent.Status.STARTING, weakRefPlusProxySubscriberCount, null ) );
                if( weakRefPlusProxySubscriberCount <= cleanupStopThreshold ) {
                    this.cancel();
                    cleanupTimer = null;
                    cleanupTimerTask = null;
                    LOG.debug( "Cancelled scheduled weak reference and proxy cleanup." );
                    ThreadSafeEventService.this.publish(
                        new CleanupEvent( CleanupEvent.Status.UNDER_STOP_THRESHOLD_CLEANING_CANCELLED,
                            weakRefPlusProxySubscriberCount, null ) );
                    return;
                }
                LOG.debug( "Starting a weak reference and proxy cleanup." );
                ThreadSafeEventService.this.publish(
                    new CleanupEvent( CleanupEvent.Status.OVER_STOP_THRESHOLD_CLEANING_BEGUN,
                        weakRefPlusProxySubscriberCount, null ) );
                final List<Map> allSubscriberMaps = new ArrayList<Map>();
                allSubscriberMaps.add( subscribersByEventType );
                allSubscriberMaps.add( subscribersByEventClass );
                allSubscriberMaps.add( subscribersByExactEventClass );
                allSubscriberMaps.add( subscribersByTopic );
                allSubscriberMaps.add( subscribersByTopicPattern );
                allSubscriberMaps.add( vetoListenersByClass );
                allSubscriberMaps.add( vetoListenersByExactClass );
                allSubscriberMaps.add( vetoListenersByTopic );
                allSubscriberMaps.add( vetoListenersByTopicPattern );

                int staleCount = 0;
                for( final Map subscriberMap : allSubscriberMaps ) {
                    final Set subscriptions = subscriberMap.keySet();
                    for( final Object subscription : subscriptions ) {
                        final List subscribers = (List) subscriberMap.get( subscription );
                        for( final Iterator iter = subscribers.iterator(); iter.hasNext(); ) {
                            final Object subscriber = iter.next();
                            final Object realSubscriber =
                                getRealSubscriberAndCleanStaleSubscriberIfNecessary( iter, subscriber );
                            if( realSubscriber == null ) {
                                staleCount++;
                            }
                        }
                    }
                }
                ThreadSafeEventService.this.publish(
                    new CleanupEvent( CleanupEvent.Status.FINISHED_CLEANING, weakRefPlusProxySubscriberCount,
                        staleCount ) );
            }
        }
    }

    private static class PrioritizedSubscriberComparator implements Comparator<Prioritized> {
        @Override
        public int compare( final Prioritized prioritized1, final Prioritized prioritized2 ) {
            if( prioritized1 == null ) {
                return -1;
            }
            if( prioritized2 == null ) {
                return 1;
            }
            if( prioritized1.getPriority() < prioritized2.getPriority() ) {
                return -1;
            } else if( prioritized1.getPriority() > prioritized2.getPriority() ) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     Since Pattern doesn't implement equals(), we need one of these
     */
    private class PatternWrapper {
        private final Pattern pattern;

        public PatternWrapper( final Pattern pat ) {
            pattern = pat;
        }

        public boolean matches( final CharSequence input ) {
            return pattern.matcher( input ).matches();
        }

        @Override
        public boolean equals( final Object o ) {
            if( this == o ) {
                return true;
            }
            if( o == null || getClass() != o.getClass() ) {
                return false;
            }

            final PatternWrapper that = (PatternWrapper) o;

            if( pattern != null ) {
                if( !pattern.equals( that.pattern ) ) {//give the JVM a shot for forward compatibility
                    return pattern.pattern() != null && this.pattern.pattern().equals( this.pattern.pattern() );
                }
            } else {
                if( that.pattern != null ) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            if( this.pattern != null && this.pattern.pattern() != null ) {
                return this.pattern.pattern().hashCode();
            }
            return (pattern != null ? pattern.hashCode() : 0);
        }
    }
}
