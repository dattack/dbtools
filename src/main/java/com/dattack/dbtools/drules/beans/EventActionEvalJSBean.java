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

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author cvarela
 * @since 0.1
 */
public class EventActionEvalJSBean implements EventActionBean {

    private static final long serialVersionUID = -7594172165036104811L;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_NAME)
    private String name;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_EXPR)
    private String expression;

    public String getName() {
        return name;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public void accept(final EventActionBeanVisitor visitor) {
        visitor.visite(this);
    }
}
