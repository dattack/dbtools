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

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.lang.StringUtils;

import com.dattack.dbtools.Builder;
import com.dattack.ext.misc.ConfigurationUtil;

/**
 * @author cvarela
 * @since 0.1
 */
public final class PingJobConfiguration implements Serializable {

    /**
     * The <code>Builder</code> pattern implementation of <code>PingJobConfiguration</code>.
     */
    public static class PingConfigurationBuilder implements Builder<PingJobConfiguration> {

        private static final String LABEL_PREFIX = "Label-";
        private static final String TASK_NAME_CONFIGURATION_VAR = "task.name";
        private static final String DEFAULT_TASK_NAME = "";
        private static final int DEFAULT_EXECUTIONS = 0; // UNLIMITED
        private static final int DEFAULT_THREADS = 1;
        private static final int DEFAULT_TIME_BETWEEN_EXECUTIONS = 0; // NOT WAIT

        private String datasource;
        private String name;

        // the number of iterations by each thread
        private int executions;

        // SQL query list
        private final List<SQLSentence> queryList;

        // the number of threads to run
        private int threads;

        // the time to wait between two iterations
        private long timeBetweenExecutions;

        private String logFile;

        private final CompositeConfiguration configuration;
        private final BaseConfiguration baseConfiguration;

        public PingConfigurationBuilder() {

            queryList = new ArrayList<SQLSentence>();
            baseConfiguration = new BaseConfiguration();
            configuration = ConfigurationUtil.createEnvSystemConfiguration();
            configuration.addConfiguration(baseConfiguration);

            // default values
            withName(DEFAULT_TASK_NAME);
            withExecutions(DEFAULT_EXECUTIONS);
            withThreads(DEFAULT_THREADS);
            withTimeBetweenExecutions(DEFAULT_TIME_BETWEEN_EXECUTIONS);
        }

        @Override
        public PingJobConfiguration build() {
            return new PingJobConfiguration(this);
        }

        /**
         * Sets the JNDI name of the datasource to use.
         * 
         * @param value
         *            the JNDI name
         * @return self instance
         */
        public PingConfigurationBuilder withDatasource(final String value) {
            this.datasource = value;
            return this;
        }

        /**
         * Sets the number of executions to loop. If the value is zero or negative then executes with no limit. By
         * default, the value is 0 (no limit).
         * 
         * @param value
         *            the number of executions
         * @return self instance
         */
        public PingConfigurationBuilder withExecutions(final int value) {
            this.executions = value;
            return this;
        }

        /**
         * Sets the task's name.
         * 
         * @param value
         *            the task's name
         * @return self instance
         */
        public PingConfigurationBuilder withName(final String value) {
            this.baseConfiguration.setProperty(TASK_NAME_CONFIGURATION_VAR, value);
            this.name = value;
            return this;
        }

        /**
         * Sets the log file pathname.
         * 
         * @param value
         *            the log file pathname.
         * @return self instance
         */
        public PingConfigurationBuilder withLogFile(final String value) {
            this.logFile = value;
            return this;
        }

        /**
         * Adds a new ping-query.
         * 
         * @param sentence
         *            the SQL query
         * @return self instance
         */
        public PingConfigurationBuilder withQuery(final SQLSentence sentence) {
            if (StringUtils.isBlank(sentence.getLabel())) {
                this.queryList.add(new SQLSentence(computeLabel(sentence), sentence.getSql()));
            } else {
                this.queryList.add(sentence);
            }
            return this;
        }

        private String computeLabel(final SQLSentence sentence) {
            return LABEL_PREFIX + sentence.getSql().hashCode();
        }

        /**
         * Sets the number of threads to use.
         * 
         * @param value
         *            the number of threads
         * @return self instance
         */
        public PingConfigurationBuilder withThreads(final int value) {
            if (value >= DEFAULT_THREADS) {
                this.threads = value;
            }
            return this;
        }

        /**
         * Sets the time that the current thread must sleep before execute a new ping-query (in milliseconds). If the
         * time is zero or negative, then the current thread loop without sleep.
         * 
         * @param value
         *            the time to sleep
         * @return self instance
         */
        public PingConfigurationBuilder withTimeBetweenExecutions(final long value) {
            if (value >= 0) {
                this.timeBetweenExecutions = value;
            }
            return this;
        }
    }

    private static final long serialVersionUID = -8595562721719896797L;

    private final String datasource;
    private final int executions;
    private final String name;
    private final List<SQLSentence> queryList;
    private final int threads;
    private final long timeBetweenExecutions;
    private final String logFile;

    private PingJobConfiguration(final PingConfigurationBuilder builder) {
        this.datasource = builder.datasource;
        this.executions = builder.executions;
        this.name = builder.name;
        this.queryList = builder.queryList;
        this.threads = builder.threads;
        this.timeBetweenExecutions = builder.timeBetweenExecutions;
        this.logFile = ConfigurationUtil.interpolate(builder.logFile, builder.configuration);
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

    public String getLogFile() {
        return logFile;
    }
}
