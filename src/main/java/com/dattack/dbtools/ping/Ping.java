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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dattack.dbtools.ping.log.FileLogWriter;
import com.dattack.dbtools.ping.log.LogHeader;
import com.dattack.dbtools.ping.log.LogWriter;
import com.dattack.ext.jdbc.JNDIDataSource.DataSourceBuilder;

/**
 * @author cvarela
 * @since 0.1
 */
public final class Ping {

	private static final Log log = LogFactory.getLog(Ping.class);

	public static void main(final String[] args) {

		try {

			if (args.length < 1) {
				log.fatal("Usage: Ping <configuration_file> [<configuration_file [...]]");
				return;
			}

			final Ping ping = new Ping();
			ping.execute(args);

		} catch (final Exception e) {
			log.fatal(e.getMessage(), e);
		}
	}

	// private final ExecutorService pool;
	private final ThreadPool pool;

	public Ping() {
		pool = new ThreadPool();
	}

	private void execute(final File file) throws ConfigurationException {

		log.info("file: " + file);

		if (file.isDirectory()) {

			final FilenameFilter filter = new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".xml");
				}
			};

			for (final File child : file.listFiles(filter)) {
				execute(child);
			}

		} else {

			List<PingJobConfiguration> pingJobConfList = PingJobConfigurationParser.parse(file);
			for (final PingJobConfiguration pingJobConf : pingJobConfList) {

				DataSource dataSource = new DataSourceBuilder().withJNDIName(pingJobConf.getDatasource()).build();

				// TODO: the sentence provider must be configured
				SQLSentenceProvider sentenceProvider = new SQLSentenceRoundRobinProvider(pingJobConf.getQueryList());

				final LogWriter logWriter = new FileLogWriter(pingJobConf.getLogFile());

				LogHeader logHeader = new LogHeader(pingJobConf);
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

	private class ThreadPool {

		private List<Thread> threadList;

		public ThreadPool() {
			this.threadList = new ArrayList<Thread>();
		}

		public void submit(Runnable task, String threadName) {

			Thread thread = new Thread(task, threadName);
			thread.start();
			threadList.add(thread);
		}
	}
}
