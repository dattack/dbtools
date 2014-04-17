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
import java.nio.file.Paths;
import java.text.MessageFormat;

/**
 * A collection of useful methods to deal with filesystem operations.
 * 
 * @author cvarela
 * @since 0.1
 */
public final class FilesystemUtils {

	private FilesystemUtils() {
		// static class
	}

	public static File locate(final String path) {

		final URL url = FilesystemUtils.class.getClassLoader().getResource(path);
		if (url != null) {
			try {
				final URI uri = new URI(url.toExternalForm());
				return new File(uri);
			} catch (final URISyntaxException e) {
				// URI syntax error? we have a valid URL 
			}
		}
		return new File(path);
	}

	public static String readFileToString(final String path) throws IOException {
		return readFileToString(path, Charset.defaultCharset());
	}

	public static String readFileToString(final Path path) throws IOException {
		return readFileToString(path, Charset.defaultCharset());
	}

	public static String readFileToString(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public static String readFileToString(final Path path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(path);
		return new String(encoded, encoding);
	}

	public static void mkdirs(File directory) throws IOException {
		if (directory.exists()) {
			if (directory.isFile()) {
				throw new IOException(MessageFormat.format(
						"Unable to create directory: file {0} exists but is not a directory", directory));
			}
		} else {
			if (!directory.mkdirs()) {
				throw new IOException("Unable to create directory " + directory);
			}
		}
	}
}
