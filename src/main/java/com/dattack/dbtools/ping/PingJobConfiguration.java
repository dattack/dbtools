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
import java.util.ArrayList;
import java.util.List;

import com.dattack.dbtools.Builder;

/**
 * @author cvarela
 * @since 0.1
 */
public class PingJobConfiguration implements Serializable {

	public static class PingConfigurationBuilder implements Builder<PingJobConfiguration> {

		private static final int DEFAULT_EXECUTIONS = 0; // UNLIMITED
		private static final int DEFAULT_THREADS = 1;
		private static final int DEFAULT_TIME_BETWEEN_EXECUTIONS = 0; // NOT WAIT

		private String datasource;
		private String name;
		
		// the number of iterations by each thread
		private int executions;
		
		// SQL query list
		private List<SQLSentence> queryList;
		
		// the number of threads to run
		private int threads;
		
		// the time to wait between two iterations
		private long timeBetweenExecutions;

		public PingConfigurationBuilder() {
			executions = DEFAULT_EXECUTIONS;
			threads = DEFAULT_THREADS;
			timeBetweenExecutions = DEFAULT_TIME_BETWEEN_EXECUTIONS;
		}

		@Override
		public PingJobConfiguration build() {
			return new PingJobConfiguration(this);
		}

		public PingConfigurationBuilder withDatasource(String datasource) {
			this.datasource = datasource;
			return this;
		}

		public PingConfigurationBuilder withExecutions(int executions) {
			this.executions = executions;
			return this;
		}

		public PingConfigurationBuilder withName(String name) {
			this.name = name;
			return this;
		}

		public PingConfigurationBuilder withQueryList(List<Object> list) {
			this.queryList = new ArrayList<SQLSentence>();
			for (int i = 0; i < list.size(); i++) {
				this.queryList.add(new SQLSentence("sql-" + i, list.get(i).toString()));
			}
			return this;
		}

		public PingConfigurationBuilder withThreads(int threads) {
			if (threads > DEFAULT_THREADS) {
				this.threads = threads;
			}
			return this;
		}

		public PingConfigurationBuilder withTimeBetweenExecutions(long timeBetweenExecutions) {
			this.timeBetweenExecutions = timeBetweenExecutions;
			return this;
		}
	}

	private static final long serialVersionUID = -8595562721719896797L;

	private String datasource;
	private int executions;
	private String name;
	private List<SQLSentence> queryList;
	private int threads;
	private long timeBetweenExecutions;

	private PingJobConfiguration(final PingConfigurationBuilder builder) {
		this.datasource = builder.datasource;
		this.executions = builder.executions;
		this.name = builder.name;
		this.queryList = builder.queryList;
		this.threads = builder.threads;
		this.timeBetweenExecutions = builder.timeBetweenExecutions;
	}

	public String getDatasource() {
		return datasource;
	}

	public int getExecutions() {
		return executions;
	}

	public String getName() {
		return name;
	}

	public List<SQLSentence> getQueryList() {
		return queryList;
	}

	public int getThreads() {
		return threads;
	}

	public long getTimeBetweenExecutions() {
		return timeBetweenExecutions;
	}
}
