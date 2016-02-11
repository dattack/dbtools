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
package com.dattack.dbtools.drules.engine;

import java.util.List;

import com.dattack.dbtools.drules.beans.Identifier;
import com.dattack.dbtools.drules.beans.JoinBean;
import com.dattack.dbtools.drules.beans.JoinBean.JoinType;
import com.dattack.dbtools.drules.exceptions.IdentifierNotFoundException;

/**
 * @author cvarela
 * @since 0.1
 */
public final class JoinStrategyFactory {

    private static final transient JoinStrategyFactory INSTANCE = new JoinStrategyFactory();

    public static JoinStrategyFactory getInstance() {
        return INSTANCE;
    }

    private JoinStrategyFactory() {
        // singleton
    }

    /**
     * Creates the join strategy defined by the {@link JoinBean#getType()}.
     *
     * @param joinBean
     *            the JoinBean
     * @param sourceResultList
     *            the SourceResultGroup
     * @return the join strategy
     * @throws IllegalArgumentException
     *             if the join type is unknown
     * @throws IdentifierNotFoundException 
     */
    public JoinStrategy create(final JoinBean joinBean, final SourceResultGroup sourceResultList)
            throws IllegalArgumentException, IdentifierNotFoundException {

        if (joinBean.getType().equals(JoinType.INNER)) {
            return createInnerJoinStrategy(joinBean, sourceResultList);
        }
        throw new IllegalArgumentException(String.format("Unknown JOIN type: {}", joinBean.getType()));
    }

    private InnerJoinStrategy createInnerJoinStrategy(final JoinBean joinBean, final SourceResultGroup sourceResultList)
            throws IllegalArgumentException, IdentifierNotFoundException {

        final List<Identifier> sourceNames = joinBean.getSources();
        if (sourceNames.size() != 2) {
            // TODO: throw other exception or support join over two or more sources
            throw new IllegalArgumentException(
                    String.format("Unsupported join over %d sources [name: %s]", sourceNames.size(), sourceNames));
        }

        final SourceResultGroup joinSourceResultList = new SourceResultGroup();
        for (final Identifier sourceId : sourceNames) {
            joinSourceResultList.add(sourceResultList.get(sourceId));
        }
        return new InnerJoinStrategy(joinSourceResultList, joinBean);
    }
}
