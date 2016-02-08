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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

/**
 * Defines the bean mapped with the {@link XmlTokens#ELEMENT_CHECK} element. It contains the expression to evaluate and
 * the actions to execute when the expression is true (<i>onSuccess</i>) or false (<i>onFail</i>).
 *
 * @author cvarela
 * @since 0.1
 */
public final class CheckExprBean implements Serializable {

    private static final long serialVersionUID = 471091198442256506L;

    @XmlElement(name = XmlTokens.ATTRIBUTE_EXPR)
    private String expression;

    @XmlElement(name = XmlTokens.ELEMENT_ON_SUCCESS, type = CheckExprResultBean.class)
    private CheckExprResultBean onSucess;

    @XmlElement(name = XmlTokens.ELEMENT_ON_FAIL, type = CheckExprResultBean.class)
    private CheckExprResultBean onFail;

    public String getExpression() {
        return expression;
    }

    public CheckExprResultBean getOnSucess() {
        return onSucess;
    }

    public CheckExprResultBean getOnFail() {
        return onFail;
    }
}
