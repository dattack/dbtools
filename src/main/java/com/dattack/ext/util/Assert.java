/*
 * Copyright (c) 2016, The Dattack team (http://www.dattack.com)
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

import org.apache.commons.lang.StringUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public final class Assert {

    private Assert() {
        // static class
    }

    /**
     * Assert that an object is not null.
     * 
     * @param object
     *            the object to check
     * @param message
     *            the exception message to use if the assertion fails
     */
    public static void isNotNull(final Object object, final String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object is not null.
     * 
     * @param object
     *            the object to check
     */
    public static void isNotNull(final Object object) {
        isNotNull(object, "This argument must not be null");
    }

    /**
     * Assert that a String is not a whitespace, empty or null.
     * 
     * @param text
     *            the object to check
     * @param message
     *            the exception message to use if the assertion fails
     */
    public static void isNotBlank(final String text, final String message) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a String is not a whitespace, empty or null.
     * 
     * @param text
     *            the object to check
     */
    public static void isNotBlank(final String text) {
        isNotBlank(text, "This argument must not be null, empty, or blank");
    }
}
