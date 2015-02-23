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
package com.dattack.dbtools.ping.log;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import com.dattack.csv.CSVConfiguration;
import com.dattack.csv.CSVObject;
import com.dattack.csv.CSVReader;
import com.dattack.dbtools.ping.LogEntry;

/**
 * @author cvarela
 *
 */
public class CSVLogReader implements LogReader {

    private final CSVReader reader;
    private final CSVConfiguration configuration;

    public CSVLogReader(final File dataFile) {
        configuration = new CSVConfigurationFactory().create();
        reader = new CSVReader(configuration, dataFile);
    }

    @Override
    public synchronized LogEntry next() throws IOException, ParseException {

        CSVObject rawObject = reader.next();

        if (rawObject == null) {
            return null;
        }

        int index = 0;
        long startTime = configuration.getDateFormat().parse(rawObject.get(index++)).getTime();
        String taskName = rawObject.get(index++);
        String threadName = rawObject.get(index++);
        long iteration = Long.valueOf(rawObject.get(index++));
        String sqlLabel = rawObject.get(index++);

        LogEntry logEntry = new LogEntry(taskName, threadName, iteration, sqlLabel);
        logEntry.setStartTime(startTime);
        logEntry.setRows(Long.valueOf(rawObject.get(index++)));
        logEntry.setConnectionTime(Long.valueOf(rawObject.get(index++)));
        logEntry.setFirstRowTime(Long.valueOf(rawObject.get(index++)));
        logEntry.setExecutionTime(Long.valueOf(rawObject.get(index++)));

        return logEntry;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
