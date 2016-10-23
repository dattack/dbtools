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
package com.dattack.dbtools.ping.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author cvarela
 * @since 0.1
 */
public class SqlScriptBean implements SqlCommandBean {

    private static final long serialVersionUID = 5671689427608954154L;

    @XmlAttribute(name = "label", required = true)
    private String label;

    @XmlAttribute(name = "weight", required = false)
    private float weight;

    @XmlElement(name = "query", required = true, type = SqlStatementBean.class)
    private List<SqlStatementBean> statementList;

    @Override
    public void accept(final SqlCommandVisitor visitor) {
        visitor.visite(this);
    }

    /**
     * @return the label
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * @return the statementList
     */
    public List<SqlStatementBean> getStatementList() {
        return statementList;
    }

    /**
     * @return the weight
     */
    @Override
    public float getWeight() {
        return weight;
    }
}
