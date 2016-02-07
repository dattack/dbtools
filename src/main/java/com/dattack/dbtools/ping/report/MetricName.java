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

import org.apache.commons.lang.StringUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public class MetricName {

    public static final String CONNECTION_TIME_KEY = "Connection time";
    public static final String FIRST_ROW_TIME_KEY = "First row time";
    public static final String EXECUTION_TIME_KEY = "Total time";

    private final String taskName;
    private final String sqlLabel;
    private final String metric;

    public MetricName(final String taskName, final String sqlLabel, final String metric) {
        this.taskName = normalize(taskName);
        this.sqlLabel = normalize(sqlLabel);
        this.metric = normalize(metric);
    }

    private MetricName(final String[] tokens) {

        int index = 0;
        if (index < tokens.length) {
            this.taskName = normalize(tokens[index++]);
        } else {
            throw new IllegalArgumentException("Unable to instantiate a MetricName from an empty array.");
        }

        if (index < tokens.length) {
            this.sqlLabel = normalize(tokens[index++]);
            if (index < tokens.length) {
                this.metric = normalize(tokens[index++]);
            } else {
                this.metric = null;
            }
        } else {
            this.sqlLabel = null;
            this.metric = null;
        }

    }

    public String getTaskName() {
        return taskName;
    }

    public String getSqlLabel() {
        return sqlLabel;
    }

    public String getMetric() {
        return metric;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((metric == null) ? 0 : metric.hashCode());
        result = prime * result + ((sqlLabel == null) ? 0 : sqlLabel.hashCode());
        result = prime * result + ((taskName == null) ? 0 : taskName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        MetricName rhs = (MetricName) obj;
        return isEquals(taskName, rhs.taskName) && isEquals(sqlLabel, rhs.sqlLabel) && isEquals(metric, rhs.metric);
    }

    private boolean isEquals(final String text1, final String text2) {
        return text1.equalsIgnoreCase(text2) || StringUtils.isBlank(text1) || StringUtils.isBlank(text2);
    }

    @Override
    public String toString() {
        return String.format("%s (%s - %s)", metric, taskName, sqlLabel);
    }

    private String normalize(final String text) {
        return StringUtils.trimToEmpty(text).replaceAll("\"", "");
    }

    /**
     * Creates a MetricName from its value.
     * 
     * @param text
     *            the metric name
     * @return the MetricName
     */
    public static MetricName parse(final String text) {

        if (text == null) {
            throw new NullPointerException("Unable to parse a 'null' value as a metric name");
        }

        String[] tokens = text.split(":");
        if (tokens.length > 3) {
            throw new IllegalArgumentException(String.format("Unable to parse the metric name (value: %s)", text));
        }

        return new MetricName(tokens);
    }
}
