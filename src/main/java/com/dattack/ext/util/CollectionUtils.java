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
package com.dattack.ext.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

/**
 * Provides utility methods for <code>Collection</code> instances.
 *
 * @author cvarela
 * @since 0.1
 */
public final class CollectionUtils {

    /**
     * Null-safe check if the specified collection is empty.
     *
     * @param collection
     *            the collection to check
     * @return true if empty or null
     */
    public static boolean isEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Null-safe check if the specified collection is not empty.
     *
     * @param collection
     *            the collection to check
     * @return true if non-null and non-empty
     */
    public static boolean isNotEmpty(final Collection<?> collection) {
        return !(isEmpty(collection));
    }

    /**
     * Adapts a Object-value <code>List</code> to a String-value one. If the list to adapt is <code>null</code>, then an
     * empty list is returned.
     *
     * @param list
     *            the List to adapt, may be null
     * @return the adapted List
     */
    public static List<String> listAsString(final List<Object> list) {

        final List<String> result = new ArrayList<String>();
        if (list != null) {
            for (final Object obj : list) {
                result.add(ObjectUtils.toString(obj));
            }
        }
        return result;
    }

    private CollectionUtils() {
        // static class
    }
}
