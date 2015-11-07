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
package com.dattack.dbtools.integrity.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dattack.dbtools.integrity.beans.Identifier;
import com.dattack.dbtools.integrity.exceptions.ConfigurationMistakeException;

/**
 * @author cvarela
 * @since 0.1
 */
final class SourceResultGroup implements Iterable<SourceResult> {

    private final List<SourceResult> sourceResults;

    public SourceResultGroup() {
        this.sourceResults = new ArrayList<SourceResult>();
    }

    public void add(final SourceResult item) {
        this.sourceResults.add(item);
    }

    public SourceResult get(final Identifier sourceName) {

        for (final SourceResult sourceResult : sourceResults) {
            if (sourceName.equals(sourceResult.getSourceAlias())) {
                return sourceResult;
            }
        }
        throw new ConfigurationMistakeException(String.format("Missing source '%s'", sourceName));
    }

    @Override
    public Iterator<SourceResult> iterator() {
        return sourceResults.iterator();
    }

    public void close() {
        for (final SourceResult sourceResult : sourceResults) {
            sourceResult.close();
        }
    }
}
