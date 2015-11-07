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

import java.util.List;

import org.apache.commons.configuration.BaseConfiguration;

import com.dattack.dbtools.integrity.beans.EventActionBeanVisitor;
import com.dattack.dbtools.integrity.beans.EventActionExecuteSqlBean;
import com.dattack.dbtools.integrity.beans.EventActionLogBean;
import com.dattack.dbtools.integrity.beans.EventActionThrowErrorBean;
import com.dattack.dbtools.integrity.beans.EventActionThrowWarningBean;
import com.dattack.dbtools.integrity.beans.SourceBean;
import com.dattack.ext.misc.ConfigurationUtil;

/**
 * @author cvarela
 * @since 0.1
 */
public class DefaultEventActionVisitor implements EventActionBeanVisitor {

    private final List<RowData> rowDataList;
    private final FlightRecorder flightRecorder;

    private enum Severity {

        EMPTY(""), WARNING("Warning"), ERROR("Error");

        private String name;

        Severity(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public DefaultEventActionVisitor(final List<RowData> rowDataList, final FlightRecorder flightRecorder) {
        this.rowDataList = rowDataList;
        this.flightRecorder = flightRecorder;
    }

    @Override
    public void visite(final EventActionExecuteSqlBean item) {

        populateContext();
        for (final SourceBean sourceBean : item.getSourceList()) {

            SourceResult sourceResult = null;
            try {
                final SourceExecutor sourceExecutor = new SourceExecutor(sourceBean);
                sourceResult = sourceExecutor.call();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (sourceResult != null) {
                    sourceResult.close();
                }
            }
        }
    }

    private void populateContext() {
        for (final RowData rowData : rowDataList) {
            for (final IdentifierValuePair item : rowData.getFieldValueList()) {
                ExecutionContext.getInstance().getConfiguration().setProperty(
                        String.format("%s.%s", rowData.getSourceId().getValue(), item.getKey().getValue()),
                        item.getValue());
            }
        }
    }

    @Override
    public void visite(final EventActionLogBean action) {
        flightRecorder.appendLog(log(Severity.EMPTY));
    }

    @Override
    public void visite(final EventActionThrowErrorBean action) {
        visite(action.getTemplate(), Severity.ERROR, action.isMaxEventsReached());
    }

    @Override
    public void visite(final EventActionThrowWarningBean action) {
        visite(action.getTemplate(), Severity.WARNING, action.isMaxEventsReached());
    }

    private void visite(final String template, final Severity severity, final boolean maxEventsReached) {

        String message = interpolate(template, log(severity));

        switch (severity) {
        case WARNING:
            flightRecorder.handleWarning(message);
            break;
        case ERROR:
            flightRecorder.handleError(message);
            break;
        default:
            break;
        }

        if (maxEventsReached) {
            throw new RuntimeException(String.format("%s: %s", severity, message));
        }
    }

    private String interpolate(final String message, final String log) {
        BaseConfiguration configuration = new BaseConfiguration();
        configuration.setProperty(PropertyNames.LOG, log);
        return ConfigurationUtil.interpolate(message, configuration);
    }

    private String log(final Severity severity) {
        StringBuilder sb = new StringBuilder();
        for (final RowData rowData : rowDataList) {
            sb.append("\n[").append(severity.getName()).append("@").append(rowData.getSourceId().getValue())
            .append("]");

            if (rowData.getFieldValueList().isEmpty()) {
                sb.append("\tNo data available.");
                continue;
            }
            for (final IdentifierValuePair item : rowData.getFieldValueList()) {
                sb.append("\t").append(item.getKey().getValue()).append("=").append(item.getValue());
            }
        }
        return sb.toString();
    }
}
