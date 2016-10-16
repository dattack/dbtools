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
package com.dattack.dbtools.drules.engine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.drules.beans.ForEachBean;
import com.dattack.dbtools.drules.beans.Identifier;
import com.dattack.dbtools.drules.beans.Identifier.IdentifierBuilder;
import com.dattack.dbtools.drules.beans.SourceBean;
import com.dattack.dbtools.drules.beans.SourceCommandBean;
import com.dattack.dbtools.drules.beans.SourceCommandBeanVisitor;
import com.dattack.dbtools.drules.beans.SqlQueryBean;
import com.dattack.dbtools.drules.exceptions.DrulesNestableException;
import com.dattack.jtoolbox.commons.configuration.ConfigurationUtil;
import com.dattack.jtoolbox.jdbc.JNDIDataSource;

/**
 * @author cvarela
 * @since 0.1
 */
final class SourceExecutor implements Callable<SourceResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceExecutor.class);

    private final SourceBean sourceBean;
    private final Configuration initialConfiguration;

    private class DefaultSourceCommandBeanVisitor implements SourceCommandBeanVisitor {

        private final AbstractConfiguration configuration;
        private final Connection connection;
        private final Map<Identifier, ResultSet> resultSetMap;
        private ResultSet lastResultSet;

        DefaultSourceCommandBeanVisitor(final Connection connection) {
            configuration = new CompositeConfiguration(ThreadContext.getInstance().getConfiguration());
            this.connection = connection;
            this.resultSetMap = new HashMap<>();
        }

        private void executeForEachLoop(final ForEachBean bean) {
            for (final SourceCommandBean child : bean.getCommandList()) {
                child.accept(this);
            }
        }

        private void forEachRef(final ForEachBean bean) {

            if (StringUtils.isBlank(bean.getRef())) {
                throw new NullPointerException("Invalid foreach loop (missing 'ref' value)");
            }

            final Identifier identifier = new IdentifierBuilder().withValue(bean.getRef()).build();
            try {

                final ResultSet resultSet = resultSetMap.get(identifier);
                if (resultSet == null) {
                    throw new NullPointerException(String.format("Missing ResultSet named '%s'", bean.getRef()));
                }

                do {
                    populateConfigurationFromResultSet(identifier, resultSet);
                    executeForEachLoop(bean);
                } while (resultSet.next());
            } catch (final SQLException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        private void forEachValue(final ForEachBean bean) {

            for (final String value : bean.getValuesList()) {
                configuration.setProperty(bean.getKey(), value);
                executeForEachLoop(bean);
            }
        }

        private ResultSet getLastResultSet() {
            return lastResultSet;
        }

        private void populateConfigurationFromFirstRows() throws SQLException {

            for (final Entry<Identifier, ResultSet> entry : resultSetMap.entrySet()) {
                populateConfigurationFromFirstRows(entry.getKey(), entry.getValue());
            }
        }

        private void populateConfigurationFromFirstRows(final Identifier identifier, final ResultSet resultSet)
                throws SQLException {

            if (resultSet.isBeforeFirst() && resultSet.next() && identifier != null) {
                for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
                    final String columnName = resultSet.getMetaData().getColumnLabel(columnIndex);
                    final Object value = resultSet.getObject(columnIndex);
                    final String key = identifier.append(columnName).getValue();
                    configuration.setProperty(key, value);
                }
            }
        }

        private void populateConfigurationFromResultSet(final Identifier identifier, final ResultSet resultSet)
                throws SQLException {

            if (resultSet.isBeforeFirst()) {
                resultSet.next();
            }

            if (identifier != null) {
                for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
                    final String columnName = resultSet.getMetaData().getColumnLabel(columnIndex);
                    final Object value = resultSet.getObject(columnIndex);
                    final String key = identifier.append(columnName).getValue();
                    configuration.setProperty(key, value);
                }
            }
        }

        private void setLastValues(final SqlQueryBean sqlQueryBean, final ResultSet resultSet) {
            this.lastResultSet = resultSet;
            if (resultSet != null) {
                resultSetMap.put(sqlQueryBean.getId(), resultSet);
            }
        }

        @Override
        public void visit(final ForEachBean bean) {

            if (StringUtils.isNotBlank(bean.getRef())) {
                // check REF construction
                forEachRef(bean);
            } else {
                forEachValue(bean);
            }
        }

        @Override
        public void visit(final SqlQueryBean bean) {

            Statement statement = null;
            try {

                populateConfigurationFromFirstRows();

                final String interpolatedSql = ConfigurationUtil.interpolate(bean.getSql(), configuration);
                statement = connection.createStatement();
                final ResultSet resultSet = executeStatement(statement, interpolatedSql);
                setLastValues(bean, resultSet);

            } catch (final SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Connection getConnection(final String jndiName) throws SQLException {
        return new JNDIDataSource(jndiName).getConnection();
    }

    SourceExecutor(final SourceBean sourceBean, final Configuration initialConfiguration) {
        this.sourceBean = sourceBean;
        this.initialConfiguration = initialConfiguration;
    }

    @Override
    public SourceResult call() throws DrulesNestableException {

        try {
            ThreadContext.getInstance().setInitialConfiguration(initialConfiguration);

            final String jndiName = getInterpolatedJndiName();

            LOGGER.info("Configuring datasource with JNDI name: '{}'", jndiName);
            final Connection connection = getConnection(jndiName);

            final DefaultSourceCommandBeanVisitor visitor = new DefaultSourceCommandBeanVisitor(connection);
            for (final Iterator<SourceCommandBean> it = sourceBean.getCommandList().iterator(); it.hasNext();) {
                final SourceCommandBean command = it.next();
                command.accept(visitor);
            }
            return new SourceResult(sourceBean.getId(), connection, visitor.getLastResultSet());
        } catch (final SQLException e) {
            throw new DrulesNestableException(e);
        }
    }

    private ResultSet executeStatement(final Statement statement, final String sql) throws SQLException {

        LOGGER.info("Executing SQL sentence [{}@{}]: {}", Thread.currentThread().getName(), sourceBean.getId(), sql);

        final boolean isResultSet = statement.execute(sql);

        if (isResultSet) {
            return statement.getResultSet();
        }

        return null;
    }

    private String getInterpolatedJndiName() {
        return ThreadContext.getInstance().interpolate(sourceBean.getJndi());
    }
}
