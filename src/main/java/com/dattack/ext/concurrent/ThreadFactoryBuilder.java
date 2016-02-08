/*
 * Copyright (c) 2015, The Dattack team (http://www.dattack.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dattack.ext.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.Validate;

/**
 * @author cvarela
 * @since 0.1
 */
public class ThreadFactoryBuilder {

    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);

    private ThreadGroup group;
    private String namePrefix;
    private Boolean daemon;
    private Integer priority;
    private UncaughtExceptionHandler uncaughtExceptionHandler;

    public class SimpleThreadFactory implements ThreadFactory {

        private final AtomicLong threadNumber = new AtomicLong(1);
        private final String threadNamePrefix;

        SimpleThreadFactory(final String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        private String generateThreadName() {
            return threadNamePrefix + "-" + threadNumber.getAndIncrement();
        }

        @Override
        public Thread newThread(final Runnable target) {

            final Thread thread = new Thread(group, target, generateThreadName(), 0);
            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            if (daemon != null) {
                thread.setDaemon(daemon);
            }
            if (priority != null) {
                thread.setPriority(priority);
            }
            return thread;
        }
    }

    public ThreadFactory build() {

        return new SimpleThreadFactory(getThreadNamePrefix());
    }

    private String getThreadNamePrefix() {

        if (namePrefix != null) {
            return namePrefix;
        }

        return "pool-" + POOL_NUMBER.getAndIncrement() + "-thread";
    }

    public ThreadFactoryBuilder withDaemon(final boolean value) {
        this.daemon = value;
        return this;
    }

    public ThreadFactoryBuilder withNamePrefix(final String value) {
        this.namePrefix = value;
        return this;
    }

    /**
     * Sets the priority for new threads.
     *
     * @param value
     *            the new priority
     * @return self object
     */
    public ThreadFactoryBuilder withPriority(final int value) {

        Validate.isTrue(value >= Thread.MIN_PRIORITY && value <= Thread.MAX_PRIORITY, String
                .format("Thread priority (%s) must be in [%s..%s]", value, Thread.MIN_PRIORITY, Thread.MAX_PRIORITY));

        this.priority = value;
        return this;
    }

    public ThreadFactoryBuilder withThreadGroup(final ThreadGroup value) {
        this.group = value;
        return this;
    }

    /**
     * Sets the handler for uncaught exceptions.
     *
     * @param value
     *            the handler
     * @return self object
     */
    public ThreadFactoryBuilder withUncaughtExceptionHandler(final UncaughtExceptionHandler value) {

        Validate.notNull(value);
        this.uncaughtExceptionHandler = value;
        return this;
    }
}
