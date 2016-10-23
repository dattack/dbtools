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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author cvarela
 * @since 0.1
 */
public class SqlStatementBean implements SqlCommandBean {

    private static final long serialVersionUID = -5343761660462688691L;

    @XmlValue
    private String sql;

    @XmlAttribute(name = "label", required = true)
    private String label;

    @XmlAttribute(name = "weight", required = false)
    private float weight;

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
     * @return the sql
     */
    public String getSql() {
        return sql;
    }

    /**
     * @return the weight
     */
    @Override
    public float getWeight() {
        return weight;
    }
}
