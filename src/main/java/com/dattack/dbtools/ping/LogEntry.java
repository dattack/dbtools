/*
 * Copyright (c) 2014, The Dattack team (http://www.dattack.com)
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
package com.dattack.dbtools.ping;

import java.io.Serializable;

import com.dattack.dbtools.Builder;

/**
 * @author cvarela
 * @since 0.1
 */
public class LogEntry implements Serializable {

    public static class LogEntryBuilder implements Serializable, Builder<LogEntry> {

        private static final long serialVersionUID = 9149270318492709877L;

        private static final long UNKNOWN = -1;

        private long connectionTime;
        private Exception exception;
        private long totalTime;
        private long firstRowTime;
        private long iteration;
        private long rows;
        private String sqlLabel;
        private long eventTime;
        private String taskName;
        private String threadName;

        public LogEntryBuilder() {
            init();
        }

        @Override
        public LogEntry build() {
            this.totalTime = computeRelativeTime();
            if (exception == null && firstRowTime == UNKNOWN) {
                this.firstRowTime = totalTime;
            }
            LogEntry logEntry = new LogEntry(this);
            init();
            return logEntry;
        }

        /**
         * Sets the connection time.
         */
        public synchronized void connect() {
            this.connectionTime = computeRelativeTime();
        }

        private long computeRelativeTime() {
            return System.currentTimeMillis() - eventTime;
        }

        /**
         * Increments the number of rows and sets the first row timer.
         */
        public void incrRows() {
            if (rows == 0) {
                this.firstRowTime = computeRelativeTime();
            }
            rows++;
        }

        public LogEntryBuilder init() {
            this.eventTime = System.currentTimeMillis();
            this.connectionTime = UNKNOWN;
            this.exception = null;
            this.firstRowTime = UNKNOWN;
            this.iteration = UNKNOWN;
            this.rows = 0;
            this.sqlLabel = null;
            this.totalTime = UNKNOWN;
            return this;
        }

        public LogEntryBuilder withEventTime(final long value) {
            this.eventTime = value;
            return this;
        }

        public LogEntryBuilder withException(final Exception value) {
            this.exception = value;
            return this;
        }

        public LogEntryBuilder withIteration(final long value) {
            this.iteration = value;
            return this;
        }

        public LogEntryBuilder withSqlLabel(final String value) {
            this.sqlLabel = value;
            return this;
        }

        public LogEntryBuilder withTaskName(final String value) {
            this.taskName = value;
            return this;
        }

        public LogEntryBuilder withThreadName(final String value) {
            this.threadName = value;
            return this;
        }

        public LogEntryBuilder withRows(final long value) {
            this.rows = value;
            return this;
        }

        public LogEntryBuilder withConnectionTime(final long value) {
            this.connectionTime = value;
            return this;
        }

        public LogEntryBuilder withFirstRowTime(final long value) {
            this.firstRowTime = value;
            return this;
        }

        public LogEntryBuilder withTotalTime(final long value) {
            this.totalTime = value;
            return this;
        }
    }

    private static final long serialVersionUID = 9149270318492709877L;

    private final long connectionTime;
    private final Exception exception;
    private final long executionTime;
    private final long firstRowTime;
    private final long iteration;
    private final long rows;
    private final String sqlLabel;
    private final long startTime;
    private final String taskName;
    private final String threadName;

    private LogEntry(final LogEntryBuilder builder) {
        this.taskName = builder.taskName;
        this.threadName = builder.threadName;
        this.iteration = builder.iteration;
        this.sqlLabel = builder.sqlLabel;
        this.rows = builder.rows;
        this.startTime = builder.eventTime;
        this.connectionTime = builder.connectionTime;
        this.firstRowTime = builder.firstRowTime;
        this.executionTime = builder.totalTime;
        this.exception = builder.exception;
    }

    public long getConnectionTime() {
        return connectionTime;
    }

    public Exception getException() {
        return exception;
    }

    public long getTotalTime() {
        return executionTime;
    }

    public long getFirstRowTime() {
        return firstRowTime;
    }

    public long getIteration() {
        return iteration;
    }

    public long getRows() {
        return rows;
    }

    public String getSqlLabel() {
        return sqlLabel;
    }

    public long getEventTime() {
        return startTime;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getThreadName() {
        return threadName;
    }
}
