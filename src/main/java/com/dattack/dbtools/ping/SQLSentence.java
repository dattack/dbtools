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
package com.dattack.dbtools.ping;

import java.io.Serializable;

/**
 * @author cvarela
 */
public class SQLSentence implements Serializable {

    private static final long serialVersionUID = 6027026851333293209L;

    private final String label;
    private final String sql;

    public SQLSentence(final String label, final String sql) {
        this.label = label;
        this.sql = sql;
    }

    public String getLabel() {
        return label;
    }

    public String getSql() {
        return sql;
    }
}