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
package com.dattack.ext.misc;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertyConverter;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.lang.ObjectUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public class ConfigurationUtil {

	private ConfigurationUtil() {
		// static class
	}
	
	/**
	 * Create a Configuration based on the environment variables and system properties.
	 * @return
	 */
	public static CompositeConfiguration createEnvSystemConfiguration() {
		CompositeConfiguration configuration = new CompositeConfiguration();
		configuration.addConfiguration(new SystemConfiguration());
		configuration.addConfiguration(new EnvironmentConfiguration());
		return configuration;
	}

	public static String interpolate(final Object value, final AbstractConfiguration configuration) {

		return interpolate(ObjectUtils.toString(value), configuration);
	}

	public static String interpolate(final String value, final AbstractConfiguration configuration) {

		if ((value == null) || (configuration == null)) {
			return value;
		}

		return PropertyConverter.interpolate(value, configuration).toString();
	}
}
