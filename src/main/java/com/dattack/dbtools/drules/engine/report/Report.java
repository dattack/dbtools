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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.GlobalConfiguration;
import com.dattack.dbtools.TemplateHelper;
import com.dattack.dbtools.drules.beans.EventActionThrowErrorBean;
import com.dattack.dbtools.drules.beans.EventActionThrowWarningBean;
import com.dattack.dbtools.drules.beans.AbstractEventActionThrowableBean;
import com.dattack.dbtools.drules.engine.IdentifierValuePair;
import com.dattack.dbtools.drules.engine.PropertyNames;
import com.dattack.dbtools.drules.engine.RowData;
import com.dattack.dbtools.drules.engine.ThreadContext;
import com.dattack.ext.misc.ConfigurationUtil;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author cvarela
 * @since 0.1
 */
public class Report {

    private static final Logger LOGGER = LoggerFactory.getLogger(Report.class);

    private static final String STATUS_WARNING = "WARNING";
    private static final String STATUS_ERROR = "ERROR";

    private final StringBuilder buffer;

    public Report() {
        this.buffer = new StringBuilder();
    }

    void append(final String text) {
        buffer.append(text);
    }

    private Template createTemplate(final AbstractEventActionThrowableBean action)
            throws ConfigurationException, IOException {

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

    private void handle(final AbstractEventActionThrowableBean action, final List<RowData> rowDataList,
            final String status) {

        try {
            final Map<Object, Object> dataModel = new HashMap<Object, Object>();
            dataModel.putAll(ConfigurationConverter.getMap(ThreadContext.getInstance().getConfiguration()));
            dataModel.put(PropertyNames.STATUS, status);
            dataModel.put("rowDataList", rowDataList);
            dataModel.put(PropertyNames.LOG, log(rowDataList));

            final StringWriter outputWriter = new StringWriter();
            final Template template = createTemplate(action);
            template.process(dataModel, outputWriter);
            append(outputWriter.toString());
        } catch (ConfigurationException | IOException | TemplateException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    public void handleError(final EventActionThrowErrorBean action, final List<RowData> rowDataList) {
        handle(action, rowDataList, STATUS_ERROR);
    }

    public void handleLog(final List<RowData> rowDataList) {

        append(log(rowDataList));
    }

    public void handleWarning(final EventActionThrowWarningBean action, final List<RowData> rowDataList) {
        handle(action, rowDataList, STATUS_WARNING);
    }

    String interpolate(final String message, final String status, final String log) {
        final CompositeConfiguration configuration = new CompositeConfiguration(
                ThreadContext.getInstance().getConfiguration());
        configuration.setProperty(PropertyNames.LOG, log);
        configuration.setProperty(PropertyNames.STATUS, status);
        return ConfigurationUtil.interpolate(message, configuration);
    }

    private String log(final List<RowData> rowDataList) {

        final StringBuilder buffer = new StringBuilder();
        for (final RowData rowData : rowDataList) {
            for (final Iterator<IdentifierValuePair> it = rowData.getFieldValueList().iterator(); it.hasNext();) {
                final IdentifierValuePair item = it.next();
                buffer.append(item.getKey()).append(": ").append(item.getValue());
                if (it.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
