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
package com.dattack.dbtools.drules.exceptions;

/**
 * Top-level exception that is thrown when the drules tool fails.
 *
 * @author cvarela
 * @since 0.1
 */
public class DrulesNestableException extends Exception {

    private static final long serialVersionUID = 8305544418991155317L;

    public DrulesNestableException(final String message) {
        super(message);
    }

    public DrulesNestableException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DrulesNestableException(final Throwable cause) {
        super(cause);
    }
}
