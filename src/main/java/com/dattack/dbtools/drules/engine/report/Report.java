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
package com.dattack.dbtools.drules.engine.report;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import com.dattack.dbtools.GlobalConfiguration;
import com.dattack.dbtools.TemplateHelper;
import com.dattack.dbtools.drules.beans.EventActionLogBean;
import com.dattack.dbtools.drules.beans.EventActionThrowErrorBean;
import com.dattack.dbtools.drules.beans.EventActionThrowWarningBean;
import com.dattack.dbtools.drules.beans.EventActionThrowableBean;
import com.dattack.dbtools.drules.engine.ExecutionContext;
import com.dattack.dbtools.drules.engine.PropertyNames;
import com.dattack.dbtools.drules.engine.RowData;
import com.dattack.ext.misc.ConfigurationUtil;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author cvarela
 * @since 0.1
 */
public class Report {

    private static final String STATUS_WARNING = "WARNING";
    private static final String STATUS_ERROR = "ERROR";

    private final StringBuilder buffer;

    public Report() {
        this.buffer = new StringBuilder();
    }

    void append(final String text) {
        buffer.append(text);
    }

    public void handleLog(final EventActionLogBean action, final List<RowData> rowDataList) {

        append(log(rowDataList));
    }

    public void handleError(final EventActionThrowErrorBean action, final List<RowData> rowDataList) {
        handle(action, rowDataList, STATUS_ERROR);
    }

    public void handleWarning(final EventActionThrowWarningBean action, final List<RowData> rowDataList) {
        handle(action, rowDataList, STATUS_WARNING);
    }

    private void handle(final EventActionThrowableBean action, final List<RowData> rowDataList, final String status) {

        try {
            Template template = createTemplate(action);
            Map<Object, Object> dataModel = new HashMap<Object, Object>();
            dataModel.putAll(ConfigurationConverter.getMap(ExecutionContext.getInstance().getConfiguration()));
            dataModel.put(PropertyNames.STATUS, status);
            dataModel.put("rowDataList", rowDataList);
            dataModel.put(PropertyNames.LOG, log(rowDataList));

            StringWriter outputWriter = new StringWriter();
            template.process(dataModel, outputWriter);
            append(outputWriter.toString());
        } catch (IOException | TemplateException | ConfigurationException e) {
            // TODO: launch a RuntimeException
        }
    }

    private Template createTemplate(final EventActionThrowableBean action) throws ConfigurationException, IOException {

        if (StringUtils.isNotBlank(action.getTemplateText())) {
            return TemplateHelper.createTemplate(action.getTemplateText());
        }

        if (StringUtils.isNotBlank(action.getTemplateFile())) {
            return TemplateHelper.loadTemplate(action.getTemplateFile());
        }

        // use default template
        return TemplateHelper
                .loadTemplate(GlobalConfiguration.getProperty(GlobalConfiguration.DRULES_TEMPLATE_THROWABLE_KEY));
    }

    private String log(final List<RowData> rowDataList) {
        return "";
    }

    // private String log(final List<RowData> rowDataList) {
    //
    // StringBuilder sb = new StringBuilder();
    // sb.append("<table><tr>");
    //
    // for (final RowData rowData : rowDataList) {
    // sb.append("<td><table border=1>");
    // sb.append("<tr bgcolor=\"#0101DF\"><td colspan=2 style=\"font-weight: bold;color: #FFFFFF;\">")
    // .append(rowData.getSourceId().getValue()).append("</td></tr>");
    //
    // if (rowData.getFieldValueList().isEmpty()) {
    // sb.append("<tr><td colspan=2>No data available.</td></tr>");
    // }
    //
    // for (final IdentifierValuePair item : rowData.getFieldValueList()) {
    // sb.append("<tr><td bgcolor=\"#0101DF\" style=\"font-weight: bold;color: #FFFFFF;\">")
    // .append(item.getKey().getValue()).append("</td><td>").append(item.getValue())
    // .append("</td></tr>");
    // }
    // sb.append("</table></td>");
    // }
    // sb.append("</tr></table>");
    // return sb.toString();
    // }

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
