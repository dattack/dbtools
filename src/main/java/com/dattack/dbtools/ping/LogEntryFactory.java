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
class LogEntryFactory implements Serializable {

    private static final long serialVersionUID = 9149270318492709877L;

    private static final long UNKNOWN_TIME = -1;

    private final LogEntry logEntry;
    private long rows;
    private boolean running;

    private final long startTime;
    private long connectionTime;
    private long firstRowTime;
    private long lastRowTime;

    public LogEntryFactory(final String taskName, final String threadName, final long iteration, final String sqlLabel) {
        logEntry = new LogEntry(taskName, threadName, iteration, sqlLabel);
        startTime = System.currentTimeMillis();
        running = true;
        connectionTime = UNKNOWN_TIME;
        firstRowTime = UNKNOWN_TIME;
        lastRowTime = UNKNOWN_TIME;
    }

    /**
     * Computes the total time.
     */
    public LogEntry create() {
        end();
        return logEntry;
    }

    public LogEntry create(final Exception exception) {
        end();
        logEntry.setException(exception);
        return logEntry;
    }

    private void end() {
        if (running) {
            running = false;
            lastRowTime = System.currentTimeMillis() - startTime;
            if (firstRowTime == UNKNOWN_TIME) {
                firstRowTime = lastRowTime;
            }
            logEntry.setRows(rows);
        }
    }

    /**
     * Sets the connection time.
     */
    public synchronized void connect() {
        if (running) {
            if (connectionTime == UNKNOWN_TIME) {
                connectionTime = System.currentTimeMillis() - startTime;
            }
        }
    }

    /**
     * Increments the number of rows.
     */
    public void incrRows() {
        if (running) {
            if (firstRowTime == UNKNOWN_TIME) {
                firstRowTime = System.currentTimeMillis() - startTime;
            }
            rows++;
        }
    }
}
