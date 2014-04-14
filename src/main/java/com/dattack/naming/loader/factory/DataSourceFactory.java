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
package com.dattack.naming.loader.factory;

import java.text.MessageFormat;
import java.util.Properties;

import com.dattack.ext.jdbc.SimpleDataSource;

/**
 * @author cvarela
 * @since 0.1
 */
public class DataSourceFactory implements ResourceFactory<SimpleDataSource> {

	private static final String DRIVER_KEY = "driverClassName";
	private static final String URL_KEY = "url";
	private static final String USERNAME_KEY = "username";
	private static final String PASSWORD_KEY = "password";

	public SimpleDataSource getObjectInstance(final Properties properties) {

		final String driver = properties.getProperty(DRIVER_KEY);
		final String url = properties.getProperty(URL_KEY);
		final String user = properties.getProperty(USERNAME_KEY);
		final String password = properties.getProperty(PASSWORD_KEY);

		if (driver == null) {
			throw new RuntimeException(MessageFormat.format("Missing property ''{0}''", DRIVER_KEY));
		}

		if (url == null) {
			throw new RuntimeException(MessageFormat.format("Missing property ''{0}''", URL_KEY));
		}

		return new SimpleDataSource(driver, url, user, password);
	}
}
