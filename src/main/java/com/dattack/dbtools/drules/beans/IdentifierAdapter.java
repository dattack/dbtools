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

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.dattack.dbtools.drules.beans.Identifier.IdentifierBuilder;

/**
 * @author cvarela
 * @since 0.1
 */
public final class IdentifierAdapter extends XmlAdapter<String, Identifier> {

    @Override
    public Identifier unmarshal(final String v) throws Exception {

        if(v == null) {
            return null;
        }
        return new IdentifierBuilder().withValue(v).build();
    }

    @Override
    public String marshal(final Identifier v) throws Exception {

        if (v == null) {
            return null;
        }
        return v.getValue();
    }
}
