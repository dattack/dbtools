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
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.dattack.dbtools.Builder;

/**
 * Implementation of a <code>DataSource</code> that references a object registered in a JNDI context.
 *
 * @author cvarela
 * @since 0.1
 */
public final class JNDIDataSource extends AbstractDataSource {

    private final String jndiName;

    /**
     * The <code>Builder</code> pattern implementation of <code>JNDIDataSource</code>.
     */
    public static final class DataSourceBuilder implements Builder<DataSource> {

        private String jndiName;

        @Override
        public DataSource build() {
            return new JNDIDataSource(this);
        }

        /**
         * Sets the JNDI name.
         *
         * @param value
         *            the JNDI name
         * @return the self object
         */
        public DataSourceBuilder withJndiName(final String value) {
            jndiName = value;
            return this;
        }
    }

    private JNDIDataSource(final DataSourceBuilder builder) {
        this.jndiName = builder.jndiName;
    }

    @Override
    public Connection getConnection() throws SQLException {

        try {
            final InitialContext context = new InitialContext();
            final DataSource dataSource = (DataSource) context.lookup(jndiName);
            if (dataSource == null) {
                throw new SQLException("Unknown JNDI resource '" + jndiName + "'");
            }
            return dataSource.getConnection();
        } catch (final NamingException e) {
            throw new SQLException(
                    String.format("Unable to get a connection from JNDI name '%s': %s", jndiName, e.getMessage()), e);
        }
    }

    @Override
    public Connection getConnection(final String username, final String pwd) throws SQLException {

        throw new UnsupportedOperationException("Not implemented");
    }
}
