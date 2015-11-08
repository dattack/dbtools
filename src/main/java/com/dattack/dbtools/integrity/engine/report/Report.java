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
package com.dattack.dbtools.integrity.engine.report;

import java.util.List;

import org.apache.commons.configuration.CompositeConfiguration;

import com.dattack.dbtools.integrity.beans.EventActionLogBean;
import com.dattack.dbtools.integrity.beans.EventActionThrowErrorBean;
import com.dattack.dbtools.integrity.beans.EventActionThrowWarningBean;
import com.dattack.dbtools.integrity.engine.ExecutionContext;
import com.dattack.dbtools.integrity.engine.PropertyNames;
import com.dattack.dbtools.integrity.engine.RowData;
import com.dattack.ext.misc.ConfigurationUtil;

/**
 * @author cvarela
 *
 */
public abstract class Report {

    static final String STATUS_WARNING = "WARNING";
    static final String STATUS_ERROR = "ERROR";
    static final String STATUS_OK = "OK";

    private final StringBuilder buffer;

    public Report() {
        this.buffer = new StringBuilder();
    }

    void append(final String text) {
        buffer.append(text);
    }

    public abstract void handleLog(final EventActionLogBean action, final List<RowData> rowDataList);

    public abstract void handleError(final EventActionThrowErrorBean action, final List<RowData> rowDataList);

    public abstract void handleWarning(final EventActionThrowWarningBean action, final List<RowData> rowDataList);

    String interpolate(final String message, final String status, final String log) {
        CompositeConfiguration configuration = new CompositeConfiguration(
                ExecutionContext.getInstance().getConfiguration());
        configuration.setProperty(PropertyNames.LOG, log);
        configuration.setProperty(PropertyNames.STATUS, status);
        return ConfigurationUtil.interpolate(message, configuration);
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
