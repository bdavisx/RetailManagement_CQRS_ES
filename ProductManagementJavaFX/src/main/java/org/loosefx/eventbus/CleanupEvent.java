/**
 * Copyright 2007 Bushe Enterprises, Inc., Hopkinton, MA, USA, www.bushe.com
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

/**
 Published when the ThreadSafeEventService cleans up stale subscribers.

 @author Michael Bushe */
public class CleanupEvent {

    /** The status of the cleanup. */
    public enum Status {
        /** Timer has started the cleanup task. Will be followed by at least one more CleanupEvent. */
        STARTING,
        /** Task has determined there's cleanup to do. */
        OVER_STOP_THRESHOLD_CLEANING_BEGUN,
        /** Task has determined there's no cleanup to do. */
        UNDER_STOP_THRESHOLD_CLEANING_CANCELLED,
        /** Finished cleaning up task. */
        FINISHED_CLEANING;
    }

    private final Status status;
    private final int totalWeakRefsAndProxies;
    private final Integer numStaleSubscribersCleaned;

    public CleanupEvent( final Status status, final int totalWeakRefsAndProxies,
        final Integer numStaleSubscribersCleaned ) {
        this.status = status;
        this.totalWeakRefsAndProxies = totalWeakRefsAndProxies;
        this.numStaleSubscribersCleaned = numStaleSubscribersCleaned;
    }

    public Status getStatus() {
        return status;
    }

    /** Total weak refs and ProxySubscribers subscribed. */
    public int getTotalWeakRefsAndProxies() {
        return totalWeakRefsAndProxies;
    }

    /**
     Null unless status is FINISHED_CLEANING.

     @return the number of stale subscribers cleaned during the cleanup run.
     */
    public Integer getNumStaleSubscribersCleaned() {
        return numStaleSubscribersCleaned;
    }
}
