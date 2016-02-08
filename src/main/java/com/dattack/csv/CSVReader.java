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
package com.dattack.csv;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import com.dattack.ext.io.IOUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public class CSVReader implements Closeable {

    private final CSVConfiguration configuration;
    private final File dataFile;
    private BufferedReader bufferedReader;

    public CSVReader(final CSVConfiguration configuration, final File dataFile) {
        this.configuration = configuration;
        this.dataFile = dataFile;
    }

    private BufferedReader getReader() throws IOException {

        if (bufferedReader == null) {
            bufferedReader = new BufferedReader(new FileReader(dataFile));
        }
        return bufferedReader;
    }

    private boolean isComment(final String line) {
        return (line.charAt(0) == configuration.getCommentChar());
    }

    /**
     * Iterate over the reader and returns the next object.
     * 
     * @return the next object o <tt>null</tt> if no one exists
     * @throws IOException
     *             if an I/O error occurs
     */
    public synchronized CSVObject next() throws IOException {

        String line = "";
        CSVObject object = null;
        while ((object == null) && (line = StringUtils.trimToNull(getReader().readLine())) != null) {
            if (isComment(line)) {
                continue;
            }
            final String[] data = line.split(configuration.getSeparator());
            object = new CSVObject(data);
        }

        return object;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(bufferedReader);
    }
}
