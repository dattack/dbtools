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
import java.text.ParseException;

import org.apache.commons.configuration.ConfigurationException;

import com.dattack.dbtools.ping.report.Reporter;

/**
 * @author cvarela
 * @since 0.1
 */
public final class PingAnalyzer {

    /**
     * The <code>main</code> method.
     *
     * @param args
     *            the program arguments
     */
    public static void main(final String[] args) {

        try {

            if (args.length < 1) {
                System.err.println("Usage: Ping <configuration_file> [<configuration_file [...]]");
                return;
            }

            final PingAnalyzer ping = new PingAnalyzer();
            ping.execute(args);

        } catch (final Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private PingAnalyzer() {
    }

    private void execute(final File file) throws ConfigurationException {

        if (file.isDirectory()) {

            final FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(final File dir, final String name) {
                    return name.toLowerCase().endsWith(".log");
                }
            };

            for (final File child : file.listFiles(filter)) {
                execute(child);
            }

        } else {

            Reporter reporter = new Reporter();
            try {
                reporter.execute(file);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void execute(final String[] args) throws ConfigurationException {

        for (final String filename : args) {
            execute(new File(filename));
        }
    }
}
