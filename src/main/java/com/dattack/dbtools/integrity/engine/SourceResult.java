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
import java.sql.Statement;

import com.dattack.dbtools.integrity.beans.Identifier;
import com.dattack.ext.jdbc.JDBCUtils;

/**
 * @author cvarela
 * @since 0.1
 */
final class SourceResult {

	private final Identifier sourceAlias;
	private final Connection connection;
	private final ResultSet resultSet;

	public SourceResult(final Identifier sourceAlias, final Connection connection, final ResultSet resultSet) {
		this.sourceAlias = sourceAlias;
		this.connection = connection;
		this.resultSet = resultSet;
	}

	public Identifier getSourceAlias() {
		return sourceAlias;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public void close() {
		JDBCUtils.closeQuietly(resultSet);
		JDBCUtils.closeQuietly(connection);
	}
}
