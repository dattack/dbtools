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
import com.dattack.dbtools.ping.beans.PingTaskBean;
import com.dattack.dbtools.ping.beans.SqlCommandBean;
import com.dattack.dbtools.ping.beans.SqlCommandVisitor;
import com.dattack.dbtools.ping.beans.SqlScriptBean;
import com.dattack.dbtools.ping.beans.SqlStatementBean;
import com.dattack.dbtools.ping.log.LogWriter;
import com.dattack.jtoolbox.jdbc.JDBCUtils;

/**
 * Executes a ping-job instance.
 *
 * @author cvarela
 * @since 0.1
 */
class PingJob implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingJob.class);

    private final PingTaskBean pingTaskBean;
    private final DataSource dataSource;
    private final SqlCommandProvider sentenceProvider;
    private final LogWriter logWriter;

    public PingJob(final PingTaskBean configuration, final DataSource dataSource,
            final SqlCommandProvider sentenceProvider, final LogWriter logWriter) {

        this.pingTaskBean = configuration;
        this.dataSource = dataSource;
        this.sentenceProvider = sentenceProvider;
        this.logWriter = logWriter;
    }

    @Override
    public void run() {

        final String threadName = Thread.currentThread().getName();

        LOGGER.info("Running job '{}' at thread '{}'", pingTaskBean.getName(), threadName);

        long iter = 0;

        final LogEntryBuilder logEntryBuilder = new LogEntryBuilder(pingTaskBean.getMaxRowsToDump()) //
                .withTaskName(pingTaskBean.getName()) //
                .withThreadName(threadName);

        while (testLoop(iter)) {
            final long currentIteration = iter++;
            // retrieve the SQL to be executed
            final SqlCommandBean sqlSentence = sentenceProvider.nextSql();

            logEntryBuilder.init().withSqlLabel(sqlSentence.getLabel()) //
                    .withIteration(iter);

            // sets the connection time
            logEntryBuilder.connect();

            sqlSentence.accept(new SqlCommandVisitor() {

                @Override
                public void visite(final SqlScriptBean command) {

                    for (final SqlStatementBean item : command.getStatementList()) {
                        logEntryBuilder.init().withSqlLabel(item.getLabel()) //
                                .withIteration(currentIteration);

                        // sets the connection time
                        logEntryBuilder.connect();
                        item.accept(this);
                    }
                }

                @Override
                public void visite(final SqlStatementBean command) {

                    try (Connection connection = dataSource.getConnection()) {
                        try (Statement stmt = connection.createStatement()) {
                            ResultSet resultSet = null;
                            try {

                                final boolean executeResult = stmt.execute(command.getSql());
                                if (executeResult) {
                                    resultSet = stmt.getResultSet();
                                    while (resultSet.next()) {
                                        logEntryBuilder.addRow(resultSet);
                                    }
                                } else {
                                    // not a ResultSet
                                }

                                // sets the total time
                                logWriter.write(logEntryBuilder.build());
                            } finally {
                                JDBCUtils.closeQuietly(resultSet);
                            }
                        }
                    } catch (final SQLException e) {
                        logWriter.write(logEntryBuilder.withException(e).build());
                        LOGGER.warn("Job error (job-name: '{}', thread: '{}'): {}", pingTaskBean.getName(), threadName,
                                e.getMessage());
                    }
                }
            });

            if (testLoop(iter) && pingTaskBean.getTimeBetweenExecutions() > 0) {
                synchronized (this) {
                    try {
                        wait(pingTaskBean.getTimeBetweenExecutions());
                    } catch (final InterruptedException e) {
                        LOGGER.warn(e.getMessage());
                    }
                }
            }
        }

        LOGGER.info("Job finished (job-name: '{}', thread: '{}')", pingTaskBean.getName(), threadName);
    }

    private boolean testLoop(final long iteration) {
        return pingTaskBean.getExecutions() <= 0 || iteration < pingTaskBean.getExecutions();
    }
}
