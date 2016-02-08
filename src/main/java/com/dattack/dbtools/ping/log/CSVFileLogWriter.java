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
package com.dattack.dbtools.ping.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.csv.CSVStringBuilder;
import com.dattack.dbtools.ping.DataRow;
import com.dattack.dbtools.ping.LogEntry;
import com.dattack.dbtools.ping.SQLSentence;
import com.dattack.ext.io.IOUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public class CSVFileLogWriter implements LogWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVFileLogWriter.class);

    private final CSVStringBuilder csvBuilder;
    private final String filename;

    public CSVFileLogWriter(final String filename) {
        this.filename = filename;
        this.csvBuilder = new CSVStringBuilder(new CSVConfigurationFactory().create());
    }

    private void addDataRowList(final List<DataRow> list) {
        for (int i = 0; i < list.size(); i++) {
            final DataRow row = list.get(i);
            csvBuilder.comment().append(String.format(" Row %d:\t", i));
            for (final Object obj : row.getData()) {
                csvBuilder.append(ObjectUtils.toString(obj));
            }
            csvBuilder.eol();
        }
    }

    private String format(final LogEntry entry) {

        String data = null;
        synchronized (csvBuilder) {
            csvBuilder.append(new Date(entry.getEventTime())) //
                    .append(StringUtils.trimToEmpty(entry.getTaskName())) //
                    .append(StringUtils.trimToEmpty(entry.getThreadName())) //
                    .append(entry.getIteration()) //
                    .append(StringUtils.trimToEmpty(entry.getSqlLabel())) //
                    .append(entry.getRows()) //
                    .append(entry.getConnectionTime()) //
                    .append(entry.getFirstRowTime()) //
                    .append(entry.getTotalTime());

            if (entry.getException() != null) {
                csvBuilder.append(normalize(entry.getException().getMessage()));
            }
            csvBuilder.eol();
            addDataRowList(entry.getRowList());

            data = csvBuilder.toString();
            csvBuilder.clear();
        }
        return data;
    }

    private String format(final LogHeader header) {

        String data = null;
        synchronized (csvBuilder) {

            csvBuilder.comment();

            final List<String> keys = new ArrayList<String>(header.getProperties().keySet());
            Collections.sort(keys);

            for (final String key : keys) {
                csvBuilder.comment(new StringBuilder() //
                        .append(normalize(ObjectUtils.toString(key))) //
                        .append(": ") //
                        .append(normalize(ObjectUtils.toString(header.getProperties().get(key)))) //
                        .toString() //
                );
            }

            csvBuilder.comment("SQL Sentences:");
            for (final SQLSentence sentence : header.getPingJobConfiguration().getQueryList()) {
                csvBuilder.comment(new StringBuilder().append("  ").append(sentence.getLabel()).append(": ")
                        .append(normalize(sentence.getSql())).toString());
            }

            csvBuilder.comment() //
                    .append("date") //
                    .append("task-name") //
                    .append("thread-name") //
                    .append("iteration") //
                    .append("sql-label") //
                    .append("rows") //
                    .append("connection-time") //
                    .append("first-row-time") //
                    .append("total-time") //
                    .append("message").eol();

            data = csvBuilder.toString();
            csvBuilder.clear();
        }
        return data;
    }

    private FileOutputStream getOutputStream() throws FileNotFoundException {

        final File file = new File(filename);
        if (!file.exists()) {
            final File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    LOGGER.warn("Unable to create directory: {}", parent);
                }
            }
        }
        return new FileOutputStream(file, true);
    }

    private String normalize(final String text) {
        return text.replaceAll("\n", " ");
    }

    @Override
    public void write(final LogEntry logEntry) {
        write(format(logEntry));
    }

    @Override
    public synchronized void write(final LogHeader logHeader) {
        write(format(logHeader));
    }

    private void write(final String message) {

        FileOutputStream out = null;
        try {
            out = getOutputStream();
            out.write(message.getBytes());
        } catch (final IOException e) {
            LOGGER.warn(e.getMessage());
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
}
