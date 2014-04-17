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
package com.dattack.naming.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dattack.naming.loader.factory.ResourceFactory;
import com.dattack.naming.loader.factory.ResourceFactoryRegistry;

/**
 * @author cvarela
 * @since 0.1
 */
public final class NamingLoader {

    private static final String TYPE_KEY = "type";

    private static final Log log = LogFactory.getLog(NamingLoader.class);

    private static Object getObject(final Properties properties, final List<String> extraClasspath) {

        final String type = properties.getProperty(TYPE_KEY);

        ResourceFactory<?> factory = ResourceFactoryRegistry.getFactory(type);
        if (factory != null) {
            return factory.getObjectInstance(properties, extraClasspath);
        }

        log.warn(MessageFormat.format("Unable to get a factory for type ''{0}''", type));
        return null;
    }

    private void put(final Context context, final String key, final Object value) throws NamingException {

        Object obj = context.lookup(key);

        if (obj instanceof Context) {
            context.destroySubcontext(key);
            obj = null;
        }

        if (obj == null) {
            context.bind(key, value);
        } else {
            context.rebind(key, value);
        }

        log.info("Context.bind: " + key + " -> " + value);
    }

    private void load(final Properties properties, final Context ctxt, final Context parentCtxt, final String ctxtName,
            final List<String> extraClasspath) throws NamingException {

        Object value = getObject(properties, extraClasspath);
        if (value != null) {
            put(parentCtxt, ctxtName, value);
        }
    }

    /**
     * Scans a directory hierarchy looking for <code>*.properties</code> files. Creates a subcontext for each directory
     * in the hierarchy and binds a new resource for each <code>*.properties</code> file with a
     * <code>ResourceFactory</code> associated.
     * 
     * 
     * @param directory
     *            the directory to scan
     * @param ctxt
     *            the Context to populate
     * @param extraClasspath
     *            additional paths to include to the classpath
     * @throws NamingException
     *             if a naming exception is encountered
     * @throws IOException
     *             if an I/O error ocurred
     */
    public void loadDirectory(final File directory, final Context ctxt, final List<String> extraClasspath)
            throws NamingException, IOException {
        loadDirectory(directory, ctxt, null, extraClasspath);
    }

    private void loadDirectory(final File directory, final Context ctxt, final Context parentCtxt, //
            final List<String> extraClasspath) throws NamingException, IOException {

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(MessageFormat.format("''{0}'' isn't a directory", directory));
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String name = file.getName();

            if (file.isDirectory()) {
                Context tmpCtxt = ctxt.createSubcontext(name);
                loadDirectory(file, tmpCtxt, ctxt, extraClasspath);
            } else {
                String[] extensions = new String[] { ".properties" };
                for (int j = 0; j < extensions.length; j++) {
                    String extension = extensions[j];
                    if (name.endsWith(extension)) {
                        name = name.substring(0, name.length() - extension.length());
                        Context subcontext = ctxt.createSubcontext(name);
                        load(loadFile(file), subcontext, ctxt, name, extraClasspath);
                    }
                }
            }
        }
    }

    private Properties loadFile(final File file) throws IOException {

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fin);
            return properties;
        } finally {
            if (fin != null) {
                fin.close();
            }
        }
    }
}
