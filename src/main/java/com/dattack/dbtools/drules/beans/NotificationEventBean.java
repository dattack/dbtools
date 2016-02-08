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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * @author cvarela
 * @since 0.1
 */
public final class NotificationEventBean implements Serializable {

    private static final long serialVersionUID = -2268484098918678124L;

    @XmlElements({ @XmlElement(name = XmlTokens.ELEMENT_SEND_MAIL, type = NotificationActionSendMailBean.class) })
    private final List<NotificationActionBean> actionList;

    public NotificationEventBean() {
        this.actionList = new ArrayList<NotificationActionBean>();
    }

    public List<NotificationActionBean> getActionList() {
        return actionList;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("NotificationEventBean [actionList=").append(actionList).append("]");
        return builder.toString();
    }
}
