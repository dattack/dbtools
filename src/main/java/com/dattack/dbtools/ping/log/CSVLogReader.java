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
import com.dattack.dbtools.ping.LogEntry.LogEntryBuilder;

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

        return new LogEntryBuilder() //
                .withEventTime(configuration.getDateFormat().parse(rawObject.get(index++)).getTime()) //
                .withTaskName(rawObject.get(index++)) //
                .withThreadName(rawObject.get(index++)) //
                .withIteration(Long.valueOf(rawObject.get(index++))) //
                .withSqlLabel(rawObject.get(index++)) //
                .withRows(Long.valueOf(rawObject.get(index++))) //
                .withConnectionTime(Long.valueOf(rawObject.get(index++))) //
                .withFirstRowTime(Long.valueOf(rawObject.get(index++))) //
                .withTotalTime(Long.valueOf(rawObject.get(index++))) //
                .build();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
