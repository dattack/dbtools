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

import javax.script.ScriptException;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationUtils;

import com.dattack.dbtools.drules.beans.EventActionBeanVisitor;
import com.dattack.dbtools.drules.beans.EventActionEvalJsBean;
import com.dattack.dbtools.drules.beans.EventActionExecuteSqlBean;
import com.dattack.dbtools.drules.beans.EventActionLogBean;
import com.dattack.dbtools.drules.beans.EventActionThrowErrorBean;
import com.dattack.dbtools.drules.beans.EventActionThrowWarningBean;
import com.dattack.dbtools.drules.beans.SourceBean;
import com.dattack.dbtools.drules.exceptions.DrulesNestableException;
import com.dattack.dbtools.drules.exceptions.DrulesNestableRuntimeException;
import com.dattack.jtoolbox.script.JavaScriptEngine;

/**
 * @author cvarela
 * @since 0.1
 */
public class DefaultEventActionVisitor implements EventActionBeanVisitor {

    private final List<RowData> rowDataList;
    private final FlightRecorder flightRecorder;

    public DefaultEventActionVisitor(final List<RowData> rowDataList, final FlightRecorder flightRecorder) {
        this.rowDataList = rowDataList;
        this.flightRecorder = flightRecorder;
    }

    private void populateContext() {
        for (final RowData rowData : rowDataList) {
            for (final IdentifierValuePair item : rowData.getFieldValueList()) {
                ThreadContext.getInstance().setProperty(rowData.getSourceId().append(item.getKey()).getValue(),
                        item.getValue());
            }
        }
    }

    @Override
    public void visit(final EventActionEvalJsBean item) {

        try {
            final Object value = JavaScriptEngine.eval(item.getExpression(),
                    ConfigurationConverter.getMap(ThreadContext.getInstance().getConfiguration()));
            ThreadContext.getInstance().setProperty(item.getName(), value);
        } catch (final ScriptException e) {
            throw new DrulesNestableRuntimeException(e);
        }
    }

    @Override
    public void visit(final EventActionExecuteSqlBean item) {

        populateContext();
        for (final SourceBean sourceBean : item.getSourceList()) {

            SourceResult sourceResult = null;
            try {
                final SourceExecutor sourceExecutor = new SourceExecutor(sourceBean,
                        ConfigurationUtils.cloneConfiguration(ThreadContext.getInstance().getConfiguration()));
                sourceResult = sourceExecutor.call();
            } catch (final DrulesNestableException e) {
                throw new DrulesNestableRuntimeException(e);
            } finally {
                if (sourceResult != null) {
                    sourceResult.close();
                }
            }
        }
    }

    @Override
    public void visit(final EventActionLogBean action) {
        flightRecorder.handleLog(rowDataList);
    }

    @Override
    public void visit(final EventActionThrowErrorBean action) {
        flightRecorder.handleError(action, rowDataList);
    }

    @Override
    public void visit(final EventActionThrowWarningBean action) {
        flightRecorder.handleWarning(action, rowDataList);
    }
}
