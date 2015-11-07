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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author cvarela
 * @since 0.1
 */
@XmlRootElement(name = XmlTokens.ELEMENT_INTEGRITY)
public final class IntegrityBean implements Serializable {

    private static final long serialVersionUID = -4459426103473113755L;

    @XmlElement(name = XmlTokens.ELEMENT_TASK, required = true, type = TaskBean.class)
    private final List<TaskBean> taskBeanList;

    @XmlElement(name = XmlTokens.ELEMENT_CONFIGURATION, required = false, type = ConfigurationBean.class)
    private ConfigurationBean configuration;

    public IntegrityBean() {
        this.taskBeanList = new ArrayList<TaskBean>();
    }

    public TaskBean getTask(final Identifier taskId) {

        for (final TaskBean taskBean : taskBeanList) {
            if (taskBean.getId().equals(taskId)) {
                return taskBean;
            }
        }
        return null;
    }

    public ConfigurationBean getConfiguration() {
        return configuration;
    }
}