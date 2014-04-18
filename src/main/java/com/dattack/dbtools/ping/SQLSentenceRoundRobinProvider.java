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

import java.util.List;

/**
 * Implements the round-robin strategy for SQL-sentence selection.
 * 
 * @author cvarela
 * @since 0.1
 */
public class SQLSentenceRoundRobinProvider implements SQLSentenceProvider {

    private final List<SQLSentence> sqlList;
    private int index;

    public SQLSentenceRoundRobinProvider(final List<SQLSentence> sqlList) {
        this.sqlList = sqlList;
        index = 0;
    }

    @Override
    public synchronized SQLSentence nextSQL() {

        SQLSentence sqlSentence = sqlList.get(index++);
        if (index >= sqlList.size()) {
            index = 0;
        }
        return sqlSentence;
    }
}
