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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import com.dattack.dbtools.drules.beans.Identifier;
import com.dattack.dbtools.drules.beans.Identifier.IdentifierBuilder;

/**
 * @author cvarela
 *
 */
final class RowDataFactory {

    private final Identifier sourceId;
    private final List<Identifier> keyFieldList;

    public RowDataFactory(final Identifier sourceId, final List<Identifier> keyFieldList) {
        this.sourceId = sourceId;
        this.keyFieldList = keyFieldList;
    }

    public RowData create(final SourceResult sourceResult) throws SQLException {

        RowData rowData = new RowData(sourceId, keyFieldList);

        if (sourceResult.getResultSet() != null && sourceResult.getResultSet().next()) {

            ResultSetMetaData metaData = sourceResult.getResultSet().getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                Identifier name = new IdentifierBuilder().withValue(metaData.getColumnLabel(i)).build();
                Object value = sourceResult.getResultSet().getObject(i);
                rowData.addField(name, value);
            }
        }

        return rowData;
    }
}
