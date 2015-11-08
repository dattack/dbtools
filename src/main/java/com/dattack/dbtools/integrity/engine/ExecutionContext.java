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
package com.dattack.dbtools.integrity.engine;

import org.apache.commons.configuration.BaseConfiguration;

/**
 * @author cvarela
 * @since 0.1
 */
public class ExecutionContext {

    private static final ThreadLocal<ExecutionContext> instance = new ThreadLocal<ExecutionContext>();

    private final BaseConfiguration configuration;

    private ExecutionContext() {
        configuration = new BaseConfiguration();
        configuration.setDelimiterParsingDisabled(true);
    }

    public synchronized static ExecutionContext getInstance() {
        ExecutionContext obj = instance.get();
        if (obj == null) {
            obj = new ExecutionContext();
            instance.set(obj);
        }
        return obj;
    }

    public BaseConfiguration getConfiguration() {
        return configuration;
    }
}
