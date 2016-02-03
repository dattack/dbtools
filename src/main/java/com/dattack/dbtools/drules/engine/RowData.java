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
package com.dattack.dbtools.drules.engine;

import java.util.ArrayList;
import java.util.List;

import com.dattack.dbtools.drules.beans.Identifier;

/**
 * @author cvarela
 * @since 0.1
 */
public final class RowData {

    private final Identifier sourceId;
    private final List<Identifier> keyFieldList;

    private JoinKey joinKey;
    private final List<IdentifierValuePair> fieldValueList;

    public RowData(final Identifier sourceId, final List<Identifier> keyFieldList) {
        this.sourceId = sourceId;
        this.keyFieldList = keyFieldList;
        this.fieldValueList = new ArrayList<IdentifierValuePair>();
        this.joinKey = null;
    }

    public Identifier getSourceId() {
        return sourceId;
    }

    public JoinKey getKey() {
        return joinKey;
    }

    public List<IdentifierValuePair> getFieldValueList() {
        return fieldValueList;
    }

    public void addField(final Identifier name, final Object value) {
        this.fieldValueList.add(new IdentifierValuePair(name, value));
        if (joinKey == null) {
            joinKey = new JoinKey(keyFieldList);
        }
        joinKey.addField(name, value);
    }
}
