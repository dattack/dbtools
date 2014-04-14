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

import java.text.SimpleDateFormat;
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

	private boolean comment;

	private CSVConfiguration configuration;
	private final SimpleDateFormat df;
	private boolean emptyLine;
	private final StringBuilder sb;

	public CSVStringBuilder(final CSVConfiguration configuration) {
		this(configuration, DEFAULT_CAPACITY);
	}

	/**
	 * Constructs a CSV builder with an initial capacity specified by the <code>capacity</code> argument.
	 */
	public CSVStringBuilder(final CSVConfiguration configuration, int capacity) {
		this.configuration = configuration;
		this.sb = new StringBuilder(capacity);
		this.emptyLine = true;
		this.comment = false;
		this.df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.S");
	}

	public CSVStringBuilder append(final Date value) {
		if (value == null) {
			appendValue(configuration.getNullStr());
		} else {
			appendValue(df.format(value));
		}
		return this;
	}

	public CSVStringBuilder append(final Double value) {
		appendValue(ObjectUtils.toString(value, configuration.getNullStr()));
		return this;
	}

	public CSVStringBuilder append(final Float value) {
		appendValue(ObjectUtils.toString(value, configuration.getNullStr()));
		return this;
	}

	public CSVStringBuilder append(final Integer value) {
		appendValue(ObjectUtils.toString(value, configuration.getNullStr()));
		return this;
	}

	public CSVStringBuilder append(final Long value) {
		appendValue(ObjectUtils.toString(value, configuration.getNullStr()));
		return this;
	}

	public CSVStringBuilder append(final String value) {
		if (value == null) {
			appendValue(configuration.getNullStr());
		} else {
			appendValue(value, !comment);
		}

		return this;
	}

	private void appendValue(final String value) {
		appendValue(value, false);
	}

	private void appendValue(final String value, boolean quote) {

		if (!emptyLine) {
			sb.append(configuration.getSeparator());
		}

		if (quote) {
			sb.append(configuration.getQuoteChar()).append(value).append(configuration.getQuoteChar());
		} else {
			sb.append(value);
		}

		emptyLine = false;
	}

	public CSVStringBuilder clear() {
		sb.setLength(0);
		emptyLine = true;
		comment = false;
		return this;
	}

	public CSVStringBuilder comment() {
		comment(null, false);
		return this;
	}

	public CSVStringBuilder comment(final String message) {
		comment(message, true);
		return this;
	}

	private void comment(final String message, final boolean eol) {

		if (!emptyLine || comment) {
			eol();
		}

		sb.append(configuration.getCommentChar());
		if (message != null) {
			sb.append(message);
		}

		comment = true;
		
		if (eol) {
			eol();
		}
	}

	public CSVStringBuilder eol() {
		sb.append(configuration.getEOL());
		emptyLine = true;
		comment = false;
		return this;
	}

	@Override
	public String toString() {
		return sb.toString();
	}
}
