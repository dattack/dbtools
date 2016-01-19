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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.dattack.dbtools.integrity.beans.Identifier;

/**
 * @author cvarela
 * @since 0.1
 */
final class RowDataMap {

    // a relation map sourceId-->RowData
    private Map<Identifier, RowData> sourceRowDataMap;

    public RowDataMap() {
        this.sourceRowDataMap = new HashMap<Identifier, RowData>();
    }
    
    public int getSize() {
        return sourceRowDataMap.size();
    }
    
    public Collection<RowData> getRows() {
        return sourceRowDataMap.values();
    }
    
    public void putData(final Identifier sourceId, RowData joinKey) {
        this.sourceRowDataMap.put(sourceId, joinKey);
    }

    /**
     * Computes the min key of all stored rowData.
     * 
     * @return the min key or <tt>null</tt> if no one exists
     */
    public JoinKey getMinKey() {

        JoinKey minKey = null;
        for (RowData rowData : sourceRowDataMap.values()) {
            
            if (rowData == null || rowData.getKey() == null) {
                continue;
            }
            
            if (minKey == null) {
                minKey = rowData.getKey();
            } else {
                if (rowData.getKey().compareTo(minKey) < 0) {
                    minKey = rowData.getKey();
                }
            }
        }
        return minKey;
    }
}