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

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cvarela
 * @since 0.1
 * @deprecated
 */
public final class ExecutionContext {

	private static final Logger log = LoggerFactory.getLogger(ExecutionContext.class);

    static final String CONFIGURATION_FILE_PROPERTY = "dattack.configurationFile";

	private static final ThreadLocal<ExecutionContext> instance = new ThreadLocal<ExecutionContext>();

	private final CompositeConfiguration configuration;

	private ExecutionContext() {
		configuration = new CompositeConfiguration(new BaseConfiguration());
		configuration.setDelimiterParsingDisabled(true);
		configuration.addConfiguration(new SystemConfiguration());

		if (configuration.containsKey(CONFIGURATION_FILE_PROPERTY)) {
			try {
				configuration.addConfiguration(
						new PropertiesConfiguration(configuration.getString(CONFIGURATION_FILE_PROPERTY)));
			} catch (ConfigurationException e) {
				log.warn(e.getMessage(), e);
			}
		}
	}

	public synchronized static ExecutionContext getInstance() {
		ExecutionContext obj = instance.get();
		if (obj == null) {
			obj = new ExecutionContext();
			instance.set(obj);
		}
		return obj;
	}

	public AbstractConfiguration getConfiguration() {
		return configuration;
	}
}
