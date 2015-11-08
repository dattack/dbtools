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

import com.dattack.dbtools.integrity.beans.EventActionLogBean;
import com.dattack.dbtools.integrity.beans.EventActionThrowErrorBean;
import com.dattack.dbtools.integrity.beans.EventActionThrowWarningBean;
import com.dattack.dbtools.integrity.engine.IdentifierValuePair;
import com.dattack.dbtools.integrity.engine.RowData;

/**
 * @author cvarela
 * @since 0.1
 */
public class HtmlReport extends Report {

    @Override
    public void handleLog(final EventActionLogBean action, final List<RowData> rowDataList) {

        append(log(rowDataList));
    }

    @Override
    public void handleError(final EventActionThrowErrorBean action, final List<RowData> rowDataList) {
        append(interpolate(action.getTemplate(), STATUS_ERROR, log(rowDataList)));
    }

    @Override
    public void handleWarning(final EventActionThrowWarningBean action, final List<RowData> rowDataList) {
        append(interpolate(action.getTemplate(), STATUS_WARNING, log(rowDataList)));
    }

    private String log(final List<RowData> rowDataList) {

        StringBuilder sb = new StringBuilder();
        sb.append("<table><tr>");

        for (final RowData rowData : rowDataList) {
            sb.append("<td><table border=1>");
            sb.append("<tr bgcolor=\"#0101DF\"><td colspan=2 style=\"font-weight: bold;color: #FFFFFF;\">")
            .append(rowData.getSourceId().getValue()).append("</td></tr>");

            if (rowData.getFieldValueList().isEmpty()) {
                sb.append("<tr><td colspan=2>No data available.</td></tr>");
            }

            for (final IdentifierValuePair item : rowData.getFieldValueList()) {
                sb.append("<tr><td bgcolor=\"#0101DF\" style=\"font-weight: bold;color: #FFFFFF;\">")
                .append(item.getKey().getValue()).append("</td><td>")
                .append(item.getValue())
                .append("</td></tr>");
            }
            sb.append("</table></td>");
        }
        sb.append("</tr></table>");
        return sb.toString();
    }
}
