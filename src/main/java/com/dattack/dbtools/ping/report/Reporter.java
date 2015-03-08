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
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import com.dattack.dbtools.ping.LogEntry;
import com.dattack.dbtools.ping.log.CSVLogReader;

/**
 * @author cvarela
 * @since 0.1
 */
public class Reporter {

    public void execute(final File dataFile, final ReportContext context) throws IOException, ParseException {

        CSVLogReader logReader = new CSVLogReader(dataFile);

        // Javascript file
        String jsFilename = dataFile.getName() + ".js";
        PrintWriter jsWriter = new PrintWriter(new File(dataFile.getParent(), jsFilename), "UTF-8");
        createJS(context, jsWriter, logReader);
        jsWriter.close();

        // HTML file
        String htmlFilename = dataFile.getName() + ".html";
        PrintWriter htmlWriter = new PrintWriter(new File(dataFile.getParent(), htmlFilename), "UTF-8");
        createHTML(htmlWriter, jsFilename, dataFile.getName());
        htmlWriter.close();

        logReader.close();
    }

    private void createJS(final ReportContext context, final PrintWriter writer, final CSVLogReader logReader)
            throws IOException, ParseException {

        ReportStats stats = new ReportStats(context);

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
            LogEntry item = logReader.next();
            if (item == null) {
                break;
            }

            // apply time filters
            if (item.getStartTime() < reportStartDateFilter || item.getStartTime() > reportEndDateFilter) {
                continue;
            }

            startDate = Math.min(startDate, item.getStartTime());
            endDate = Math.max(endDate, item.getStartTime());

            final List<EntryStats> entryStatsList = stats.add(item);

            for (EntryStats entryStats : entryStatsList) {
                String line = String.format("{x: '%s', y: %d, group: %d}",
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

        for (EntryGroup entryGroup : stats.getEntryGroups()) {
            writer.println(String.format(
                    "groups.add({id: '%d', content: '%s', options: {drawPoints: {style: 'circle'}}});",
                    entryGroup.getId(), entryGroup.getName()));

        }

        writer.println("var container = document.getElementById('visualization');");
        writer.println("var dataset = new vis.DataSet(items);");
        writer.println(String.format("var options = {defaultGroup: 'ungrouped',legend: true,start: '%s',end: '%s'};",
                context.getDateFormat().format(new Date(startDate)), //
                context.getDateFormat().format(new Date(endDate))));
        writer.println("var graph2d = new vis.Graph2d(container, dataset, groups, options);");
    }

    private void createHTML(final PrintWriter writer, final String jsFile, final String logFile) {

        writer.println("<!DOCTYPE HTML>");
        writer.println("<html>");
        writer.println("<head>");
        writer.println("  <title>DBPing</title>");
        writer.println("  <meta content='text/html;charset=utf-8' http-equiv='Content-Type'>");
        writer.println("  <meta content='utf-8' http-equiv='encoding'>");
        writer.println("  <link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css'>");
        writer.println("  <link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css'>");
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
}
