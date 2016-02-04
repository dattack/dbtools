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
import com.dattack.dbtools.drules.beans.EventActionEvalJSBean;
import com.dattack.dbtools.drules.beans.EventActionExecuteSqlBean;
import com.dattack.dbtools.drules.beans.EventActionLogBean;
import com.dattack.dbtools.drules.beans.EventActionThrowErrorBean;
import com.dattack.dbtools.drules.beans.EventActionThrowWarningBean;
import com.dattack.dbtools.drules.beans.SourceBean;
import com.dattack.ext.script.JavaScriptEngine;

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

	@Override
	public void visite(final EventActionEvalJSBean item) {

		try {
			Object value = JavaScriptEngine.eval(item.getExpression(),
					ConfigurationConverter.getMap(ThreadContext.getInstance().getConfiguration()));
			ThreadContext.getInstance().setProperty(item.getName(), value);
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void visite(final EventActionExecuteSqlBean item) {

		populateContext();
		for (final SourceBean sourceBean : item.getSourceList()) {

			SourceResult sourceResult = null;
			try {
				final SourceExecutor sourceExecutor = new SourceExecutor(sourceBean,
						ConfigurationUtils.cloneConfiguration(ThreadContext.getInstance().getConfiguration()));
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
				ThreadContext.getInstance().setProperty(rowData.getSourceId().append(item.getKey()).getValue(),
						item.getValue());
			}
		}
	}

	@Override
	public void visite(final EventActionLogBean action) {
		flightRecorder.handleLog(action, rowDataList);
	}

	@Override
	public void visite(final EventActionThrowErrorBean action) {
		flightRecorder.handleError(action, rowDataList);
	}

	@Override
	public void visite(final EventActionThrowWarningBean action) {
		flightRecorder.handleWarning(action, rowDataList);
	}
}
