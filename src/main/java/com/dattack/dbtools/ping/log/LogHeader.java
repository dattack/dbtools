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
package com.dattack.dbtools.ping.log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.dattack.dbtools.ping.beans.PingTaskBean;

/**
 * @author cvarela
 * @since 0.1
 */
public class LogHeader implements Serializable {

    private static final long serialVersionUID = -1459958170880853467L;

    private final PingTaskBean pingTaskBean;
    private final Map<String, String> properties;

    public LogHeader(final PingTaskBean pingJobConfiguration) {
        this.pingTaskBean = pingJobConfiguration;
        properties = new HashMap<>();
        populateProperties();
    }

    public PingTaskBean getPingTaskBean() {
        return pingTaskBean;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    private void populateProperties() {
        properties.put("java version", System.getProperty("java.version"));
        properties.put("OS",
                System.getProperty("os.name") + " " //
                        + System.getProperty("os.arch") + " " //
                        + System.getProperty("os.version"));
        properties.put("username", System.getProperty("user.name"));
    }
}
