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
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.ext.io.FilesystemUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public final class DataSourceClasspathDecorator extends DataSourceDecorator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JNDIDataSource.class);

    private final List<String> extraClasspath;
    private volatile boolean initialized;

    public DataSourceClasspathDecorator(final DataSource inner, final List<String> extraClasspath) {
        super(inner);
        this.extraClasspath = new ArrayList<String>(extraClasspath);
        initialized = false;
    }

    private synchronized void configureClasspath() {

        if (initialized) {
            return;
        }

        for (String path : extraClasspath) {
            final File file = FilesystemUtils.locate(path);
            if (file.exists()) {
                try {
                    if (file.isDirectory()) {
                        configureDirectoryClasspath(file);
                    } else {
                        configureJarClasspath(file);
                    }
                } catch (final Exception e) {
                    LOGGER.warn(e.getMessage());
                }
            } else {
                LOGGER.debug("Missing directory/file: '{}'", path);
            }
        }

        initialized = true;
    }

    private void configureClasspath(final List<URL> urlList) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {

        try (final URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader()) {
            final Class<?> urlClass = URLClassLoader.class;
            final Method method = urlClass.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);

            for (final URL u : urlList) {
                method.invoke(urlClassLoader, new Object[] { u });
            }
        }
    }

    private void configureDirectoryClasspath(final File directory) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {

        final File[] jars = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pathname.isFile() && pathname.getAbsolutePath().endsWith(".jar");
            }
        });

        final List<URL> urlList = new ArrayList<URL>();
        for (final File jar : jars) {
            try {
                LOGGER.info("Scanning JAR: {}", jar);
                urlList.add(jar.toURI().toURL());
            } catch (final MalformedURLException e) {
                LOGGER.warn(e.getMessage());
            }
        }

        configureClasspath(urlList);
    }

    private void configureJarClasspath(final File jar) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {

        final List<URL> urlList = new ArrayList<URL>();
        try {
            LOGGER.info("Scanning JAR: {}", jar);
            urlList.add(jar.toURI().toURL());
            configureClasspath(urlList);
        } catch (final MalformedURLException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    @Override
    public Connection getConnection() throws SQLException {

        if (!initialized) {
            configureClasspath();
        }

        return super.getConnection();
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        if (!initialized) {
            configureClasspath();
        }
        return super.getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        if (!initialized) {
            configureClasspath();
        }
        return super.getLogWriter();
    }

    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        if (!initialized) {
            configureClasspath();
        }
        super.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        if (!initialized) {
            configureClasspath();
        }
        super.setLoginTimeout(seconds);

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        if (!initialized) {
            configureClasspath();
        }
        return super.getLoginTimeout();
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        if (!initialized) {
            configureClasspath();
        }
        return super.getParentLogger();
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (!initialized) {
            configureClasspath();
        }
        return super.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        if (!initialized) {
            configureClasspath();
        }
        return super.isWrapperFor(iface);
    }
}
