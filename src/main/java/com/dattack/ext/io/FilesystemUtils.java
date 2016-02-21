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
package com.dattack.ext.io;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of useful methods to deal with filesystem operations.
 *
 * @author cvarela
 * @since 0.1
 */
public final class FilesystemUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilesystemUtils.class);

    /**
     * Gets the extension of a filename.
     *
     * @param file
     *            he file to retrieve the extension of.
     * @return the extension of the file or an empty string if none exists.
     */
    public static String getFileExtension(final File file) {

        if (file == null) {
            throw new IllegalArgumentException("Unable to get the file extension from 'null'");
        }

        final String fileName = file.getName();
        final int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot > 0) {
            return fileName.substring(lastIndexOfDot + 1);
        }
        return "";
    }

    /**
     * Finds the file with the given name.
     *
     * @param path
     *            the file name
     * @return the <code>File</code>
     */
    public static File locate(final String path) {
        
        if (path == null) {
            throw new IllegalArgumentException("Unable to locate the file 'null'");
        }

        final URL url = FilesystemUtils.class.getClassLoader().getResource(path);
        if (url != null) {
            try {
                final URI uri = new URI(url.toExternalForm());
                return new File(uri);
            } catch (final URISyntaxException e) {
                // URI syntax error? we have a valid URL
                LOGGER.warn(e.getMessage());
                return new File(path);
            }
        }

        return new File(path);
    }

    private FilesystemUtils() {
        // static class
    }
}
