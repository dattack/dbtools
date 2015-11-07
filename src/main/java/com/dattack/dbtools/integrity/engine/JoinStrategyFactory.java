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

import java.util.List;

import com.dattack.dbtools.integrity.beans.Identifier;
import com.dattack.dbtools.integrity.beans.JoinBean;

/**
 * @author cvarela
 * @since 0.1
 */
public final class JoinStrategyFactory {

    private static transient final JoinStrategyFactory INSTANCE = new JoinStrategyFactory();

    public static JoinStrategyFactory getInstance() {
        return INSTANCE;
    }

    private JoinStrategyFactory() {
        // singleton
    }

    public JoinStrategy create(final JoinBean joinBean, final SourceResultGroup sourceResultList)
            throws IllegalArgumentException {

        switch (joinBean.getType()) {

        case INNER:
            return createInnerJoinStrategy(joinBean, sourceResultList);

        default:
            throw new IllegalArgumentException(String.format("Unknown JOIN type: {}", joinBean.getType()));
        }
    }

    private InnerJoinStrategy createInnerJoinStrategy(final JoinBean joinBean,
            final SourceResultGroup sourceResultList) {

        List<Identifier> sourceNames = joinBean.getSources();
        if (sourceNames.size() != 2) {
            // TODO: throw other exception or support join over two or more sources
            throw new RuntimeException(
                    String.format("Unsupported join over %d sources [name: %s]", sourceNames.size(), sourceNames));
        }

        final SourceResultGroup joinSourceResultList = new SourceResultGroup();
        for (final Identifier sourceId : sourceNames) {
            joinSourceResultList.add(sourceResultList.get(sourceId));
        }
        return new InnerJoinStrategy(joinSourceResultList, joinBean);
    }
}
