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

    private List<SQLSentence> sentenceList;
    private int index;

    public SQLSentenceRoundRobinProvider() {
        index = 0;
    }

    @Override
    public synchronized SQLSentence nextSql() {

        if (sentenceList == null || sentenceList.isEmpty()) {
            throw new IllegalArgumentException("The sentence list must not be null or empty");
        }

        final SQLSentence sqlSentence = sentenceList.get(index++);
        if (index >= sentenceList.size()) {
            index = 0;
        }
        return sqlSentence;
    }

    @Override
    public void setSentences(final List<SQLSentence> sqlList) {
        this.sentenceList = sqlList;
    }
}
