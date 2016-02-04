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

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.ext.misc.ConfigurationUtil;

/**
 * @author cvarela
 * @since 0.1
 */
public final class ThreadContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadContext.class);

	private static final ThreadLocal<ThreadContext> THREAD_LOCAL = new ThreadLocal<ThreadContext>();

	private CompositeConfiguration initialConfiguration;

	private ThreadContext() {
	}

	public synchronized static ThreadContext getInstance() {

		ThreadContext obj = THREAD_LOCAL.get();
		if (obj == null) {
			obj = new ThreadContext();
			THREAD_LOCAL.set(obj);
		}
		return obj;
	}

	private void setInitialConfiguration() {
		setInitialConfiguration(null);
	}

	public void setInitialConfiguration(final Configuration configuration) {

		if (initialConfiguration != null) {
			LOGGER.warn("InitialConfiguration ");
		}

		CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
		compositeConfiguration.setDelimiterParsingDisabled(true);
		compositeConfiguration.addConfiguration(new SystemConfiguration());
		if (configuration != null) {
			compositeConfiguration.addConfiguration(configuration);
		}
		initialConfiguration = compositeConfiguration;
	}

	public Configuration getConfiguration() {
		return getInternalConfiguration();
	}

	private CompositeConfiguration getInternalConfiguration() {

		if (initialConfiguration == null) {
			setInitialConfiguration();
		}
		return initialConfiguration;
	}

	public void setProperty(final String key, final Object value) {
		getConfiguration().setProperty(key, value);
	}

	public void clearProperty(final String key) {
		getConfiguration().clearProperty(key);
	}

	public String interpolate(final String value) {
		return ConfigurationUtil.interpolate(value, getInternalConfiguration());
	}
}
