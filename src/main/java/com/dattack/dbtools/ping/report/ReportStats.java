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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dattack.dbtools.ping.LogEntry;

/**
 * @author cvarela
 * @since 0.1
 */
class ReportStats {

    private final Map<MetricName, EntryGroup> groupMap;
    private final HashMap<Integer, EntryStats> entryStatsMap;
    private final ReportContext context;

    public ReportStats(final ReportContext context) {
        this.context = context;
        this.groupMap = new HashMap<MetricName, EntryGroup>();
        this.entryStatsMap = new HashMap<Integer, EntryStats>();
    }

    List<EntryStats> add(final LogEntry logEntry) {

        long eventTime = normalizeEventTime(logEntry.getStartTime());

        List<EntryStats> list = new ArrayList<EntryStats>();

        // connection time
        addEntryStats(list, new MetricName(logEntry.getTaskName(), logEntry.getSqlLabel(),
                MetricName.CONNECTION_TIME_KEY), eventTime, logEntry.getConnectionTime());

        // first row
        addEntryStats(list, new MetricName(logEntry.getTaskName(), logEntry.getSqlLabel(),
                MetricName.FIRST_ROW_TIME_KEY), eventTime, logEntry.getFirstRowTime());

        // execution time
        addEntryStats(list, new MetricName(logEntry.getTaskName(), logEntry.getSqlLabel(),
                MetricName.EXECUTION_TIME_KEY), eventTime, logEntry.getExecutionTime());

        return list;
    }

    private long normalizeEventTime(final long eventTime) {
        if (context.getTimeSpan() != null && context.getTimeSpan() > 0) {
            return (eventTime / context.getTimeSpan()) * context.getTimeSpan();
        }
        return eventTime;
    }

    private EntryStats process(final EntryStats entryStats) {

        EntryStats previousEntryStats = entryStatsMap.get(entryStats.getGroup());
        if (previousEntryStats == null) {
            // it's a new metric
            entryStatsMap.put(entryStats.getGroup(), entryStats);
        } else if (previousEntryStats.getX() < entryStats.getX()) {
            // it's a new X value so returns the previous one
            entryStatsMap.put(entryStats.getGroup(), entryStats);
            return previousEntryStats;

        } else if (previousEntryStats.getX() == entryStats.getX() && previousEntryStats.getY() < entryStats.getY()) {
            // update with MAX value
            entryStatsMap.put(entryStats.getGroup(), entryStats);
        }
        return null;
    }

    List<EntryGroup> getEntryGroups() {

        List<EntryGroup> list = new ArrayList<EntryGroup>();
        list.addAll(groupMap.values());
        return list;
    }

    private EntryGroup getGroup(final MetricName key) {
        EntryGroup group = groupMap.get(key);
        if (group == null) {
            group = new EntryGroup(groupMap.size(), key);
            groupMap.put(key, group);
        }
        return group;
    }

    private void addEntryStats(final List<EntryStats> list, final MetricName metricName, final long x, final long y) {

        if (context.getMetricNameList().isEmpty() || context.getMetricNameList().contains(metricName)) {

            EntryStats entry = process(new EntryStats(x, normalizeValue(y), getGroup(metricName).getId()));
            if (entry != null) {
                list.add(entry);
            }
        }
    }

    private long normalizeValue(final long value) {

        long normalizedValue = value;
        if (context.getMinValue() != null && normalizedValue < context.getMinValue()) {
            normalizedValue = context.getMinValue();
        } else if (context.getMaxValue() != null && normalizedValue > context.getMaxValue()) {
            normalizedValue = context.getMaxValue();
        }
        return normalizedValue;
    }
}
