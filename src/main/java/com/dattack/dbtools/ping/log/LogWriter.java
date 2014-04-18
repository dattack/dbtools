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
package com.dattack.dbtools.ping.log;

/**
 * @author cvarela
 * @since 0.1
 */
public interface LogWriter {

    /**
     * Writes the header to the log.
     * 
     * @param logHeader
     *            the header entity
     */
    void write(final LogHeader logHeader);

    /**
     * Writes a data to the log.
     * 
     * @param logEntry
     *            the data entity
     */
    void write(final LogEntry logEntry);
}
