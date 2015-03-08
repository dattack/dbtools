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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author cvarela
 *
 */
public class ReportContext {

    private SimpleDateFormat dateFormat;
    private Date endDate;
    private Date startDate;
    private Long timeSpan;
    private Long maxValue;
    private Long minValue;
    private final List<MetricName> metricNameList;

    public ReportContext() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.metricNameList = new ArrayList<MetricName>();
    }

    public Long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(final Long maxValue) {
        this.maxValue = maxValue;
    }

    public Long getMinValue() {
        return minValue;
    }

    public void setMinValue(final Long minValue) {
        this.minValue = minValue;
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public Long getTimeSpan() {
        return timeSpan;
    }

    public void setTimeSpan(final Long timeSpan) {
        this.timeSpan = timeSpan;
    }

    public void setDateFormat(final SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void addMetricNameFilter(final MetricName item) {
        this.metricNameList.add(item);
    }

    public List<MetricName> getMetricNameList() {
        return metricNameList;
    }
}
