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
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * @author cvarela
 * @since 0.1
 */
public class PingTaskBean implements Serializable {

    private static final long serialVersionUID = 3640559668991529501L;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "threads", required = false)
    private int threads;

    @XmlAttribute(name = "executions", required = false)
    private int executions;

    @XmlAttribute(name = "timeBetweenExecutions", required = true)
    private int timeBetweenExecutions;

    @XmlAttribute(name = "datasource", required = true)
    private String datasource;

    @XmlAttribute(name = "maxRowsToDump", required = false)
    private int maxRowsToDump;

    @XmlElements({ @XmlElement(name = "query", type = SqlStatementBean.class),
        @XmlElement(name = "script", type = SqlScriptBean.class) })
    private List<SqlCommandBean> sqlStatementList;

    @XmlElement(name = "log-file", type = String.class)
    private String logFile;

    @XmlElement(name = "command-provider", type = String.class)
    private String commandProvider;

    /**
     * @return the commandProvider
     */
    public String getCommandProvider() {
        return commandProvider;
    }

    /**
     * @return the datasource
     */
    public String getDatasource() {
        return datasource;
    }

    /**
     * @return the executions
     */
    public int getExecutions() {
        return executions;
    }

    /**
     * @return the logFile
     */
    public String getLogFile() {
        return logFile;
    }

    /**
     * @return the maxRowsToDump
     */
    public int getMaxRowsToDump() {
        return maxRowsToDump;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the sqlStatementList
     */
    public List<SqlCommandBean> getSqlStatementList() {
        return sqlStatementList;
    }

    /**
     * @return the threads
     */
    public int getThreads() {
        return threads;
    }

    /**
     * @return the timeBetweenExecutions
     */
    public int getTimeBetweenExecutions() {
        return timeBetweenExecutions;
    }
}
