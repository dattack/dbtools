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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basic datasource implementation.
 * 
 * @author cvarela
 * @since 0.1
 */
public final class SimpleDataSource extends AbstractDataSource {

    private static final Log log = LogFactory.getLog(SimpleDataSource.class);

    private final String username;
    private final String password;
    private final String url;
    private final String driver;
    private volatile boolean ensureDriverLoadedNeeded;

    public SimpleDataSource(final String driver, final String url, final String username, final String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        ensureDriverLoadedNeeded = true;
    }

    private synchronized void ensureDriverLoaded() {

        if (!ensureDriverLoadedNeeded) {
            return;
        }

        try {
            Class.forName(driver).newInstance();
            ensureDriverLoadedNeeded = false;
        } catch (final Exception e) {
            log.error(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public Connection getConnection() throws SQLException {
        if (ensureDriverLoadedNeeded) {
            ensureDriverLoaded();
        }
        return this.getConnection(username, password);
    }

    /** {@inheritDoc} */
    @Override
    public Connection getConnection(final String user, final String pass) throws SQLException {

        if (ensureDriverLoadedNeeded) {
            ensureDriverLoaded();
        }

        if ((user == null) || (pass == null)) {
            return DriverManager.getConnection(url);
        }

        return DriverManager.getConnection(url, user, pass);
    }

    @Override
    public String toString() {
        return driver + ":" + username + "@" + url;
    }
}
