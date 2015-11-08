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
package com.dattack.dbtools.integrity.beans;

/**
 * Visitor pattern for {@link EventActionBean} hierarchy.
 *
 * @author cvarela
 * @since 0.1
 */
public interface EventActionBeanVisitor {

    /**
     * @param item
     *            the element to visite
     */
    void visite(final EventActionEvalJSBean item);

    /**
     * @param item
     *            the element to visite
     */
    void visite(final EventActionExecuteSqlBean item);

    /**
     * @param item
     *            the element to visite
     */
    void visite(final EventActionLogBean item);

    /**
     * @param item
     *            the element to visite
     */
    void visite(final EventActionThrowErrorBean item);

    /**
     * @param item
     *            the element to visite
     */
    void visite(final EventActionThrowWarningBean item);
}
