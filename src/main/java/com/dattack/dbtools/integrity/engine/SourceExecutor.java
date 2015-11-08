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
package com.dattack.dbtools.integrity.engine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.integrity.beans.Identifier;
import com.dattack.dbtools.integrity.beans.SourceBean;
import com.dattack.dbtools.integrity.beans.SqlQueryBean;
import com.dattack.ext.jdbc.JDBCUtils;
import com.dattack.ext.jdbc.JNDIDataSource.DataSourceBuilder;
import com.dattack.ext.misc.ConfigurationUtil;

/**
 * @author cvarela
 * @since 0.1
 */
final class SourceExecutor implements Callable<SourceResult> {

    private static final Logger log = LoggerFactory.getLogger(SourceExecutor.class);

    private final SourceBean sourceBean;

    SourceExecutor(final SourceBean sourceBean) {
        this.sourceBean = sourceBean;
    }

    private Connection getConnection() throws SQLException {

        DataSource dataSource = new DataSourceBuilder().withJNDIName(sourceBean.getJndi()).build();
        return dataSource.getConnection();
    }

    private void populateConfiguration(final AbstractConfiguration configuration, final Identifier queryAlias,
            final ResultSet rs) throws SQLException {

        if (rs == null) {
            return;
        }

        if (rs.next()) {
            if (queryAlias != null) {
                for (int columnIndex = 1; columnIndex <= rs.getMetaData().getColumnCount(); columnIndex++) {
                    String columnName = rs.getMetaData().getColumnLabel(columnIndex);
                    Object value = rs.getObject(columnIndex);
                    String key = queryAlias.append(columnName).getValue();
                    configuration.setProperty(key, value);
                }
            }
        }
    }

    private ResultSet executeStatement(final Statement statement, final String sql) throws SQLException {

        log.info("Executing SQL sentence [Thread: {}]: {}", Thread.currentThread().getName(), sql);

        boolean isResultSet = statement.execute(sql);

        if (isResultSet) {
            return statement.getResultSet();
        }

        return null;
    }

    @Override
    public SourceResult call() throws Exception {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            log.info("Configuring datasource with JNDI name: '{}'", sourceBean.getJndi());

            connection = getConnection();
            statement = connection.createStatement();
            resultSet = null;

            final AbstractConfiguration configuration = ExecutionContext.getInstance().getConfiguration();

            for (Iterator<SqlQueryBean> it = sourceBean.getSqlList().iterator(); it.hasNext();) {

                final SqlQueryBean bean = it.next();

                String interpolatedSql = ConfigurationUtil.interpolate(bean.getSql(), configuration);
                resultSet = executeStatement(statement, interpolatedSql);

                if (it.hasNext()) {
                    populateConfiguration(configuration, bean.getId(), resultSet);
                }
            }

            return new SourceResult(sourceBean.getId(), connection, statement, resultSet);

        } catch (Exception e) {
            JDBCUtils.closeQuietly(resultSet);
            JDBCUtils.closeQuietly(statement);
            JDBCUtils.closeQuietly(connection);
            throw e;
        }
    }
}
