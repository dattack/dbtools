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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public final class SimpleDataSource extends AbstractDataSource {

	private final String username;
	private final String password;
	private final String url;
	private final String driver;

	public SimpleDataSource(final String driver, final String url, final String username, final String password) {
		ensureLoaded(driver);
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	private static boolean ensureLoaded(final String name) {
		try {
			Class.forName(name).newInstance();
			return true;
		} catch (final Exception e) {
			return false;
		}
	}

	/** {@inheritDoc} */
	public Connection getConnection() throws SQLException {
		return this.getConnection(username, password);
	}

	/** {@inheritDoc} */
	public Connection getConnection(final String user, final String pass) throws SQLException {

		if ((user == null) || (pass == null)) {
			return DriverManager.getConnection(url);
		}

		return DriverManager.getConnection(url, user, pass);
	}

	@Override
	public String toString() {
		return driver + "::::" + url + "::::" + username;
	}

	@Override
	public boolean equals(final Object obj) {

		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}

		SimpleDataSource other = (SimpleDataSource) obj;
		return new EqualsBuilder() //
				.appendSuper(super.equals(obj)) //
				.append(url, other.url) //
				.append(driver, other.driver) //
				.append(username, other.username) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(url).append(username).append(driver).toHashCode();
	}
}
