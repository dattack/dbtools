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
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertyConverter;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dattack.naming.loader.NamingLoader;

/**
 * Initial Context Factory for <tt>StandaloneContext</tt>.
 * 
 * @author cvarela
 * @since 0.1
 */
public class StandaloneContextFactory implements InitialContextFactory {

    private static volatile Context context;

    private static final String DIRECTORY_PROPERTY = StandaloneContextFactory.class.getName() + ".directory";

    private Log log = LogFactory.getLog(StandaloneContextFactory.class);

    public StandaloneContextFactory() {
        super();
    }

    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {

        synchronized (StandaloneContextFactory.class) {
            if (context == null) {

                CompositeConfiguration configuration = new CompositeConfiguration();
                configuration.addConfiguration(new SystemConfiguration());
                configuration.addConfiguration(new EnvironmentConfiguration());
                BaseConfiguration baseConf = new BaseConfiguration();
                for (Entry<?, ?> entry : environment.entrySet()) {
                    baseConf.setProperty(ObjectUtils.toString(entry.getKey()), entry.getValue());
                }
                configuration.addConfiguration(baseConf);

                final Object configDir = PropertyConverter.interpolate(configuration.getProperty(DIRECTORY_PROPERTY),
                        configuration);
                if (configDir != null) {

                    final URL url = getClass().getClassLoader().getResource(configDir.toString());

                    File dir = null;
                    if (url != null) {
                        try {
                            final URI uri = new URI(url.toExternalForm());
                            dir = new File(uri);
                        } catch (final Exception e) {
                            log.error(e.getMessage());
                        }
                    } else {
                        dir = new File(configDir.toString());
                    }

                    if ((dir != null) && dir.exists()) {
                        log.info("INFO - Scanning directory '" + dir + "' for JNDI resources.");
                        final NamingLoader loader = new NamingLoader();

                        StandaloneContext ctx = new StandaloneContext(environment);
                        try {
                            loader.loadDirectory(dir, ctx);
                        } catch (IOException e) {
                            throw new NamingException(e.getMessage());
                        }
                        context = ctx;
                    } else {
                        log.error(MessageFormat.format("JNDI configuration error: directory ''{0}'' not exists", dir));
                        throw new ConfigurationException(MessageFormat.format(
                                "JNDI configuration error: directory ''{0}'' not exists", dir));
                    }
                } else {
                    log.error(MessageFormat.format("JNDI configuration error: missing property ''{0}''",
                            DIRECTORY_PROPERTY));
                    throw new ConfigurationException(MessageFormat.format(
                            "JNDI configuration error: missing property ''{0}''", DIRECTORY_PROPERTY));
                }
            }

            return context;
        }
    }
}
