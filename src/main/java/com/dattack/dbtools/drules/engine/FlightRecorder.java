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
package com.dattack.dbtools.drules.engine;

import java.util.List;

import com.dattack.dbtools.drules.beans.ConfigurationBean;
import com.dattack.dbtools.drules.beans.EventActionThrowErrorBean;
import com.dattack.dbtools.drules.beans.EventActionThrowWarningBean;
import com.dattack.dbtools.drules.beans.TaskBean;
import com.dattack.dbtools.drules.engine.report.Report;

/**
 * @author cvarela
 * @since 0.1
 */
public final class FlightRecorder {

    private final TaskBean taskBean;
    private final ConfigurationBean configurationBean;
    private int errorCounter;
    private int warningCounter;
    private final Report report;

    public FlightRecorder(final TaskBean taskBean, final ConfigurationBean configurationBean) {
        this.taskBean = taskBean;
        this.configurationBean = configurationBean;
        this.errorCounter = 0;
        this.warningCounter = 0;
        this.report = new Report();
    }

    public TaskBean getTaskBean() {
        return taskBean;
    }

    public ConfigurationBean getConfigurationBean() {
        return configurationBean;
    }

    public boolean hasErrors() {
        return errorCounter > 0;
    }

    public boolean hasWarnings() {
        return warningCounter > 0;
    }

    /**
     * Returns the current report.
     * 
     * @return the report
     */
    public Report getReport() {
        return report;
    }

    public void handleLog(final List<RowData> rowDataList) {

        report.handleLog(rowDataList);
    }

    public void handleError(final EventActionThrowErrorBean action, final List<RowData> rowDataList) {
        errorCounter++;
        report.handleError(action, rowDataList);
    }

    public void handleWarning(final EventActionThrowWarningBean action, final List<RowData> rowDataList) {
        warningCounter++;
        report.handleWarning(action, rowDataList);
    }
}
