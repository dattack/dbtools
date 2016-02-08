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

    private static final Logger LOGGER = LoggerFactory.getLogger(PingJobConfigurationParser.class);

    private static final float DEFAULT_WEIGHT_VALUE = 1F;
    private static final String TASK_KEY = "task";
    private static final String PROVIDER_KEY = "[@provider]";
    private static final String EXECUTIONS_KEY = "[@executions]";
    private static final String NAME_KEY = "[@name]";
    private static final String THREADS_KEY = "[@threads]";
    private static final String TIME_BETWEEN_EXECUTIONS_KEY = "[@timeBetweenExecutions]";
    private static final String MAX_ROWS_TO_DUMP_KEY = "[@maxRowsToDump]";
    private static final String DATASOURCE_KEY = "[@datasource]";
    private static final String QUERY_KEY = "query";
    private static final String QUERY_LABEL_KEY = "[@label]";
    private static final String QUERY_WEIGHT_KEY = "[@weight]";
    private static final String THIS_KEY = ""; // empty
    private static final String LOG_FILE_KEY = "log-file";

    public static List<PingJobConfiguration> parse(final File file) throws ConfigurationException {

        LOGGER.debug("parsing file '{}'", file);

        final List<PingJobConfiguration> list = new ArrayList<PingJobConfiguration>();

        final XMLConfiguration config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        config.load(file);

        final List<HierarchicalConfiguration> taskList = config.configurationsAt(TASK_KEY);

        for (final HierarchicalConfiguration taskElement : taskList) {

            final PingConfigurationBuilder builder = new PingConfigurationBuilder() //
                    .withName(taskElement.getString(NAME_KEY)) //
                    .withDatasource(taskElement.getString(DATASOURCE_KEY));

            final List<HierarchicalConfiguration> queryElementList = taskElement.configurationsAt(QUERY_KEY);
            for (final HierarchicalConfiguration queryElement : queryElementList) {
                final String label = queryElement.getString(QUERY_LABEL_KEY);
                final String sql = queryElement.getString(THIS_KEY);
                final float weight = queryElement.getFloat(QUERY_WEIGHT_KEY, DEFAULT_WEIGHT_VALUE);
                builder.withQuery(new SQLSentence(label, sql, weight));
            }

            if (taskElement.containsKey(PROVIDER_KEY)) {
                builder.withProviderClassName(taskElement.getString(PROVIDER_KEY));
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

            if (taskElement.containsKey(MAX_ROWS_TO_DUMP_KEY)) {
                builder.withMaxRowsToDump(taskElement.getLong(MAX_ROWS_TO_DUMP_KEY));
            }

            builder.withLogFile(taskElement.getString(LOG_FILE_KEY));

            list.add(builder.build());
        }

        return list;
    }

    private PingJobConfigurationParser() {
        // static class
    }
}
