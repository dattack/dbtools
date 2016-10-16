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

import java.util.HashMap;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.jtoolbox.commons.configuration.ConfigurationUtil;

/**
 * @author cvarela
 * @since 0.1
 */
public final class ThreadContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadContext.class);

    private static final ThreadLocal<ThreadContext> THREAD_LOCAL = new ThreadLocal<>();

    private MapConfiguration internalConfiguration;

    /**
     * Singleton method.
     *
     * @return the singleton
     */
    public static synchronized ThreadContext getInstance() {

        ThreadContext obj = THREAD_LOCAL.get();
        if (obj == null) {
            obj = new ThreadContext();
            THREAD_LOCAL.set(obj);
        }
        return obj;
    }

    private ThreadContext() {
    }

    public void clearProperty(final String key) {
        getInternalConfiguration().clearProperty(key);
    }

    public Configuration getConfiguration() {
        return getInternalConfiguration();
    }

    private MapConfiguration getInternalConfiguration() {

        if (internalConfiguration == null) {
            setInitialConfiguration();
        }
        return internalConfiguration;
    }

    public String interpolate(final String value) {
        return ConfigurationUtil.interpolate(value, getInternalConfiguration());
    }

    private void setInitialConfiguration() {
        setInitialConfiguration(null);
    }

    /**
     * Sets the initial configuration to use.
     *
     * @param configuration
     *            the initial configuration
     */
    public void setInitialConfiguration(final Configuration configuration) {

        if (internalConfiguration != null) {
            LOGGER.warn("InitialConfiguration ");
        }

        final CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
        compositeConfiguration.setDelimiterParsingDisabled(true);
        compositeConfiguration.addConfiguration(new SystemConfiguration());
        if (configuration != null) {
            compositeConfiguration.addConfiguration(configuration);
        }
        internalConfiguration = new MapConfiguration(new HashMap<String, Object>());
        internalConfiguration.copy(compositeConfiguration);
    }

    public void setProperty(final String key, final Object value) {
        getInternalConfiguration().setProperty(key, value);
    }
}
