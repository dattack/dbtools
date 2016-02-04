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
	private final Configuration initialConfiguration;

	SourceExecutor(final SourceBean sourceBean, final Configuration initialConfiguration) {
		this.sourceBean = sourceBean;
		this.initialConfiguration = initialConfiguration;
	}

	private ResultSet executeStatement(final Statement statement, final String sql) throws SQLException {

		log.info("Executing SQL sentence [{}@{}]: {}", Thread.currentThread().getName(), sourceBean.getId(), sql);

		boolean isResultSet = statement.execute(sql);

		if (isResultSet) {
			return statement.getResultSet();
		}

		return null;
	}

	private Connection getConnection(final String jndiName) throws SQLException {
		return new DataSourceBuilder().withJNDIName(jndiName).build().getConnection();
	}

	private String getInterpolatedJNDIName() {
		return ThreadContext.getInstance().interpolate(sourceBean.getJndi());
	}

	@Override
	public SourceResult call() throws Exception {

		ThreadContext.getInstance().setInitialConfiguration(initialConfiguration);

		final String jndiName = getInterpolatedJNDIName();

		log.info("Configuring datasource with JNDI name: '{}'", jndiName);
		Connection connection = getConnection(jndiName);

		DefaultSourceCommandBeanVisitor visitor = new DefaultSourceCommandBeanVisitor(connection);
		try {
			for (Iterator<SourceCommandBean> it = sourceBean.getCommandList().iterator(); it.hasNext();) {
				SourceCommandBean command = it.next();
				command.accept(visitor);
			}
			return new SourceResult(sourceBean.getId(), connection, visitor.getLastResultSet());
		} finally {
		}
	}

	private class DefaultSourceCommandBeanVisitor implements SourceCommandBeanVisitor {

		private final AbstractConfiguration configuration;
		private final Connection connection;
		private final Map<Identifier, ResultSet> resultSetMap;
		private ResultSet lastResultSet;

		DefaultSourceCommandBeanVisitor(final Connection connection) {
			configuration = new CompositeConfiguration(ThreadContext.getInstance().getConfiguration());
			this.connection = connection;
			this.resultSetMap = new HashMap<Identifier, ResultSet>();
		}

		/**
		 * @return the lastResultSet
		 */
		public ResultSet getLastResultSet() {
			return lastResultSet;
		}

		private void populateConfigurationFromFirstRows(final Identifier identifier, final ResultSet resultSet)
				throws SQLException {

			if (resultSet.isBeforeFirst() && resultSet.next()) {
				if (identifier != null) {
					for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
						String columnName = resultSet.getMetaData().getColumnLabel(columnIndex);
						Object value = resultSet.getObject(columnIndex);
						String key = identifier.append(columnName).getValue();
						configuration.setProperty(key, value);
					}
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
					String columnName = resultSet.getMetaData().getColumnLabel(columnIndex);
					Object value = resultSet.getObject(columnIndex);
					String key = identifier.append(columnName).getValue();
					configuration.setProperty(key, value);
				}
			}
		}

		private void populateConfigurationFromFirstRows() throws SQLException {

			for (Entry<Identifier, ResultSet> entry : resultSetMap.entrySet()) {
				populateConfigurationFromFirstRows(entry.getKey(), entry.getValue());
			}
		}

		private void setLastValues(final SqlQueryBean sqlQueryBean, final ResultSet resultSet) {
			this.lastResultSet = resultSet;
			if (resultSet != null) {
				resultSetMap.put(sqlQueryBean.getId(), resultSet);
			}
		}

		@Override
		public void visite(final SqlQueryBean bean) {

			Statement statement = null;
			ResultSet resultSet = null;

			try {

				populateConfigurationFromFirstRows();

				statement = connection.createStatement();
				String interpolatedSql = ConfigurationUtil.interpolate(bean.getSql(), configuration);
				resultSet = executeStatement(statement, interpolatedSql);

				setLastValues(bean, resultSet);

			} catch (SQLException e) {
				JDBCUtils.closeQuietly(resultSet);
				JDBCUtils.closeQuietly(statement);
				throw new RuntimeException(e);
			}
		}

		@Override
		public void visite(final ForEachBean bean) {

			if (StringUtils.isNotBlank(bean.getRef())) {
				// check REF construction
				forEachRef(bean);
			} else {
				forEachValue(bean);
			}
		}

		private void forEachRef(final ForEachBean bean) {

			if (StringUtils.isBlank(bean.getRef())) {
				throw new NullPointerException("Invalid foreach loop (missing 'ref' value)");
			}

			Identifier identifier = new IdentifierBuilder().withValue(bean.getRef()).build();
			ResultSet resultSet = resultSetMap.get(identifier);

			if (resultSet == null) {
				throw new NullPointerException(String.format("Missing ResultSet named '%s'", bean.getRef()));
			}

			try {
				do {
					populateConfigurationFromResultSet(identifier, resultSet);
					executeForEachLoop(bean);
				} while (resultSet.next());
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			}
		}

		private void forEachValue(final ForEachBean bean) {

			for (final String value : bean.getValuesList()) {
				configuration.setProperty(bean.getKey(), value);
				executeForEachLoop(bean);
			}
		}

		private void executeForEachLoop(final ForEachBean bean) {
			for (final SourceCommandBean child : bean.getCommandList()) {
				child.accept(this);
			}
		}
	}
}
