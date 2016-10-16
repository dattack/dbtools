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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.ping.log.CSVFileLogWriter;
import com.dattack.dbtools.ping.log.LogHeader;
import com.dattack.dbtools.ping.log.LogWriter;
import com.dattack.jtoolbox.jdbc.JNDIDataSource;

/**
 * @author cvarela
 * @since 0.1
 */
public final class Ping {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ping.class);

    // private final ExecutorService pool;
    private final ThreadPool pool;

    private class ThreadPool {

        private final List<Thread> threadList;

        public ThreadPool() {
            this.threadList = new ArrayList<>();
        }

        public void submit(final Runnable task, final String threadName) {

            final Thread thread = new Thread(task, threadName);
            thread.start();
            threadList.add(thread);
        }
    }

    private static SQLSentenceProvider getSentenceProvider(final String clazzname) {

        SQLSentenceProvider sentenceProvider = null;

        if (clazzname != null) {
            try {
                sentenceProvider = (SQLSentenceProvider) Class.forName(clazzname).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                LOGGER.trace(String.format("Using default SqlSentenceProvider: %s", e.getMessage()));
                // ignore
            }
        }

        if (sentenceProvider == null) {
            sentenceProvider = new SQLSentenceRoundRobinProvider();
        }
        return sentenceProvider;
    }

    /**
     * The <code>main</code> method.
     *
     * @param args
     *            the program arguments
     */
    public static void main(final String[] args) {

        try {

            if (args.length < 1) {
                System.err.println("Usage: Ping <configuration_file> [<configuration_file [...]]");
                return;
            }

            final Ping ping = new Ping();
            ping.execute(args);

        } catch (final ConfigurationException e) {
            System.err.println(e.getMessage());
        }
    }

    private Ping() {
        pool = new ThreadPool();
    }

    private void execute(final File file) throws ConfigurationException {

        if (file.isDirectory()) {

            final FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(final File dir, final String name) {
                    return name.toLowerCase().endsWith(".xml");
                }
            };

            final File[] files = file.listFiles(filter);
            if (files != null) {
                for (final File child : files) {
                    execute(child);
                }
            }

        } else {

            final List<PingJobConfiguration> pingJobConfList = PingJobConfigurationParser.parse(file);
            for (final PingJobConfiguration pingJobConf : pingJobConfList) {

                final DataSource dataSource = new JNDIDataSource(pingJobConf.getDatasource());

                final SQLSentenceProvider sentenceProvider = getSentenceProvider(pingJobConf.getProviderClassName());
                sentenceProvider.setSentences(pingJobConf.getQueryList());

                final LogWriter logWriter = new CSVFileLogWriter(pingJobConf.getLogFile());

                final LogHeader logHeader = new LogHeader(pingJobConf);
                logWriter.write(logHeader);

                for (int i = 0; i < pingJobConf.getThreads(); i++) {
                    pool.submit(new PingJob(pingJobConf, dataSource, sentenceProvider, logWriter),
                            pingJobConf.getName() + "@Thread-" + i);
                }
            }
        }
    }

    private void execute(final String[] args) throws ConfigurationException {

        for (final String filename : args) {
            execute(new File(filename));
        }
    }
}
