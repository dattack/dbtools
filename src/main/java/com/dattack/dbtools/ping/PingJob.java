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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.ping.LogEntry.LogEntryBuilder;
import com.dattack.dbtools.ping.log.LogWriter;

/**
 * Executes a ping-job instance.
 *
 * @author cvarela
 * @since 0.1
 */
class PingJob implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingJob.class);

    private final PingJobConfiguration configuration;
    private final DataSource dataSource;
    private final SQLSentenceProvider sentenceProvider;
    private final LogWriter logWriter;

    public PingJob(final PingJobConfiguration configuration, final DataSource dataSource,
            final SQLSentenceProvider sentenceProvider, final LogWriter logWriter) {

        this.configuration = configuration;
        this.dataSource = dataSource;
        this.sentenceProvider = sentenceProvider;
        this.logWriter = logWriter;
    }

    @Override
    public void run() {

        final String threadName = Thread.currentThread().getName();

        LOGGER.info("Running job '{}' at thread '{}'", configuration.getName(), threadName);

        long iter = 0;

        final LogEntryBuilder logEntryBuilder = new LogEntryBuilder(configuration.getMaxRowsToDump()) //
                .withTaskName(configuration.getName()) //
                .withThreadName(threadName);

        while (testLoop(iter)) {
            iter++;
            // retrieve the SQL to be executed
            final SQLSentence sqlSentence = sentenceProvider.nextSql();

            try (Connection connection = dataSource.getConnection()) {

                logEntryBuilder.init().withSqlLabel(sqlSentence.getLabel()) //
                        .withIteration(iter);

                // sets the connection time
                logEntryBuilder.connect();

                // execute the query
                try (Statement stmt = connection.createStatement()) {
                    try (ResultSet resultSet = stmt.executeQuery(sqlSentence.getSql())) {

                        while (resultSet.next()) {
                            logEntryBuilder.addRow(resultSet);
                        }

                        // sets the total time
                        logWriter.write(logEntryBuilder.build());
                    }
                }

            } catch (final SQLException e) {
                logWriter.write(logEntryBuilder.withException(e).build());
                LOGGER.warn("Job error (job-name: '{}', thread: '{}'): {}", configuration.getName(), threadName,
                        e.getMessage());
            }

            if (testLoop(iter) && configuration.getTimeBetweenExecutions() > 0) {
                synchronized (this) {
                    try {
                        wait(configuration.getTimeBetweenExecutions());
                    } catch (final InterruptedException e) {
                        LOGGER.warn(e.getMessage());
                    }
                }
            }
        }

        LOGGER.info("Job finished (job-name: '{}', thread: '{}')", configuration.getName(), threadName);
    }

    private boolean testLoop(final long iteration) {
        return configuration.getExecutions() <= 0 || iteration < configuration.getExecutions();
    }
}
