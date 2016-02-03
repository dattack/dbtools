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
package com.dattack.dbtools.drules.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author cvarela
 * @since 0.1
 */
public final class RowCheckBean implements Serializable {

    private static final long serialVersionUID = 5675788263421406766L;

    public enum CONSTRAINT {
        SORTED
    }

    private final CONSTRAINT constraint;
    
    @XmlElement(name = XmlTokens.ELEMENT_JOIN)
    private final List<JoinBean> joinList;

    public RowCheckBean() {
        this.constraint = CONSTRAINT.SORTED;
        this.joinList = new ArrayList<JoinBean>();
    }

    public CONSTRAINT getConstraint() {
        return constraint;
    }

    public List<JoinBean> getJoinList() {
        return joinList;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RowCheckBean [constraint=").append(constraint).append(", joinList=").append(joinList)
                .append("]");
        return builder.toString();
    }
}
