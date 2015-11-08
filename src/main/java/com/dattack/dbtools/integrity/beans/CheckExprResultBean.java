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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * Defines the bean mapped with a {@link XmlTokens#ELEMENT_ON_SUCCESS} or {@link XmlTokens#ELEMENT_ON_FAIL element. This
 * is a list with all the {@link EventActionBean} to be executed.
 *
 * @author cvarela
 * @since 0.1
 */
public final class CheckExprResultBean implements Serializable {

    private static final long serialVersionUID = -859826184950423875L;

    @XmlElements({ @XmlElement(name = XmlTokens.ELEMENT_EVAL, type = EventActionEvalJSBean.class),
            @XmlElement(name = XmlTokens.ELEMENT_EXECUTE_SQL, type = EventActionExecuteSqlBean.class),
        @XmlElement(name = XmlTokens.ELEMENT_LOG, type = EventActionLogBean.class),
        @XmlElement(name = XmlTokens.ELEMENT_THROW_ERROR, type = EventActionThrowErrorBean.class),
        @XmlElement(name = XmlTokens.ELEMENT_THROW_WARNING, type = EventActionThrowWarningBean.class) })
    private final List<EventActionBean> actionList;

    public CheckExprResultBean() {
        this.actionList = new ArrayList<EventActionBean>();
    }

    public List<EventActionBean> getActionList() {
        return actionList;
    }
}
