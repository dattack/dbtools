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
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;

import com.dattack.dbtools.ping.report.MetricName;
import com.dattack.dbtools.ping.report.ReportContext;
import com.dattack.dbtools.ping.report.Reporter;
import com.dattack.ext.util.TimeUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public final class PingAnalyzer {

    private static final String START_DATE_OPTION = "start_date";
    private static final String END_DATE_OPTION = "end_date";
    private static final String SPAN_OPTION = "time_span";
    private static final String METRIC_OPTION = "metric";
    private static final String DATA_FILE_OPTION = "file";
    private static final String MAX_VALUE_OPTION = "max";
    private static final String MIN_VALUE_OPTION = "min";

    /**
     * The <code>main</code> method.
     *
     * @param args
     *            the program arguments
     */
    public static void main(final String[] args) {

        try {

            // create Options object
            final Options options = new Options();

            // add t option
            options.addOption(START_DATE_OPTION, true, "the date for an analysis run to begin");
            options.addOption(END_DATE_OPTION, true, "the date for an analysis run to finish");
            options.addOption(SPAN_OPTION, true, "the period of time between points");
            options.addOption(DATA_FILE_OPTION, true, "the data file to analyze");
            options.addOption(METRIC_OPTION, true, "the metric to analyze");
            options.addOption(MAX_VALUE_OPTION, true, "the maximum value to use");
            options.addOption(MIN_VALUE_OPTION, true, "the minimum value to use");

            final CommandLineParser parser = new DefaultParser();
            final CommandLine cmd = parser.parse(options, args);

            final ReportContext context = new ReportContext();
            context.setStartDate(TimeUtils.parseDate(cmd.getOptionValue(START_DATE_OPTION)));
            context.setEndDate(TimeUtils.parseDate(cmd.getOptionValue(END_DATE_OPTION)));
            context.setTimeSpan(TimeUtils.parseTimeSpanMillis(cmd.getOptionValue(SPAN_OPTION)));
            context.setMaxValue(parseLong(cmd.getOptionValue(MAX_VALUE_OPTION)));
            context.setMinValue(parseLong(cmd.getOptionValue(MIN_VALUE_OPTION)));
            if (cmd.hasOption(METRIC_OPTION)) {
                for (final String metricName : cmd.getOptionValues(METRIC_OPTION)) {
                    context.addMetricNameFilter(MetricName.parse(metricName));
                }
            }

            final PingAnalyzer ping = new PingAnalyzer();
            for (final String file : cmd.getOptionValues(DATA_FILE_OPTION)) {
                ping.execute(new File(file), context);
            }

        } catch (final ParseException | ConfigurationException e) {
            System.err.println(e.getMessage());
        }
    }

    /* TODO: remove this method and create a NumberUtils. */
    private static Long parseLong(final String txt) {

        try {
            if (txt != null) {
                return Long.valueOf(txt);
            }
        } catch (final NumberFormatException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    private PingAnalyzer() {
    }

    private void execute(final File file, final ReportContext context) throws ConfigurationException {

        if (file.isDirectory()) {

            final FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(final File dir, final String name) {
                    return name.toLowerCase().endsWith(".log");
                }
            };

            execute(file.listFiles(filter), context);

        } else {

            try {
                final Reporter reporter = new Reporter();
                reporter.execute(file, context);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void execute(final File[] files, final ReportContext context) throws ConfigurationException {
        
        if (files == null) {
            return;
        }
        
        for (final File child : files) {
            execute(child, context);
        }
    }
}
