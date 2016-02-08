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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author cvarela
 * @since 0.1
 */
public abstract class AbstractEventActionThrowableBean implements EventActionBean {

    private static final int MAX_EVENTS_DEFAULT = 1;

    private static final long serialVersionUID = -6297545544240009404L;

    private int currentEvents;
    private int maxEvents;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_TEMPLATE)
    private String templateFile;

    @XmlValue
    private String templateText;

    AbstractEventActionThrowableBean() {
        this.maxEvents = MAX_EVENTS_DEFAULT;
        this.currentEvents = 0;
    }

    public int getCurrentEvents() {
        return currentEvents;
    }

    int getMaxEvents() {
        return maxEvents;
    }

    public String getTemplateFile() {
        return templateFile;
    }

    public String getTemplateText() {
        return templateText;
    }

    /**
     * Increments the number of events handled by this action.
     */
    public void incrEvents() {
        this.currentEvents++;
    }

    public boolean isMaxEventsReached() {
        return currentEvents >= maxEvents;
    }

    /**
     * Sets the maximum number of elements to handle.
     *
     * @param maxEvents
     *            the maximum number of events to handle
     */
    @XmlAttribute(name = XmlTokens.ATTRIBUTE_MAX)
    public void setMaxEvents(final int maxEvents) {
        if (maxEvents > 0) {
            this.maxEvents = maxEvents;
        }
    }
}
