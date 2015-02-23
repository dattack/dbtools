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
package com.dattack.dbtools.ping.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dattack.dbtools.ping.LogEntry;

/**
 * @author cvarela
 * @since 0.1
 */
class ReportStats {

    private static final String CONNECTION_TIME_KEY = "Connection time";
    private static final String FIRST_ROW_TIME_KEY = "First row time";
    private static final String EXECUTION_TIME_KEY = "Execution time";

    private final Map<String, EntryGroup> groupMap;
    private final Context context;

    public ReportStats(final Context context) {
        this.context = context;
        this.groupMap = new HashMap<String, EntryGroup>();
    }

    List<EntryStats> add(final LogEntry logEntry) {

        List<EntryStats> list = new ArrayList<EntryStats>();

        String x = context.getDateFormat().format(new Date(logEntry.getStartTime()));

        // connection time
        list.add(new EntryStats(x, logEntry.getConnectionTime(), getGroup(
                getKey(logEntry.getTaskName(), logEntry.getSqlLabel(), CONNECTION_TIME_KEY)).getId()));

        // first row
        list.add(new EntryStats(x, logEntry.getFirstRowTime(), getGroup(
                getKey(logEntry.getTaskName(), logEntry.getSqlLabel(), FIRST_ROW_TIME_KEY)).getId()));

        // execution time
        list.add(new EntryStats(x, logEntry.getExecutionTime(), getGroup(
                getKey(logEntry.getTaskName(), logEntry.getSqlLabel(), EXECUTION_TIME_KEY)).getId()));

        return list;
    }

    List<EntryGroup> getEntryGroups() {

        List<EntryGroup> list = new ArrayList<EntryGroup>();
        list.addAll(groupMap.values());
        return list;
    }

    private EntryGroup getGroup(final String key) {
        EntryGroup group = groupMap.get(key);
        if (group == null) {
            group = new EntryGroup(groupMap.size(), key);
            groupMap.put(key, group);
        }
        return group;
    }

    private String getKey(final String taskName, final String sqlLabel, final String metricName) {
        return String.format("%s (%s - %s)", metricName, normalize(taskName), normalize(sqlLabel));
    }

    private String normalize(final String text) {
        return text.replaceAll("\"", "");
    }
}
