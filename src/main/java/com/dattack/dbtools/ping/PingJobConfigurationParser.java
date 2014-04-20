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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.ping.PingJobConfiguration.PingConfigurationBuilder;

/**
 * @author cvarela
 * @since 0.1
 */
final class PingJobConfigurationParser {

    private static final Logger log = LoggerFactory.getLogger(PingJobConfigurationParser.class);

    private static final String TASK_KEY = "task";
    private static final String EXECUTIONS_KEY = "[@executions]";
    private static final String NAME_KEY = "[@name]";
    private static final String THREADS_KEY = "[@threads]";
    private static final String TIME_BETWEEN_EXECUTIONS_KEY = "[@timeBetweenExecutions]";
    private static final String DATASOURCE_KEY = "[@datasource]";
    private static final String QUERY_KEY = "query";
    private static final String QUERY_LABEL_KEY = "[@label]";
    private static final String THIS_KEY = ""; // empty
    private static final String LOG_FILE_KEY = "log-file";

    private PingJobConfigurationParser() {
        // static class
    }

    public static List<PingJobConfiguration> parse(final File file) throws ConfigurationException {

        log.debug("parsing file '{}'", file);

        List<PingJobConfiguration> list = new ArrayList<PingJobConfiguration>();

        final XMLConfiguration config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        config.load(file);

        final List<HierarchicalConfiguration> taskList = config.configurationsAt(TASK_KEY);

        for (final HierarchicalConfiguration taskElement : taskList) {

            PingConfigurationBuilder builder = new PingConfigurationBuilder() //
                    .withName(taskElement.getString(NAME_KEY)) //
                    .withDatasource(taskElement.getString(DATASOURCE_KEY));

            final List<HierarchicalConfiguration> queryElementList = taskElement.configurationsAt(QUERY_KEY);
            for (final HierarchicalConfiguration queryElement : queryElementList) {
                String label = queryElement.getString(QUERY_LABEL_KEY);
                String sql = queryElement.getString(THIS_KEY);
                builder.withQuery(new SQLSentence(label, sql));
            }

            if (taskElement.containsKey(EXECUTIONS_KEY)) {
                builder.withExecutions(taskElement.getInt(EXECUTIONS_KEY));
            }

            if (taskElement.containsKey(THREADS_KEY)) {
                builder.withThreads(taskElement.getInt(THREADS_KEY));
            }

            if (taskElement.containsKey(TIME_BETWEEN_EXECUTIONS_KEY)) {
                builder.withTimeBetweenExecutions(taskElement.getLong(TIME_BETWEEN_EXECUTIONS_KEY));
            }

            builder.withLogFile(taskElement.getString(LOG_FILE_KEY));

            list.add(builder.build());
        }

        return list;
    }
}