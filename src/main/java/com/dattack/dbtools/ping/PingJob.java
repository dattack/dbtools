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
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.ping.log.LogEntry;
import com.dattack.dbtools.ping.log.LogWriter;
import com.dattack.ext.jdbc.JDBCUtils;

/**
 * Executes a ping-job instance.
 * 
 * @author cvarela
 * @since 0.1
 */
class PingJob implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PingJob.class);

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

        log.info("Running job '{}' at thread '{}'", configuration.getName(), threadName);

        long iter = 0;

        while (testLoop(iter)) {
            iter++;
            Connection connection = null;
            Statement stmt = null;
            ResultSet rs = null;

            // retrieve the SQL to be executed
            SQLSentence sqlSentence = sentenceProvider.nextSQL();

            final LogEntry logEntry = new LogEntry(configuration.getName(), threadName, iter, sqlSentence.getLabel());

            try {

                connection = dataSource.getConnection();

                // sets the connection time
                logEntry.connect();

                // execute the query
                stmt = connection.createStatement();
                rs = stmt.executeQuery(sqlSentence.getSql());

                while (rs.next()) {
                    logEntry.incrRows();
                }

                // sets the total time
                logEntry.end();

                logWriter.write(logEntry);

            } catch (final Exception e) {
                logEntry.setException(e);
                logWriter.write(logEntry);
                log.warn("Job error (job-name: '{}', thread: '{}'): {}", configuration.getName(), threadName,
                        e.getMessage());
            } finally {
                JDBCUtils.closeQuietly(rs);
                JDBCUtils.closeQuietly(stmt);
                JDBCUtils.closeQuietly(connection);
            }

            if (testLoop(iter) && configuration.getTimeBetweenExecutions() > 0) {
                synchronized (this) {
                    try {
                        wait(configuration.getTimeBetweenExecutions());
                    } catch (final Exception e) {
                        log.warn(e.getMessage());
                    }
                }
            }
        }

        log.info("Job finished (job-name: '{}', thread: '{}')", configuration.getName(), threadName);
    }

    private boolean testLoop(final long iteration) {
        return configuration.getExecutions() <= 0 || iteration < configuration.getExecutions();
    }
}