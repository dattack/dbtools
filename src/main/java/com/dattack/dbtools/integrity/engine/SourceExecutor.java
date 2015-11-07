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
import org.apache.commons.configuration.BaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.integrity.beans.Identifier;
import com.dattack.dbtools.integrity.beans.SourceBean;
import com.dattack.dbtools.integrity.beans.SqlQueryBean;
import com.dattack.dbtools.integrity.exceptions.ConfigurationMistakeException;
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
                    String key = createKey(queryAlias, columnName);
                    configuration.setProperty(key, value);
                }
            }
        }
    }

    private String createKey(final Identifier queryAlias, final String columnName) {
        return String.format("%s.%s", queryAlias.getValue(), columnName);
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

        try {
            log.info("Configuring datasource with JNDI name: '{}'", sourceBean.getJndi());

            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = null;

            final AbstractConfiguration configuration = new BaseConfiguration();

            for (Iterator<SqlQueryBean> it = sourceBean.getSqlList().iterator(); it.hasNext();) {

                final SqlQueryBean bean = it.next();

                String interpolatedSql = ConfigurationUtil.interpolate(bean.getSql(), configuration);
                resultSet = executeStatement(statement, interpolatedSql);

                if (it.hasNext()) {
                    populateConfiguration(configuration, bean.getId(), resultSet);
                }
            }

            return createSourceResult(sourceBean.getId(), resultSet);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private SourceResult createSourceResult(final Identifier sourceId, final ResultSet resultSet) {

        if (resultSet == null) {
            throw new ConfigurationMistakeException(
                    String.format("[Source: %s] At least one of the SQL statements executed must return a ResultSet",
                            sourceBean.getId()));
        }

        return new SourceResult(sourceId, resultSet);
    }
}
