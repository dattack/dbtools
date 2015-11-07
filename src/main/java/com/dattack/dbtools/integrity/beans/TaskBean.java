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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Defines a data integrity task that can be executed independently of any other. The task is uniquely identified from
 * their task ID.
 * 
 * @author cvarela
 * @since 0.1
 */
public final class TaskBean implements Serializable {

    private static final long serialVersionUID = 7388023420557429959L;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_ID, required = true)
    private Identifier id;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_NAME, required = false)
    private String name;

    @XmlElement(name = XmlTokens.ELEMENT_SOURCE, required = true)
    private List<SourceBean> sourceList;

    @XmlElement(name = XmlTokens.ELEMENT_ROW_CHECK, required = true)
    private List<RowCheckBean> rowCheckList;

    @XmlElement(name = XmlTokens.ELEMENT_NOTIFICATION)
    private NotificationBean notification;

    public TaskBean() {
        this.sourceList = new ArrayList<SourceBean>();
        this.rowCheckList = new ArrayList<RowCheckBean>();
    }

    public Identifier getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<SourceBean> getSources() {
        return sourceList;
    }

    public List<RowCheckBean> getRowChecks() {
        return rowCheckList;
    }
    
    public NotificationBean getNotification() {
        return notification;
    }
}
