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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author cvarela
 * @since 0.1
 */
@XmlRootElement(name = "dbping")
public class DbpingBean implements Serializable {

    private static final long serialVersionUID = 8398044544048577943L;

    @XmlElement(name = "task", required = true, type = PingTaskBean.class)
    private final List<PingTaskBean> taskList;

    public DbpingBean() {
        this.taskList = new ArrayList<>();
    }

    /**
     * @return the taskList
     */
    public List<PingTaskBean> getTaskList() {
        return taskList;
    }
}
