/*
 * Copyright (c) 2016, The Dattack team (http://www.dattack.com)
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
package com.dattack.dbtools.drules;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.GlobalConfiguration;
import com.dattack.dbtools.drules.beans.Identifier;
import com.dattack.dbtools.drules.beans.Identifier.IdentifierBuilder;
import com.dattack.dbtools.drules.engine.DrulesEngine;
import com.dattack.ext.misc.ConfigurationUtil;

/**
 * @author cvarela
 * @since 0.1
 */
public final class DrulesClient {

	private static final Logger log = LoggerFactory.getLogger(DrulesClient.class);

	private static final String DRULES_OPTION = "d";
	private static final String TASK_OPTION = "t";
	private static final String PROPERTIES_OPTION = "p";

	private static Options createOptions() {

		Options options = new Options();

		options.addOption(Option.builder(DRULES_OPTION) //
				.required(true) //
				.longOpt("drules") //
				.hasArg(true) //
				.argName("DRULES_FILE") //
				.desc("the path of the file containing the DRules configuration") //
				.build());

		options.addOption(Option.builder(TASK_OPTION) //
				.required(true) //
				.longOpt("task") //
				.hasArg(true) //
				.argName("TASK_NAME") //
				.desc("the name of the task being performed") //
				.build());

		options.addOption(Option.builder(PROPERTIES_OPTION) //
				.required(false) //
				.longOpt("properties") //
				.hasArg(true) //
				.argName("PROPERTIES") //
				.desc("the path of the file containing custom runtime properties") //
				.build());

		return options;
	}

	private static void execute(final String[] args) throws Exception {

		Options options = createOptions();

		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);
			String filename = cmd.getOptionValue(DRULES_OPTION);
			Identifier taskId = new IdentifierBuilder().withValue(cmd.getOptionValue(TASK_OPTION)).build();

			CompositeConfiguration cc = null;
			if (cmd.hasOption(PROPERTIES_OPTION)) {
				cc = loadProperties(cmd.getOptionValues(PROPERTIES_OPTION));
			}

			final DrulesEngine engine = new DrulesEngine();
			engine.execute(filename, taskId, cc);
		} catch (final ParseException e) {
			showUsage(options);
		}
	}

	private static CompositeConfiguration loadProperties(final String[] propertyFiles) throws ConfigurationException {

		CompositeConfiguration cc = new CompositeConfiguration();
		cc.addConfiguration(GlobalConfiguration.getConfiguration());

		if (propertyFiles != null) {
			for (String filename : propertyFiles) {
				if (StringUtils.isNotBlank(filename)) {
					cc.addConfiguration(new PropertiesConfiguration(ConfigurationUtil.interpolate(filename, cc)));
				}
			}
		}
		return cc;
	}

	/**
	 * @param args
	 *            the arguments
	 */
	public static void main(final String[] args) {

		try {
			execute(args);
			System.exit(0);
		} catch (final Throwable e) {
			log.error(e.getMessage(), e);
			System.exit(-1);
		}
	}

	private static void showUsage(final Options options) {
		HelpFormatter formatter = new HelpFormatter();
		final int descPadding = 5;
		final int leftPadding = 4;
		formatter.setDescPadding(descPadding);
		formatter.setLeftPadding(leftPadding);
		String header = "\n";
		String footer = "\nPlease report issues at https://github.com/dattack/dbtools/issues";
		formatter.printHelp("drules ", header, options, footer, true);
	}

	private DrulesClient() {
		// static class
	}
}
