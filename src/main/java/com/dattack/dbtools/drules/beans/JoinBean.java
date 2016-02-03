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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author cvarela
 * @since 0.1
 */
public final class JoinBean implements Serializable {

    private static final long serialVersionUID = -6798673196715995781L;

    @XmlType(name = "joinType")
    @XmlEnum
    public enum JoinType {
        INNER;
    }

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_TYPE)
    private JoinType type;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_USING, required = true)
    @XmlJavaTypeAdapter(IdentifierListAdapter.class)
    private List<Identifier> using;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_SOURCE, required = true)
    @XmlJavaTypeAdapter(IdentifierListAdapter.class)
    private List<Identifier> source;

    @XmlElements({ @XmlElement(name = XmlTokens.ELEMENT_ON_MATCH, type = JoinResultMatchBean.class),
            @XmlElement(name = XmlTokens.ELEMENT_ON_MISSING, type = JoinResultMissingBean.class) })
    private List<JoinResultBean> eventList;

    public JoinBean() {
        this.eventList = new ArrayList<JoinResultBean>();
        this.type = JoinType.INNER;
    }

    public JoinType getType() {
        return type;
    }

    public List<Identifier> getUsing() {
        return using;
    }

    public List<Identifier> getSources() {
        return source;
    }
    
    public List<JoinResultBean> getEventList() {
        return eventList;
    }
}
