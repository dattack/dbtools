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
package com.dattack.dbtools.integrity.beans;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author cvarela
 * @since 0.1
 */
public final class SqlQueryBean implements Serializable {

    private static final long serialVersionUID = -8673720034464586206L;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE_ID, required = false)
    private Identifier id;

    @XmlValue
    private String sql;
    
    public Identifier getId() {
        return id;
    }

    public String getSql() {
        return sql;
    }
}
