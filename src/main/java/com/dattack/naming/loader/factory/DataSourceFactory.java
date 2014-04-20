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
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.ext.jdbc.DataSourceClasspathDecorator;
import com.dattack.ext.jdbc.SimpleDataSource;
import com.dattack.ext.misc.ConfigurationUtil;

/**
 * @author cvarela
 * @since 0.1
 */
public class DataSourceFactory implements ResourceFactory<DataSource> {

    private static final Logger log = LoggerFactory.getLogger(DataSourceFactory.class);

    private static final String DRIVER_KEY = "driverClassName";
    private static final String URL_KEY = "url";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";

    @Override
    public DataSource getObjectInstance(final Properties properties, final List<String> extraClasspath) {

        CompositeConfiguration configuration = ConfigurationUtil.createEnvSystemConfiguration();
        configuration.addConfiguration(new MapConfiguration(properties));

        final String driver = configuration.getString(DRIVER_KEY);
        final String url = configuration.getString(URL_KEY);
        final String user = configuration.getString(USERNAME_KEY);
        final String password = configuration.getString(PASSWORD_KEY);

        if (driver == null) {
            throw new RuntimeException(MessageFormat.format("Missing property ''{0}''", DRIVER_KEY));
        }

        if (url == null) {
            throw new RuntimeException(MessageFormat.format("Missing property ''{0}''", URL_KEY));
        }

        DataSource ds = null;
        try {
            Properties props = ConfigurationConverter.getProperties(configuration);
            ds = BasicDataSourceFactory.createDataSource(props);
        } catch (final Exception e) {
            // we will use a DataSource without a connection pool
            log.warn(e.getMessage());
            ds = new SimpleDataSource(driver, url, user, password);
        }

        return new DataSourceClasspathDecorator(ds, extraClasspath);
    }
}
