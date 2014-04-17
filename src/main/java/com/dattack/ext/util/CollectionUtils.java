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
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

/**
 * Provides utility methods for <code>Collection</code> instances.
 * 
 * @author cvarela
 * @since 0.1
 */
public final class CollectionUtils {

    private CollectionUtils() {
        // static class
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

        List<String> result = new ArrayList<String>();
        if (list != null) {
            for (final Object obj : list) {
                result.add(ObjectUtils.toString(obj));
            }
        }
        return result;
    }
}
