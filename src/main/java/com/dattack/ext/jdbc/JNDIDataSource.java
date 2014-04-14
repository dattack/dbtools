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
package com.dattack.ext.jdbc;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dattack.dbtools.Builder;

/**
 * @author cvarela
 * @since 0.1
 */
public class JNDIDataSource extends AbstractDataSource {

	public static class DataSourceBuilder implements Builder<DataSource> {

		private String jndiName;
		private String path;

		public DataSource build() {
			return new JNDIDataSource(this);
		}

		public DataSourceBuilder withJNDIName(final String value) {
			jndiName = value;
			return this;
		}

		public DataSourceBuilder withPath(final String value) {
			path = value;
			return this;
		}
	}

	private static final Log log = LogFactory.getLog(JNDIDataSource.class);

	private final String driverPath;
	private boolean init;
	private final String jndiName;

	private JNDIDataSource(final DataSourceBuilder builder) {
		this.driverPath = builder.path;
		this.jndiName = builder.jndiName;
		init = false;
	}

	private void configureClasspath() {

		if (driverPath != null) {
			final File file = new File(driverPath);
			if (file.exists()) {
				try {
					if (file.isDirectory()) {
						configureDirectoryClasspath(file);
					} else {
						configureJarClasspath(file);
					}
				} catch (final Exception e) {
					log.warn(e.getMessage());
				}
			} else {
				log.info("Missing resource: " + driverPath);
			}
		}
	}

	private void configureClasspath(final List<URL> urlList) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		final URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		final Class<?> urlClass = URLClassLoader.class;
		final Method method = urlClass.getDeclaredMethod("addURL", new Class[] { URL.class });
		method.setAccessible(true);

		for (final URL u : urlList) {
			method.invoke(urlClassLoader, new Object[] { u });
		}
	}

	private void configureDirectoryClasspath(final File directory) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		final File[] jars = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getAbsolutePath().endsWith(".jar");
			}
		});

		final List<URL> urlList = new ArrayList<URL>();
		for (final File jar : jars) {
			try {
				log.info("Scanning JAR: " + jar);
				urlList.add(jar.toURI().toURL());
			} catch (final MalformedURLException e) {
				log.warn(e.getMessage());
			}
		}

		configureClasspath(urlList);
	}

	private void configureJarClasspath(final File jar) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		final List<URL> urlList = new ArrayList<URL>();
		try {
			log.info("Scanning JAR: " + jar);
			urlList.add(jar.toURI().toURL());
			configureClasspath(urlList);
		} catch (final MalformedURLException e) {
			log.warn(e.getMessage());
		}
	}

	public Connection getConnection() throws SQLException {

		if (init) {
			configureClasspath();
			init = false;
		}

		try {
			final InitialContext context = new InitialContext();
			final DataSource ds = (DataSource) context.lookup(jndiName);
			if (ds == null) {
				throw new SQLException("Unknown JNDI resource '" + jndiName + "'");
			}
			final Connection connection = ds.getConnection();
			return connection;
		} catch (final NamingException e) {
			throw new SQLException("Unable to get a connection from JNDI name '" + jndiName + "': " + e.getMessage());
		}
	}

	public Connection getConnection(String username, String pwd) throws SQLException {

		throw new UnsupportedOperationException("Not implemented");
	}
}
