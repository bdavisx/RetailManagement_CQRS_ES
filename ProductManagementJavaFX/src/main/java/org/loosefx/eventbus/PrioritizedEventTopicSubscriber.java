package org.loosefx.eventbus;

/**
 This is a convenience interface, particularly for inner classes, that implements {@link
org.loosefx.eventbus.EventTopicSubscriber} and {@link org.loosefx.eventbus.Prioritized}.
 */
public interface PrioritizedEventTopicSubscriber extends EventTopicSubscriber, Prioritized {
}
