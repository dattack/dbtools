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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.drules.beans.Identifier;
import com.dattack.dbtools.drules.beans.JoinBean;
import com.dattack.dbtools.drules.beans.JoinResultBean;

/**
 * @author cvarela
 * @since 0.1
 */
public class InnerJoinStrategy implements JoinStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(InnerJoinStrategy.class);

    private final SourceResultGroup sourceResultList;
    private final JoinBean joinBean;

    public InnerJoinStrategy(final SourceResultGroup sourceResultList, final JoinBean joinBean) {
        this.sourceResultList = sourceResultList;
        this.joinBean = joinBean;
    }

    private RowDataMap createRowDataMap() throws SQLException {

        final RowDataMap rowDataMap = new RowDataMap();
        for (final SourceResult sr : sourceResultList) {
            final RowDataFactory factory = new RowDataFactory(sr.getSourceAlias(), joinBean.getUsing());
            rowDataMap.putData(sr.getSourceAlias(), factory.create(sr));
        }
        return rowDataMap;
    }

    @Override
    public void execute(final FlightRecorder flightRecorder) {

        try {

            setExecutionProperties();

            final RowDataMap rowDataMap = createRowDataMap();

            JoinKey minKey = null;
            do {
                minKey = rowDataMap.getMinKey();

                LOGGER.debug("Min key: {}", minKey);

                if (minKey == null) {
                    break;
                }

                final List<RowData> currentRowDataList = new ArrayList<RowData>(rowDataMap.getSize());
                final List<Identifier> missingSourceList = new ArrayList<Identifier>();
                for (final RowData rowData : rowDataMap.getRows()) {

                    currentRowDataList.add(rowData);

                    if (minKey.equals(rowData.getKey())) {
                        // matches
                        final RowDataFactory factory = new RowDataFactory(rowData.getSourceId(), joinBean.getUsing());
                        rowDataMap.putData(rowData.getSourceId(),
                                factory.create(sourceResultList.get(rowData.getSourceId())));

                        LOGGER.debug("    Source: {}, Key: {} -> Matches", rowData.getSourceId().getValue(),
                                rowData.getKey());
                    } else {
                        // no matches
                        LOGGER.debug("    Source: {}, Key: {} -> Not Matches", rowData.getSourceId().getValue(),
                                rowData.getKey());
                        missingSourceList.add(rowData.getSourceId());
                    }
                }

                executeJoinResult(missingSourceList, currentRowDataList, flightRecorder);

            } while (true);

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    private void executeJoinResult(final List<Identifier> missingSourceList, final List<RowData> rowDataList,
            final FlightRecorder flightRecorder) {

        final DefaultJoinResultVisitor visitor = new DefaultJoinResultVisitor(missingSourceList, rowDataList,
                flightRecorder);
        for (final JoinResultBean joinResult : joinBean.getEventList()) {
            joinResult.accept(visitor);
        }
    }

    private void setExecutionProperties() {

        final StringBuilder joinUsingBuilder = new StringBuilder();
        joinUsingBuilder.append("USING[");
        for (int i = 0; i < joinBean.getUsing().size(); i++) {
            final Identifier identifier = joinBean.getUsing().get(i);
            if (i > 0) {
                joinUsingBuilder.append(", ");
            }
            joinUsingBuilder.append(identifier.getValue());
        }
        joinUsingBuilder.append("]");

        ThreadContext.getInstance().setProperty(PropertyNames.JOIN_CONDITION, joinUsingBuilder.toString());
    }
}
