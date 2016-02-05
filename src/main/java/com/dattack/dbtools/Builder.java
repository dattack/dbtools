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
package com.dattack.dbtools;

/**
 * Defines the <code>Builder</code> pattern.
 *
 * @author cvarela
 * @since 0.1
 * @param <T>
 *            the generic type of the objects to build
 */
public interface Builder<T> {

    /**
     * Creates a new instance of the generic type.
     *
     * @return a new instance of the generic type
     */
    T build();
}
