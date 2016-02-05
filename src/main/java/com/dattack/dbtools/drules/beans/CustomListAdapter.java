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
package com.dattack.dbtools.drules.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Implements a {@link XmlAdapter} that use a comma or space to separate the elements in the list.
 *
 * @param <T>
 *            the datatype of the elements in the list.
 * @author cvarela
 * @since 0.1
 */
public abstract class CustomListAdapter<T> extends XmlAdapter<String, List<T>> {

    // whitespace or comma character
    private static final String SEPARATOR_REGEX = "\\s*(,|\\s)\\s*";

    @Override
    public final List<T> unmarshal(final String string) {

        final List<T> identifierList = new ArrayList<T>();

        for (final String s : string.split(SEPARATOR_REGEX)) {
            final String trimmed = s.trim();
            if (trimmed.length() > 0) {
                identifierList.add(create(trimmed));
            }
        }

        return identifierList;
    }

    @Override
    public final String marshal(final List<T> strings) {

        final StringBuilder sb = new StringBuilder();

        for (final T string : strings) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(string);
        }

        return sb.toString();
    }

    /**
     * @param text
     *            a single value represented as a text
     * @return the T-instance
     */
    protected abstract T create(final String text);
}