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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.drules.beans.CheckExprBean;
import com.dattack.dbtools.drules.beans.CheckExprResultBean;
import com.dattack.dbtools.drules.beans.EventActionBean;
import com.dattack.dbtools.drules.beans.Identifier;
import com.dattack.dbtools.drules.beans.JoinResultBeanVisitor;
import com.dattack.dbtools.drules.beans.JoinResultMatchBean;
import com.dattack.dbtools.drules.beans.JoinResultMissingBean;
import com.dattack.ext.script.JavaScriptEngine;

/**
 * @author cvarela
 * @since 0.1
 */
public class DefaultJoinResultVisitor implements JoinResultBeanVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJoinResultVisitor.class);

    private final List<Identifier> missingSourceList;
    private final List<RowData> rowDataList;
    private final FlightRecorder flightRecorder;

    public DefaultJoinResultVisitor(final List<Identifier> missingSourceList, final List<RowData> rowDataList,
            final FlightRecorder flightRecorder) {
        this.missingSourceList = missingSourceList;
        this.rowDataList = rowDataList;
        this.flightRecorder = flightRecorder;
    }

    private Map<Object, Object> createJavascriptParametersMap() {
        final HashMap<Object, Object> params = new HashMap<Object, Object>();
        for (final RowData rowData : rowDataList) {

            final HashMap<String, Object> sourceParams = new HashMap<String, Object>();
            params.put(rowData.getSourceId().getValue(), sourceParams);

            for (final IdentifierValuePair item : rowData.getFieldValueList()) {
                sourceParams.put(item.getKey().getValue(), item.getValue());
            }
        }
        return params;
    }

    private void execute(final CheckExprBean checkExprBean) throws ScriptException {

        registerCheckExpr(checkExprBean);

        // eval the check expression
        LOGGER.debug(String.format("Executing javascript expression: %s", checkExprBean.getExpression()));
        final Boolean bool = JavaScriptEngine.evalBoolean(checkExprBean.getExpression(),
                createJavascriptParametersMap());
        if (bool == null || !bool) {
            execute(checkExprBean.getOnFail());
        } else {
            execute(checkExprBean.getOnSucess());
        }
    }

    private void execute(final CheckExprResultBean checkExprResult) {

        if (checkExprResult != null) {
            execute(checkExprResult.getActionList());
        }
    }

    private void execute(final List<EventActionBean> actionList) {

        final DefaultEventActionVisitor visitor = new DefaultEventActionVisitor(rowDataList, flightRecorder);
        for (final EventActionBean action : actionList) {
            action.accept(visitor);
        }
    }

    private void registerCheckExpr(final CheckExprBean checkExprBean) {
        ThreadContext.getInstance().setProperty(PropertyNames.CHECK_EXPR, checkExprBean.getExpression());
        registerMissingSource(null);
    }

    private void registerMissingSource(final Identifier sourceIdentifier) {

        if (sourceIdentifier == null) {
            ThreadContext.getInstance().clearProperty(PropertyNames.MISSING_SOURCE);
        } else {
            ThreadContext.getInstance().setProperty(PropertyNames.MISSING_SOURCE, sourceIdentifier.getValue());
        }
    }

    @Override
    public void visit(final JoinResultMatchBean item) {
        if (missingSourceList.isEmpty()) {
            for (final CheckExprBean checkExprBean : item.getCheckList()) {
                try {
                    execute(checkExprBean);
                } catch (final ScriptException e) {
                    // TODO: throw a proper exception
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void visit(final JoinResultMissingBean item) {
        if (!missingSourceList.isEmpty()) {
            if (missingSourceList.contains(item.getSourceId())) {
                registerMissingSource(item.getSourceId());
                execute(item.getActionList());
            }
        }
    }
}
