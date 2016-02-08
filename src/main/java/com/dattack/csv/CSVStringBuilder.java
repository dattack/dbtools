/*
 * Copyright (c) 2014, The Dattack team (http://www.dattack.com)
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
package com.dattack.csv;

import java.util.Date;

import org.apache.commons.lang.ObjectUtils;

/**
 * A utility class for creating CSV data from scratch. This class is not thread-safe and with no guarantee of
 * synchronization. The methods in this class can be chained to add multiple rows/columns to the object.
 *
 * @author cvarela
 * @since 0.1
 */
public class CSVStringBuilder {

    private static final int DEFAULT_CAPACITY = 100;

    private boolean isComment;

    private final CSVConfiguration configuration;
    private boolean emptyLine;
    private final StringBuilder rebuild;

    public CSVStringBuilder(final CSVConfiguration configuration) {
        this(configuration, DEFAULT_CAPACITY);
    }

    /**
     * Constructs a CSV builder with an initial capacity specified by the <code>capacity</code> argument.
     *
     * @param configuration
     *            the CSV configuration
     * @param capacity
     *            the initial capacity of the internal buffer
     */
    public CSVStringBuilder(final CSVConfiguration configuration, final int capacity) {
        this.configuration = configuration;
        this.rebuild = new StringBuilder(capacity);
        this.emptyLine = true;
        this.isComment = false;
    }

    /**
     * Appends a new value.
     * 
     * @param value
     *            the new value to append
     * @return the instance of CSVStringBuilder
     */
    public CSVStringBuilder append(final Date value) {
        if (value == null) {
            appendValue(configuration.getNullStr());
        } else {
            appendValue(configuration.getDateFormat().format(value));
        }
        return this;
    }

    /**
     * Appends a new value.
     * 
     * @param value
     *            the new value to append
     * @return the instance of CSVStringBuilder
     */
    public CSVStringBuilder append(final Double value) {
        appendValue(ObjectUtils.toString(value, configuration.getNullStr()));
        return this;
    }

    /**
     * Appends a new value.
     * 
     * @param value
     *            the new value to append
     * @return the instance of CSVStringBuilder
     */
    public CSVStringBuilder append(final Float value) {
        appendValue(ObjectUtils.toString(value, configuration.getNullStr()));
        return this;
    }

    /**
     * Appends a new value.
     * 
     * @param value
     *            the new value to append
     * @return the instance of CSVStringBuilder
     */
    public CSVStringBuilder append(final Integer value) {
        appendValue(ObjectUtils.toString(value, configuration.getNullStr()));
        return this;
    }

    /**
     * Appends a new value.
     * 
     * @param value
     *            the new value to append
     * @return the instance of CSVStringBuilder
     */
    public CSVStringBuilder append(final Long value) {
        appendValue(ObjectUtils.toString(value, configuration.getNullStr()));
        return this;
    }

    /**
     * Appends a new value.
     * 
     * @param value
     *            the new value to append
     * @return the instance of CSVStringBuilder
     */
    public CSVStringBuilder append(final String value) {
        if (value == null) {
            appendValue(configuration.getNullStr());
        } else {
            appendValue(value, !isComment);
        }

        return this;
    }

    private void appendValue(final String value) {
        appendValue(value, false);
    }

    private void appendValue(final String value, final boolean quote) {

        if (!emptyLine) {
            rebuild.append(configuration.getSeparator());
        }

        if (quote) {
            rebuild.append(configuration.getQuoteChar()).append(value).append(configuration.getQuoteChar());
        } else {
            rebuild.append(value);
        }

        emptyLine = false;
    }

    /**
     * Clear the internal buffer.
     * 
     * @return the instance of CSVStringBuilder
     */
    public CSVStringBuilder clear() {
        rebuild.setLength(0);
        emptyLine = true;
        isComment = false;
        return this;
    }

    /**
     * Starts a new comment block.
     *
     * @return the instance of CSVStringBuilder
     */
    public CSVStringBuilder comment() {
        comment(null, false);
        return this;
    }

    /**
     * Appends a comment.
     * 
     * @param message
     *            the comment to add
     * @return the instance of CSVStringBuilder
     */
    public CSVStringBuilder comment(final String message) {
        comment(message, true);
        return this;
    }

    private void comment(final String message, final boolean eol) {

        if (!emptyLine || isComment) {
            eol();
        }

        rebuild.append(configuration.getCommentChar());
        if (message != null) {
            rebuild.append(message);
        }

        isComment = true;

        if (eol) {
            eol();
        }
    }

    /**
     * Appends the end-of-line mark.
     *
     * @return the instance of CSVStringBuilder
     */
    public CSVStringBuilder eol() {
        rebuild.append(configuration.getEol());
        emptyLine = true;
        isComment = false;
        return this;
    }

    @Override
    public String toString() {
        return rebuild.toString();
    }
}
