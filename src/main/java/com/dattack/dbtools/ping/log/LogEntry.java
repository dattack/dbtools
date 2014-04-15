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
package com.dattack.dbtools.ping.log;

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
	private boolean running;

	private final long startTime;
	private long connectionTime;
	private long firstRowTime;
	private long lastRowTime;
	private Exception exception;

	public LogEntry(final String taskName, final String threadName, final long iteration, final String sqlLabel) {
		this.taskName = taskName;
		this.threadName = threadName;
		startTime = System.currentTimeMillis();
		this.iteration = iteration;
		this.sqlLabel = sqlLabel;
		running = true;
		connectionTime = UNKNOWN_TIME;
		firstRowTime = UNKNOWN_TIME;
		lastRowTime = UNKNOWN_TIME;
	}

	public void end() {
		if (running) {
			running = false;
			lastRowTime = System.currentTimeMillis();
			if (firstRowTime == UNKNOWN_TIME) {
				firstRowTime = lastRowTime;
			}
		}
	}

	public Exception getException() {
		return exception;
	}

	public long getConnectionTime() {
		return connectionTime - startTime;
	}
	
	public long getExecutionTime() {
		return lastRowTime - startTime;
	}

	public long getFirstRowTime() {
		return firstRowTime - startTime;
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

	public long getRows() {
		return rows;
	}

	public long getStartTime() {
		return startTime;
	}

	public synchronized void connect() {
		if (connectionTime == UNKNOWN_TIME) {
			connectionTime = System.currentTimeMillis();
		}
	}

	public void incrRows() {
		if (running) {
			if (firstRowTime == UNKNOWN_TIME) {
				firstRowTime = System.currentTimeMillis();
			}
			rows++;
		}
	}

	public void setException(Exception exception) {
		this.exception = exception;
		end();
	}
}
