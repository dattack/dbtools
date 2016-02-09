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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

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

    /**
     * Creates the directory named by this abstract pathname, including any necessary but nonexistent parent
     * directories.
     *
     * @param directory
     *            the directory named
     * @throws IOException
     *             if an I/O error occurs creating the directory hierarchy
     * @deprecated Use {@link org.apache.commons.io.FileUtils#forceMkdir(File)}
     */
    @Deprecated
    public static void mkdirs(final File directory) throws IOException {
        if (directory.exists()) {
            if (directory.isFile()) {
                throw new IOException(MessageFormat
                        .format("Unable to create directory: file {0} exists but is not a directory", directory));
            }
        } else {
            if (!directory.mkdirs()) {
                throw new IOException("Unable to create directory " + directory);
            }
        }
    }

    /**
     * Reads the content of a file.
     *
     * @param path
     *            the file to read
     * @return a <code>String</code> containing the bytes read from the file
     * @throws IOException
     *             if an I/O error occurs reading from the stream
     * @deprecated Use {@link org.apache.commons.io.FileUtils#readFileToString(File)}
     */
    @Deprecated
    public static String readFileToString(final Path path) throws IOException {
        return readFileToString(path, Charset.defaultCharset());
    }

    /**
     * Reads the content of a file.
     *
     * @param path
     *            the file to read
     * @param encoding
     *            The {@link java.nio.charset.Charset charset} to be used to decode the {@code bytes}
     * @return a <code>String</code> containing the bytes read from the file
     * @throws IOException
     *             if an I/O error occurs reading from the stream
     * @deprecated Use {@link org.apache.commons.io.FileUtils#readFileToString(File, Charset)}
     */
    @Deprecated
    public static String readFileToString(final Path path, final Charset encoding) throws IOException {
        final byte[] encoded = Files.readAllBytes(path);
        return new String(encoded, encoding);
    }

    /**
     * Reads the content of a file.
     *
     * @param path
     *            the file to read
     * @return a <code>String</code> containing the bytes read from the file
     * @throws IOException
     *             if an I/O error occurs reading from the stream
     * @deprecated Use {@link org.apache.commons.io.FileUtils#readFileToString(File)}
     */
    @Deprecated
    public static String readFileToString(final String path) throws IOException {
        return readFileToString(path, Charset.defaultCharset());
    }

    /**
     * Reads the content of a file.
     *
     * @param path
     *            the file to read
     * @param encoding
     *            The {@link java.nio.charset.Charset charset} to be used to decode the {@code bytes}
     * @return a <code>String</code> containing the bytes read from the file
     * @throws IOException
     *             if an I/O error occurs reading from the stream
     * @deprecated Use {@link org.apache.commons.io.FileUtils#readFileToString(File, Charset)}
     */
    @Deprecated
    public static String readFileToString(final String path, final Charset encoding) throws IOException {
        final byte[] encoded = Files.readAllBytes(locate(path).toPath());
        return new String(encoded, encoding);
    }

    private FilesystemUtils() {
        // static class
    }
}
