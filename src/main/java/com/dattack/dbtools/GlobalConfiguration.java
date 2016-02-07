/*
 * Copyright (c) 2016, The Dattack team (http://www.dattack.com)
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
package com.dattack.dbtools;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.dattack.ext.io.FilesystemUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public final class GlobalConfiguration {

    private static final String DBTOOLS_CONFIGURATION_FILENAME = "dbtools.properties";

    /** The application's version. */
    public static final String DBTOOLS_VERSION = "0.1";

    public static final String TEMPLATES_DIRECTORY_KEY = "dbtools.templates.directory";

    public static final String DRULES_TEMPLATE_THROWABLE_KEY = "drules.template.throwable";
    public static final String DRULES_TEMPLATE_EMAIL_KEY = "drules.template.email";

    public static final String DRULES_CONFIGURATION_FILE_KEY = "drules.configuration.filename";

    public static final String DRULES_NOTIFICATIONS_FILE_KEY = "drules.notifications.filename";

    private static volatile Configuration configuration;

    private GlobalConfiguration() {

    }

    /**
     * Load the global configuration from 'dbtools.properties' file.
     * 
     * @return the global configuration
     * @throws ConfigurationException
     *             if an error occurs
     */
    public static Configuration getConfiguration() throws ConfigurationException {

        Configuration conf = configuration;
        if (conf == null) {
            synchronized (GlobalConfiguration.class) {
                conf = configuration;
                if (conf == null) {
                    conf = new PropertiesConfiguration(FilesystemUtils.locate(DBTOOLS_CONFIGURATION_FILENAME));
                    configuration = conf;
                }
            }
        }
        return conf;
    }

    public static String getProperty(final String name) throws ConfigurationException {
        return getConfiguration().getString(name);
    }
}
