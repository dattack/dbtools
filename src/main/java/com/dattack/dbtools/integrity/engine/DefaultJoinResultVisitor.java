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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.integrity.beans.CheckExprBean;
import com.dattack.dbtools.integrity.beans.CheckExprResultBean;
import com.dattack.dbtools.integrity.beans.EventActionBean;
import com.dattack.dbtools.integrity.beans.Identifier;
import com.dattack.dbtools.integrity.beans.JoinResultBeanVisitor;
import com.dattack.dbtools.integrity.beans.JoinResultMatchBean;
import com.dattack.dbtools.integrity.beans.JoinResultMissingBean;
import com.dattack.ext.script.JavaScriptEngine;

/**
 * @author cvarela
 * @since 0.1
 */
public class DefaultJoinResultVisitor implements JoinResultBeanVisitor {

    private static final Logger log = LoggerFactory.getLogger(DefaultJoinResultVisitor.class);

    private final List<Identifier> missingSourceList;
    private final List<RowData> rowDataList;
    private final FlightRecorder flightRecorder;

    public DefaultJoinResultVisitor(final List<Identifier> missingSourceList, final List<RowData> rowDataList,
            final FlightRecorder flightRecorder) {
        this.missingSourceList = missingSourceList;
        this.rowDataList = rowDataList;
        this.flightRecorder = flightRecorder;
    }

    @Override
    public void visite(final JoinResultMatchBean item) {
        if (missingSourceList.isEmpty()) {
            for (CheckExprBean checkExprBean : item.getCheckList()) {
                try {
                    execute(checkExprBean);
                } catch (ScriptException e) {
                    // TODO: throw a proper exception
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private Map<String, Object> createParametersMap() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        for (RowData rowData : rowDataList) {
            for (IdentifierValuePair item : rowData.getFieldValueList()) {
                params.put(computeVariableName(rowData.getSourceId(), item.getKey()), item.getValue());
            }
        }
        return params;
    }

    private String computeVariableName(final Identifier sourceId, final Identifier fieldName) {
        return String.format("%s_%s", sourceId.getValue(), fieldName.getValue());
    }

    private void execute(CheckExprBean checkExprBean) throws ScriptException {

        Boolean bool = JavaScriptEngine.evalBoolean(checkExprBean.getExpression(), createParametersMap());
        if (bool == null || !bool) {
            execute(checkExprBean.getOnFail());
        } else {
            execute(checkExprBean.getOnSucess());
        }
    }

    @Override
    public void visite(final JoinResultMissingBean item) {
        if (!missingSourceList.isEmpty()) {
            if (missingSourceList.contains(item.getSourceId())) {
                execute(item.getActionList());
            }
        }
    }

    private void execute(final List<EventActionBean> actionList) {

        final DefaultEventActionVisitor visitor = new DefaultEventActionVisitor(rowDataList, flightRecorder);
        for (EventActionBean action : actionList) {
            action.accept(visitor);
        }
    }

    private void execute(final CheckExprResultBean checkExprResult) {

        if (checkExprResult != null) {
            execute(checkExprResult.getActionList());
        }
    }
}
