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
package com.dattack.dbtools.integrity.beans;

import java.io.Serializable;

/**
 * @author cvarela
 * @since 0.1
 */
public interface SourceCommandBean extends Serializable {

    /**
     * Allow the visitor access to the element.
     *
     * @param visitor
     *            The Visitor to allow
     */
    void accept(final SourceCommandBeanVisitor visitor);
}
