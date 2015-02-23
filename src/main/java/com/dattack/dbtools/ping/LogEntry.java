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

/**
 * @author cvarela
 * @since 0.1
 */
public class LogEntry implements Serializable {

    private static final long serialVersionUID = 9149270318492709877L;

    private static final long UNKNOWN_TIME = -1;

    private final String taskName;
    private final String threadName;
    private final long iteration;
    private final String sqlLabel;
    private long rows;
    private long startTime;
    private long connectionTime;
    private long firstRowTime;
    private long executionTime;
    private Exception exception;

    public LogEntry(final String taskName, final String threadName, final long iteration, final String sqlLabel) {
        this.taskName = taskName;
        this.threadName = threadName;
        this.iteration = iteration;
        this.sqlLabel = sqlLabel;
        this.startTime = System.currentTimeMillis();
        connectionTime = UNKNOWN_TIME;
        firstRowTime = UNKNOWN_TIME;
        executionTime = UNKNOWN_TIME;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    public long getRows() {
        return rows;
    }

    public void setRows(final long rows) {
        this.rows = rows;
    }

    public long getConnectionTime() {
        return connectionTime;
    }

    public void setConnectionTime(final long connectionTime) {
        this.connectionTime = connectionTime;
    }

    public long getFirstRowTime() {
        return firstRowTime;
    }

    public void setFirstRowTime(final long firstRowTime) {
        this.firstRowTime = firstRowTime;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(final long lastRowTime) {
        this.executionTime = lastRowTime;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(final Exception exception) {
        this.exception = exception;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getThreadName() {
        return threadName;
    }

    public long getIteration() {
        return iteration;
    }

    public String getSqlLabel() {
        return sqlLabel;
    }
}
