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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.ping.beans.DbpingBean;
import com.dattack.dbtools.ping.beans.DbpingParser;
import com.dattack.dbtools.ping.beans.DbpingParserException;
import com.dattack.dbtools.ping.beans.PingTaskBean;
import com.dattack.dbtools.ping.log.CSVFileLogWriter;
import com.dattack.dbtools.ping.log.LogHeader;
import com.dattack.dbtools.ping.log.LogWriter;
import com.dattack.jtoolbox.commons.configuration.ConfigurationUtil;
import com.dattack.jtoolbox.io.FilesystemUtils;
import com.dattack.jtoolbox.jdbc.JNDIDataSource;

/**
 * @author cvarela
 * @since 0.1
 */
public final class Ping {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ping.class);

    private static final String FILE_OPTION = "f";
    private static final String LONG_FILE_OPTION = "file";
    private static final String TASK_NAME_OPTION = "t";
    private static final String LONG_TASK_NAME_OPTION = "task";

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

    private static Options createOptions() {

        final Options options = new Options();

        options.addOption(Option.builder(FILE_OPTION) //
                .required(true) //
                .longOpt(LONG_FILE_OPTION) //
                .hasArg(true) //
                .argName("DBPING_FILE") //
                .desc("the path of the file containing the DBPing configuration") //
                .build());

        options.addOption(Option.builder(TASK_NAME_OPTION) //
                .required(false) //
                .longOpt(LONG_TASK_NAME_OPTION) //
                .hasArg(true) //
                .argName("TASK_NAME") //
                .desc("the name of the task to execute") //
                .build());

        return options;
    }

    private static SqlCommandProvider getSentenceProvider(final String clazzname) {

        SqlCommandProvider sentenceProvider = null;

        if (clazzname != null) {
            try {
                sentenceProvider = (SqlCommandProvider) Class.forName(clazzname).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                LOGGER.trace(String.format("Using default SqlSentenceProvider: %s", e.getMessage()));
                // ignore
            }
        }

        if (sentenceProvider == null) {
            sentenceProvider = new SqlCommandRoundRobinProvider();
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

        final Options options = createOptions();

        try {
            final CommandLineParser parser = new DefaultParser();
            final CommandLine cmd = parser.parse(options, args);
            final String[] filenames = cmd.getOptionValues(FILE_OPTION);
            final String[] taskNames = cmd.getOptionValues(TASK_NAME_OPTION);

            HashSet<String> hs = null;
            if (taskNames != null) {
                hs = new HashSet<>(Arrays.asList(taskNames));
            }

            final Ping ping = new Ping();
            ping.execute(filenames, hs);

        } catch (@SuppressWarnings("unused") final ParseException e) {
            showUsage(options);
        } catch (final ConfigurationException | DbpingParserException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void showUsage(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        final int descPadding = 5;
        final int leftPadding = 4;
        formatter.setDescPadding(descPadding);
        formatter.setLeftPadding(leftPadding);
        final String header = "\n";
        final String footer = "\nPlease report issues at https://github.com/dattack/dbtools/issues";
        formatter.printHelp("dbping ", header, options, footer, true);
    }

    private Ping() {
        pool = new ThreadPool();
    }

    private void execute(final File file, final Set<String> taskNames)
            throws ConfigurationException, DbpingParserException {

        if (file.isDirectory()) {

            final File[] files = file.listFiles(FilesystemUtils.createFilenameFilterByExtension("xml"));
            if (files != null) {
                for (final File child : files) {
                    execute(child, taskNames);
                }
            }

        } else {

            final DbpingBean dbpingBean = DbpingParser.parse(file);
            for (final PingTaskBean pingTaskBean : dbpingBean.getTaskList()) {

                if (taskNames != null && !taskNames.isEmpty() && !taskNames.contains(pingTaskBean.getName())) {
                    continue;
                }

                final CompositeConfiguration conf = new CompositeConfiguration();
                conf.setProperty("task.name", pingTaskBean.getName());
                conf.addConfiguration(ConfigurationUtil.createEnvSystemConfiguration());

                final DataSource dataSource = new JNDIDataSource(pingTaskBean.getDatasource());

                final SqlCommandProvider sentenceProvider = getSentenceProvider(pingTaskBean.getCommandProvider());
                sentenceProvider.setSentences(pingTaskBean.getSqlStatementList());

                final LogWriter logWriter = new CSVFileLogWriter(
                        ConfigurationUtil.interpolate(pingTaskBean.getLogFile(), conf));

                final LogHeader logHeader = new LogHeader(pingTaskBean);
                logWriter.write(logHeader);

                for (int i = 0; i < pingTaskBean.getThreads(); i++) {
                    pool.submit(new PingJob(pingTaskBean, dataSource, sentenceProvider, logWriter),
                            pingTaskBean.getName() + "@Thread-" + i);
                }
            }
        }
    }

    private void execute(final String[] filenames, final Set<String> taskNames)
            throws ConfigurationException, DbpingParserException {

        for (final String filename : filenames) {
            execute(new File(filename), taskNames);
        }
    }
}
