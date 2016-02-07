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
package com.dattack.dbtools.drules.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import org.apache.commons.lang.StringUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public class ForEachBean implements SourceCommandBean {

    private static final long serialVersionUID = 5817647970884889561L;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_KEY)
    private String key;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_VALUES)
    private String values;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_REF)
    private String ref;

    @XmlElements({ @XmlElement(name = XmlTokens.ELEMENT_SQL, type = SqlQueryBean.class),
            @XmlElement(name = XmlTokens.ELEMENT_FOREACH, type = ForEachBean.class) })
    private final List<SourceCommandBean> commandList;

    public ForEachBean() {
        this.commandList = new ArrayList<SourceCommandBean>();
    }

    public List<SourceCommandBean> getCommandList() {
        return commandList;
    }

    public String getKey() {
        return key;
    }

    public List<String> getValuesList() {
        return Arrays.asList(StringUtils.trimToEmpty(values).split(","));
    }

    public String getRef() {
        return ref;
    }

    @Override
    public void accept(final SourceCommandBeanVisitor visitor) {
        visitor.visit(this);
    }
}
