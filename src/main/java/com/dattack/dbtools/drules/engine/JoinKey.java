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

import org.apache.commons.lang.builder.CompareToBuilder;

import com.dattack.dbtools.drules.beans.Identifier;

/**
 * @author cvarela
 * @since 0.1
 */
final class JoinKey implements Comparable<JoinKey> {

    private final List<Identifier> keyFieldList;
    private final List<Object> values;

    public JoinKey(final List<Identifier> keyFieldList) {
        this.keyFieldList = keyFieldList;
        this.values = new ArrayList<Object>(keyFieldList.size());
    }

    public void addField(final Identifier fieldName, final Object fieldValue) {

        final int index = keyFieldList.indexOf(fieldName);
        if (index >= 0) {
            if (index >= values.size()) {
                this.values.add(index, fieldValue);
            } else {
                this.values.set(index, fieldValue);
            }
        }
    }

    @Override
    public int compareTo(final JoinKey other) {

        if (values.size() != other.values.size()) {
            throw new IllegalArgumentException("Unable to compare two keys with different values");
        }

        final CompareToBuilder builder = new CompareToBuilder();
        for (int i = 0; i < values.size(); i++) {
            builder.append(values.get(i), other.values.get(i));
        }

        return builder.toComparison();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JoinKey other = (JoinKey) obj;
        if (values == null) {
            if (other.values != null) {
                return false;
            }
        } else if (!values.equals(other.values)) {
            return false;
        }
        return true;
    }

    public List<Object> getValues() {
        return values;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("JoinKey [values=").append(values).append("]");
        return builder.toString();
    }

}
