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
 * Defines the bean mapped with the {@link XmlTokens#ELEMENT_MAILING_LISTS} elements. 
 * 
 * @author cvarela
 * @since 0.1
 */
public final class ConfigurationMailingListBean implements Serializable {

    private static final long serialVersionUID = -3222591368575902241L;

    /** The name of the list. */
    @XmlAttribute(name = XmlTokens.ATTRIBUTE_NAME)
    private String name;

    /** comma-separated values. */
    @XmlValue
    private String addressList;

    public String getName() {
        return name;
    }

    public String getAddressList() {
        return addressList;
    }
}
