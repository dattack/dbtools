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

import java.util.List;
import java.util.Properties;

/**
 * @author cvarela
 * @since 0.1
 * @param <T>
 *            the type of objects instantiated by this factory
 */
public interface ResourceFactory<T> {

    /**
     * Creates a new instance using the specified properties.
     * 
     * @param properties
     *            a Properties data structure
     * @param extraClasspath
     *            additional paths to explore
     * @return an instance of T
     */
    T getObjectInstance(Properties properties, final List<String> extraClasspath);
}
