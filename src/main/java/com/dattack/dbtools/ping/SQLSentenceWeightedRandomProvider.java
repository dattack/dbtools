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
package com.dattack.dbtools.ping;

import java.util.List;
import java.util.Random;

/**
 * Selects a query from the provided list using a weighted randon selection algorithm.
 * 
 * @author cvarela
 * @since 0.1
 */
public class SQLSentenceWeightedRandomProvider implements SQLSentenceProvider {

    private List<SQLSentence> sentenceList;
    private final Random randomGenerator;
    private float[] cumulativeWeight;

    public SQLSentenceWeightedRandomProvider() {
        this.randomGenerator = new Random();
    }
    
    public void setSentences(final List<SQLSentence> sqlList) {
        this.sentenceList = sqlList;
        this.cumulativeWeight = cdf();
    }

    // the cumulative density function
    private float[] cdf() {

        float totalWeight = 0;
        for (SQLSentence sentence : sentenceList) {
            totalWeight += sentence.getWeight();
        }

        float[] cdfValues = new float[sentenceList.size()];
        for (int i = 0; i < cdfValues.length; i++) {
            float normWeight = norm(sentenceList.get(i).getWeight(), totalWeight);
            if (i == 0) {
                cdfValues[i] = normWeight;
            } else {
                cdfValues[i] = cdfValues[i - 1] + normWeight;
            }
        }

        return cdfValues;
    }

    private float norm(final float weight, final float sumWeight) {
        return weight / sumWeight;
    }

    @Override
    public SQLSentence nextSQL() {
        
        if (sentenceList == null) {
            throw new IllegalArgumentException("Unable to ");
        }

        float cw = randomGenerator.nextFloat();
        for (int i = 0; i < cumulativeWeight.length; i++) {
            if (cumulativeWeight[i] > cw) {
                return sentenceList.get(i);
            }
        }
        return sentenceList.get(sentenceList.size() - 1);
    }
}
