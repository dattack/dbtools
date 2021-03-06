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

import com.dattack.dbtools.drules.beans.Identifier;

/**
 * RuntimeException that is thrown when a identifier references can't be resolved.
 *
 * @author cvarela
 * @since 0.1
 */
public class IdentifierNotFoundException extends DrulesNestableException {

    private static final long serialVersionUID = 8870365523740741549L;

    public IdentifierNotFoundException(final Class<?> entityClazz, final Identifier identifier) {
        super(String.format("%s not found [Id: %s]", entityClazz.getSimpleName(),
                identifier == null ? null : identifier.getValue()));
    }
}
