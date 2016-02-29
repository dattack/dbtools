/*
 * Copyright (c) 2015, The Dattack team (http://www.dattack.com)
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
package com.dattack.dbtools.ping.report;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import com.dattack.dbtools.ping.LogEntry;
import com.dattack.dbtools.ping.log.CSVFileLogReader;

/**
 * @author cvarela
 * @since 0.1
 */
public class Reporter {

    private static void createHtml(final PrintWriter writer, final String jsFile, final String logFile) {

        writer.println("<!DOCTYPE HTML>");
        writer.println("<html>");
        writer.println("<head>");
        writer.println("  <title>DBPing</title>");
        writer.println("  <meta content='text/html;charset=utf-8' http-equiv='Content-Type'>");
        writer.println("  <meta content='utf-8' http-equiv='encoding'>");
        writer.println(
                "  <link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css'>");
        writer.println(
                "  <link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css'>");
        writer.println("  <script src='https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js'></script>");
        writer.println("  <script src='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js'></script>");
        writer.println("  <script src='http://visjs.org/dist/vis.js'></script>");
        writer.println("  <link href='http://visjs.org/dist/vis.css' rel='stylesheet' type='text/css' />");
        writer.println("</head>");
        writer.println("<body>");
        writer.println(String.format("<h3>Log file: %s</h3>", logFile));
        writer.println("<br />");
        writer.println("<div id='visualization'></div>");
        writer.println(String.format("<script src='%s'></script>", jsFile));
        writer.println("</body>");
        writer.println("</html>");
    }

    private static void createJs(final ReportContext context, final PrintWriter writer,
            final CSVFileLogReader logReader) throws IOException {

        final ReportStats reportStats = new ReportStats(context);

        writer.println("var items = [");

        int items = 0;

        long startDate = Long.MAX_VALUE;
        long endDate = Long.MIN_VALUE;

        long reportStartDateFilter = -1;
        long reportEndDateFilter = Long.MAX_VALUE;

        if (context.getStartDate() != null) {
            reportStartDateFilter = context.getStartDate().getTime();
        }

        if (context.getEndDate() != null) {
            reportEndDateFilter = context.getEndDate().getTime();
        }

        while (true) {
            final LogEntry item = logReader.next();
            if (item == null) {
                break;
            }

            // apply time filters
            if (item.getEventTime() < reportStartDateFilter || item.getEventTime() > reportEndDateFilter) {
                continue;
            }

            startDate = Math.min(startDate, item.getEventTime());
            endDate = Math.max(endDate, item.getEventTime());

            final List<EntryStats> entryStatsList = reportStats.add(item);

            for (final EntryStats entryStats : entryStatsList) {
                final String line = String.format("{x: '%s', y: %d, group: %d}",
                        context.getDateFormat().format(new Date(entryStats.getX())), entryStats.getY(),
                        entryStats.getGroup());
                if (items > 0) {
                    writer.print(",");
                }
                writer.print(line);
                items++;
            }
        }

        writer.println("];");
        writer.println("var groups = new vis.DataSet();");

        for (final EntryGroup entryGroup : reportStats.getEntryGroups()) {

            final GroupStats groupStats = reportStats.getGroupStats(entryGroup.getId());
            if (groupStats != null) {
                System.out.format("%n%nGroup (%d): %s%n", entryGroup.getId(), entryGroup.getName());
                System.out.format("Elements: %d%n", groupStats.getStatistics().getN());
                System.out.format("Min. value: %s%n", groupStats.getStatistics().getMin());
                System.out.format("Max. value: %s%n", groupStats.getStatistics().getMax());
                System.out.format("Mean: %s%n", groupStats.getStatistics().getMean());
                System.out.format("Standard deviation: %s%n", groupStats.getStatistics().getStandardDeviation());
            }
            writer.println(
                    String.format("groups.add({id: '%d', content: '%s', options: {drawPoints: {style: 'circle'}}});",
                            entryGroup.getId(), entryGroup.getName()));

        }

        writer.println("var container = document.getElementById('visualization');");
        writer.println("var dataset = new vis.DataSet(items);");
        writer.println(String.format("var options = {defaultGroup: 'ungrouped',legend: true,start: '%s',end: '%s'};",
                context.getDateFormat().format(new Date(startDate)), //
                context.getDateFormat().format(new Date(endDate))));
        writer.println("var graph2d = new vis.Graph2d(container, dataset, groups, options);");
    }

    /**
     * Process all data from an input file and generates a HTML report.
     *
     * @param dataFile
     *            the input file
     * @param context
     *            the report context
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void execute(final File dataFile, final ReportContext context) throws IOException {

        try (CSVFileLogReader logReader = new CSVFileLogReader(dataFile)) {

            // Javascript file
            final String jsFilename = dataFile.getName() + ".js";
            try (PrintWriter jsWriter = new PrintWriter(new File(dataFile.getParent(), jsFilename), "UTF-8")) {
                createJs(context, jsWriter, logReader);
            }

            // HTML file
            final String htmlFilename = dataFile.getName() + ".html";
            try (PrintWriter htmlWriter = new PrintWriter(new File(dataFile.getParent(), htmlFilename), "UTF-8")) {
                createHtml(htmlWriter, jsFilename, dataFile.getName());
            }
        }
    }
}
