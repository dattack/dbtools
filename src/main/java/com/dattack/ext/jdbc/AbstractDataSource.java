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

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public abstract class AbstractDataSource implements DataSource {

	/*
	 * A value of zero means that the timeout is the default system timeout if there is one; otherwise, it means that
	 * there is no timeout
	 */
	private static final int DEFAULT_TIMEOUT_VALUE = 0;

	/** {@inheritDoc} */
	public PrintWriter getLogWriter() throws SQLException {
		// null -> logging is disabled
		return null;
	}

	/** {@inheritDoc} */
	public int getLoginTimeout() throws SQLException {
		return DEFAULT_TIMEOUT_VALUE;
	}

	/** {@inheritDoc} */
	public void setLogWriter(final PrintWriter logWriter) throws SQLException {
		// ignore
	}

	/** {@inheritDoc} */
	public void setLoginTimeout(final int timeout) throws SQLException {
		throw new UnsupportedOperationException("Not supported by BasicDataSource");
	}

	/** {@inheritDoc} */
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		return false;
	}

	/** {@inheritDoc} */
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		throw new SQLException("This object is not a wrapper");
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}
}
