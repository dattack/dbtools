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
package com.dattack.dbtools.integrity.engine;

import java.util.ArrayList;
import java.util.List;

import com.dattack.dbtools.integrity.beans.ConfigurationBean;
import com.dattack.dbtools.integrity.beans.TaskBean;

/**
 * @author cvarela
 * @since 0.1
 */
public final class FlightRecorder {

    private final TaskBean taskBean;
    private final ConfigurationBean configurationBean;
    private final StringBuilder log;
    private final List<String> errorList;
    private final List<String> warningList;

    public FlightRecorder(final TaskBean taskBean, final ConfigurationBean configurationBean) {
        this.taskBean = taskBean;
        this.configurationBean = configurationBean;
        this.log = new StringBuilder();
        this.errorList = new ArrayList<String>();
        this.warningList = new ArrayList<String>();
    }

    public TaskBean getTaskBean() {
        return taskBean;
    }
    
    public ConfigurationBean getConfigurationBean() {
        return configurationBean;
    }

    public void appendLog(final String message) {
        this.log.append(message).append("\n");
    }

    public boolean hasErrors() {
        return !errorList.isEmpty();
    }

    public boolean hasWarnings() {
        return !warningList.isEmpty();
    }

    public void handleError(final String message) {
        this.errorList.add(message);
        appendLog(message);
    }

    public void handleWarning(final String message) {
        this.warningList.add(message);
        appendLog(message);
    }

    public String getLog() {
        return log.toString();
    }

    public List<String> getErrorList() {
        return errorList;
    }

    public List<String> getWarningList() {
        return warningList;
    }
}
