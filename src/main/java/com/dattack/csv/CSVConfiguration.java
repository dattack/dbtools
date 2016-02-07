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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.dattack.dbtools.Builder;

/**
 * @author cvarela
 * @since 0.1
 */
public final class CSVConfiguration {

    /**
     * Builder class used to build a {@link CSVConfiguration} instance.
     */
    public static class CsvConfigurationBuilder implements Builder<CSVConfiguration> {

        /**
         * The default comment-line character to use.
         */
        private static final char DEFAULT_COMMENT_CHAR = '#';

        /**
         * The end-of-line character to use.
         */
        private static final String DEFAULT_EOL = System.getProperty("line.separator");

        /**
         * The default escape character to use if none is supplied.
         */
        private static final char DEFAULT_ESCAPE_CHARACTER = '\\';

        /**
         * This is the "null" character - if a value is set to this then it is ignored.
         */
        private static final String DEFAULT_NULL_VALUE = "";

        /**
         * The default quote character to use if none is supplied.
         */
        private static final char DEFAULT_QUOTE_CHARACTER = '"';

        /**
         * The default separator to use if none is supplied.
         */
        private static final String DEFAULT_SEPARATOR = ",";

        private static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.S";

        private char commentChar;
        private String eol;
        private char escapeChar;
        private String nullValue;
        private char quoteChar;
        private String separator;
        private SimpleDateFormat dateFormat;

        public CsvConfigurationBuilder() {
            this.commentChar = DEFAULT_COMMENT_CHAR;
            this.separator = DEFAULT_SEPARATOR;
            this.nullValue = DEFAULT_NULL_VALUE;
            this.quoteChar = DEFAULT_QUOTE_CHARACTER;
            this.escapeChar = DEFAULT_ESCAPE_CHARACTER;
            this.eol = DEFAULT_EOL;
        }

        @Override
        public CSVConfiguration build() {
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
            }
            return new CSVConfiguration(this);
        }

        /**
         * Sets the comment character delimiter.
         *
         * @param value
         *            the value to set
         * @return the instance of CSVConfigurationBuilder
         */
        public CsvConfigurationBuilder withCommentChar(final char value) {
            this.commentChar = value;
            return this;
        }

        /**
         * Sets the end-of-line mark.
         *
         * @param value
         *            the value to set
         * @return the instance of CSVConfigurationBuilder
         */
        public CsvConfigurationBuilder withEol(final String value) {
            this.eol = value;
            return this;
        }

        /**
         * Sets the escape character.
         *
         * @param value
         *            the value to set
         * @return the instance of CSVConfigurationBuilder
         */
        public CsvConfigurationBuilder withEscapeChar(final char value) {
            this.escapeChar = value;
            return this;
        }

        /**
         * Sets the comment character delimiter.
         *
         * @param value
         *            the value to set
         * @return the instance of CSVConfigurationBuilder
         */
        public CsvConfigurationBuilder withNullValue(final String value) {
            this.nullValue = value;
            return this;
        }

        /**
         * Sets the quote character.
         *
         * @param value
         *            the value to set
         * @return the instance of CSVConfigurationBuilder
         */
        public CsvConfigurationBuilder withQuoteChar(final char value) {
            this.quoteChar = value;
            return this;
        }

        /**
         * Sets the separator character.
         *
         * @param value
         *            the value to set
         * @return the instance of CSVConfigurationBuilder
         */
        public CsvConfigurationBuilder withSeparator(final String value) {
            this.separator = value;
            return this;
        }
    }

    private final char commentChar;
    private final String eol;
    private final char escapeChar;
    private final String nullValue;
    private final char quoteChar;
    private final DateFormat dateFormat;

    private final String separator;

    private CSVConfiguration(final CsvConfigurationBuilder builder) {
        this.commentChar = builder.commentChar;
        this.separator = builder.separator;
        this.nullValue = builder.nullValue;
        this.quoteChar = builder.quoteChar;
        this.escapeChar = builder.escapeChar;
        this.eol = builder.eol;
        this.dateFormat = builder.dateFormat;
    }

    public char getCommentChar() {
        return commentChar;
    }

    public String getEol() {
        return eol;
    }

    public char getEscapeChar() {
        return escapeChar;
    }

    public String getNullStr() {
        return nullValue;
    }

    public char getQuoteChar() {
        return quoteChar;
    }

    public String getSeparator() {
        return separator;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }
}
