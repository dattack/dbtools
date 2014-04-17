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

import java.util.HashMap;
import java.util.Map;

/**
 * @author cvarela
 * @since 0.1
 */
public final class ResourceFactoryRegistry {

    private static final String DATASOURCE_TYPE_KEY = "javax.sql.DataSource";

    private static final Map<String, ResourceFactory<?>> MAPPING = new HashMap<String, ResourceFactory<?>>();

    static {
        // default factory list
        MAPPING.put(DATASOURCE_TYPE_KEY, new DataSourceFactory());
    }

    private ResourceFactoryRegistry() {
        // static class
    }

    /**
     * Returns the <code>ResourceFactory</code> associated to the resource type specified. If the resource type is
     * <code>null</code> or unknown then returns <code>null</code>.
     * 
     * @param type
     *            the resource type, may be <code>null</code>
     * @return the factory associated to the resource type, or <code>null</code> if no one exists
     */
    public static ResourceFactory<?> getFactory(final String type) {
        if (type == null) {
            return null;
        }
        return MAPPING.get(type);
    }
}
