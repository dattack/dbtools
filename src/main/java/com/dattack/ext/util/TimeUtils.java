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
package com.dattack.ext.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * A suite of utilities surrounding the use of the {@link java.util.Calendar} and {@link java.util.Date} object.
 *
 * @author cvarela
 * @since 0.1
 */
public final class TimeUtils {

    private static final long MILLIS_PER_SECOND = 1000;
    private static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    private static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    private static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
    private static final long MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY;

    private static final List<String> ISO_8601_PATTERN_LIST = computeIso8601List();

    private TimeUtils() {
        // utility class
    }
    

    private static List<String> computeIso8601List() {

        final String[] patterns = { //
            "yyyy-MM-dd'T'HH:mm", //
            "yyyy-MM-dd'T'HH:mm:ss", //
            "yyyy-MM-dd'T'HH:mm:ss.S", //
            "yyyy-MM-dd HH:mm", //
            "yyyy-MM-dd HH:mm:ss", //
            "yyyy-MM-dd HH:mm:ss.S" };

        final List<String> patternList = Arrays.asList(patterns);
        Collections.sort(patternList, new Comparator<String>() {

            @Override
            public int compare(final String obj1, final String obj2) {
                return Integer.valueOf(obj1.length()).compareTo(obj2.length());
            }
        });
        return patternList;
    }

    /**
     * <p>
     * Parses a string representing a date by trying a variety of different parsers based on ISO 8601 standard (Data
     * elements and interchange formats -- Information interchange -- Representation of dates and times).
     * </p>
     * <p>
     * The parse will try a few parse pattern in turn. A parse is only deemed successful if it parses the whole of the
     * input string.
     * </p>
     * <b>This method is null-safe.</b>
     *
     * @param txt
     *            the date to parse
     * @return the parsed date or null if no parse patterns match
     */
    public static Date parseDate(final String txt) {

        if (StringUtils.isBlank(txt)) {
            return null;
        }

        final SimpleDateFormat parser = new SimpleDateFormat();

        for (final String pattern : ISO_8601_PATTERN_LIST) {
            if (txt.length() <= pattern.length()) {
                try {
                    parser.applyPattern(pattern);
                    return parser.parse(txt);
                } catch (@SuppressWarnings("unused") final ParseException e) {
                    // ignore exception
                }
            }
        }
        return null;
    }

    /**
     * Computes the number of milliseconds represented by the specified span expression.
     *
     * @param text
     *            the span expression
     * @return the number of milliseconds
     */
    private static Long parseTimeSpanExpression(final String text) {

        long timeInMillis = 0;
        long value = 0;
        for (int i = 0; i < text.length(); i++) {
            final char charAt = text.charAt(i);
            if (Character.isDigit(charAt)) {
                value = value * 10 + Character.digit(charAt, 10);
            } else {
                switch (charAt) {
                case 'w':
                case 'W':
                    timeInMillis += value * MILLIS_PER_WEEK;
                    break;
                case 'd':
                case 'D':
                    timeInMillis += value * MILLIS_PER_DAY;
                    break;
                case 'h':
                case 'H':
                    timeInMillis += value * MILLIS_PER_HOUR;
                    break;
                case 'm':
                case 'M':
                    timeInMillis += value * MILLIS_PER_MINUTE;
                    break;
                case 's':
                case 'S':
                    timeInMillis += value * MILLIS_PER_SECOND;
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Unknown time unit: '%s'", charAt));
                    // ignore value
                }
                value = 0;
            }
        }
        timeInMillis += value;
        return timeInMillis;
    }

    /**
     * <p>
     * Parse a time span expression and returns the milliseconds represented by this one. A valid time span expression
     * has the following format: <code>(&lt;digit+&gt;&lt;letter&gt;)+</code>. The following pattern letters are
     * defined:
     * </p>
     *
     * <table summary="">
     * <thead>
     * <tr>
     * <td>Letter</td>
     * <td>Time unit</td>
     * </tr>
     * </thead> <tbody>
     * <tr>
     * <td>w or W</td>
     * <td>Week</td>
     * </tr>
     * <tr>
     * <td>d or D</td>
     * <td>Day</td>
     * </tr>
     * <tr>
     * <td>h or H</td>
     * <td>Hour</td>
     * </tr>
     * <tr>
     * <td>m or M</td>
     * <td>Minute</td>
     * </tr>
     * <tr>
     * <td>s or S</td>
     * <td>Second</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * <p>
     * Example: <code>2h30m = 2 hours + 30 minutes = 2 * 60 * 60 * 1000 + 30 * 60 * 1000 = 9.000.000 milliseconds</code>
     * </p>
     *
     * @param text
     *            the span expression
     * @return the number of milliseconds
     */
    public static Long parseTimeSpanMillis(final String text) {

        Long result = null;
        if (text != null) {
            try {
                result = Long.valueOf(text);
            } catch (@SuppressWarnings("unused") final NumberFormatException e) {
                result = parseTimeSpanExpression(text);
            }
        }
        return result;
    }
}
