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
package com.dattack.naming.standalone;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.ext.io.FilesystemUtils;
import com.dattack.ext.misc.ConfigurationUtil;
import com.dattack.ext.util.CollectionUtils;
import com.dattack.naming.loader.NamingLoader;

/**
 * Initial Context Factory for {@link StandaloneContext}.
 * 
 * @author cvarela
 * @since 0.1
 */
public final class StandaloneContextFactory implements InitialContextFactory {

    private static volatile Context context;

    private static final String CLASSPATH_DIRECTORY_PROPERTY = StandaloneContextFactory.class.getName()
            + ".classpath.directory";

    private static final String RESOURCES_DIRECTORY_PROPERTY = StandaloneContextFactory.class.getName()
            + ".resources.directory";

    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneContextFactory.class);

    private CompositeConfiguration getConfiguration(final Hashtable<?, ?> environment) {

        BaseConfiguration baseConf = new BaseConfiguration();
        for (Entry<?, ?> entry : environment.entrySet()) {
            baseConf.setProperty(ObjectUtils.toString(entry.getKey()), entry.getValue());
        }

        CompositeConfiguration configuration = ConfigurationUtil.createEnvSystemConfiguration();
        configuration.addConfiguration(baseConf);
        return configuration;
    }

    @Override
    public Context getInitialContext(final Hashtable<?, ?> environment) throws NamingException {

        synchronized (StandaloneContextFactory.class) {
            if (context == null) {

                CompositeConfiguration configuration = getConfiguration(environment);

                final Object configDir = ConfigurationUtil.interpolate(
                        configuration.getProperty(RESOURCES_DIRECTORY_PROPERTY), configuration);

                if (configDir != null) {

                    List<String> extraClasspath = CollectionUtils.listAsString(configuration
                            .getList(CLASSPATH_DIRECTORY_PROPERTY));

                    File dir = FilesystemUtils.locate(ObjectUtils.toString(configDir));

                    if ((dir != null) && dir.exists()) {
                        LOGGER.info("Scanning directory '{}' for JNDI resources.", dir);
                        try {
                            StandaloneContext ctx = new StandaloneContext(environment);
                            final NamingLoader loader = new NamingLoader();
                            loader.loadDirectory(dir, ctx, extraClasspath);
                            context = ctx;
                        } catch (IOException e) {
                            throw new NamingException(e.getMessage());
                        }
                    } else {
                        throw new ConfigurationException(MessageFormat.format(
                                "JNDI configuration error: directory ''{0}'' not exists", dir));
                    }
                } else {
                    throw new ConfigurationException(MessageFormat.format(
                            "JNDI configuration error: missing property ''{0}''", RESOURCES_DIRECTORY_PROPERTY));
                }
            }
        }
        return context;
    }
}
