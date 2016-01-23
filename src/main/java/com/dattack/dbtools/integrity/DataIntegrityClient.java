/*
 * Copyright (c) 2016, The Dattack team (http://www.dattack.com)
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
package com.dattack.dbtools.integrity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.integrity.beans.Identifier;
import com.dattack.dbtools.integrity.beans.Identifier.IdentifierBuilder;
import com.dattack.dbtools.integrity.engine.DataIntegrityEngine;

/**
 * @author cvarela
 * @since 0.1
 */
public final class DataIntegrityClient {

    private static final Logger log = LoggerFactory.getLogger(DataIntegrityClient.class);

    private DataIntegrityClient() {
        // static class
    }

    /**
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {

        try {

            if (args.length < 2) {
                log.error("Usage: DataIntegrityClient <configuration_file_path> <task_id>");
                return;
            }
            int argIndex = 0;
            String filename = args[argIndex++];
            Identifier taskId = new IdentifierBuilder().withValue(args[argIndex++]).build();
            String configurationFilename = null;

            final DataIntegrityEngine engine = new DataIntegrityEngine();
            engine.execute(filename, taskId, configurationFilename);
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
    }
}
