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
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * Defines the bean mapped with the {@code XmlTokens.ELEMENT_SMTP} element that contains the SMTP parameters.
 * 
 * @author cvarela
 * @since 0.1
 */
public final class ConfigurationSmtpBean implements Serializable {

    private static final long serialVersionUID = -5155803149034336444L;

    @XmlElement(name = XmlTokens.ELEMENT_HOSTNAME, required = true)
    private String hostname;

    @XmlElement(name = XmlTokens.ELEMENT_PORT, required = true)
    private int port;

    @XmlElement(name = XmlTokens.ELEMENT_USERNAME)
    private String username;
    @XmlElement(name = XmlTokens.ELEMENT_PASSWORD)
    private String password;

    @XmlElement(name = XmlTokens.ELEMENT_SSL_ON_CONNECT)
    private Boolean sslOnConnect;

    @XmlElement(name = XmlTokens.ELEMENT_START_TLS_ENABLED)
    private Boolean startTlsEnabled;

    @XmlElement(name = XmlTokens.ELEMENT_FROM)
    private String from;

    @XmlElement(name = XmlTokens.ELEMENT_MAILING_LISTS)
    private List<ConfigurationMailingListBean> mailingLists;

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Boolean isSslOnConnect() {
        return sslOnConnect;
    }

    public Boolean isStartTlsEnabled() {
        return startTlsEnabled;
    }

    public String getFrom() {
        return from;
    }

    public List<ConfigurationMailingListBean> getMailingLists() {
        return mailingLists;
    }
}
