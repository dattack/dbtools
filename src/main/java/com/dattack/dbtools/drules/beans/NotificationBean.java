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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author cvarela
 * @since 0.1
 */
@XmlRootElement(name = XmlTokens.ELEMENT_NOTIFICATION)
public final class NotificationBean implements Serializable {

    private static final long serialVersionUID = 5261856228713722543L;

    @XmlElement(name = XmlTokens.ELEMENT_ON_WARNING)
    private NotificationEventBean onWarning;

    @XmlElement(name = XmlTokens.ELEMENT_ON_ERROR)
    private NotificationEventBean onError;

    @XmlElement(name = XmlTokens.ELEMENT_ON_SUCCESS)
    private NotificationEventBean onSuccess;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NotificationBean [onWarning=").append(onWarning).append(", onError=").append(onError)
                .append("]");
        return builder.toString();
    }

    public NotificationEventBean getOnError() {
        return onError;
    }

    public NotificationEventBean getOnWarning() {
        return onWarning;
    }

    public NotificationEventBean getOnSuccess() {
        return onSuccess;
    }
}
