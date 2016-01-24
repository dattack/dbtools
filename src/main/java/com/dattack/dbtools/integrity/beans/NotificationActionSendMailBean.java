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

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author cvarela
 * @since 0.1
 */
public final class NotificationActionSendMailBean implements NotificationActionBean {

    private static final long serialVersionUID = 8194754349971717624L;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_TO)
    @XmlJavaTypeAdapter(StringListAdapter.class)
    private List<String> toAddressesList;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_SUBJECT)
    private String subject;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_TEMPLATE)
    private String messageTemplateFile;

    @XmlValue
    private String messageTemplateText;

    public List<String> getToAddressesList() {
        return toAddressesList;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessageTemplateFile() {
        return messageTemplateFile;
    }

    public String getMessageTemplateText() {
        return messageTemplateText;
    }

    @Override
    public void accept(final NotificationActionBeanVisitor visitor) {
        visitor.visite(this);
    }
}
