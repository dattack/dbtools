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
package com.dattack.ext.mail;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import com.dattack.ext.util.Assert;
import com.dattack.ext.util.CollectionUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public class HtmlEmailBuilder {

    private String hostname;
    private int port;
    private String username;
    private String password;
    private Boolean sslOnConnect;
    private Boolean startTlsEnabled;
    private String from;
    private String subject;
    private String message;
    private final List<InternetAddress> toList;
    private final List<InternetAddress> ccList;
    private final List<InternetAddress> bccList;

    public HtmlEmailBuilder() {
        toList = new ArrayList<InternetAddress>();
        ccList = new ArrayList<InternetAddress>();
        bccList = new ArrayList<InternetAddress>();
    }

    public HtmlEmailBuilder withHostName(final String hostname) {
        this.hostname = hostname;
        return this;
    }

    public HtmlEmailBuilder withPort(final Integer port) {
        this.port = port;
        return this;
    }

    public HtmlEmailBuilder withUsername(final String username) {
        this.username = username;
        return this;
    }

    public HtmlEmailBuilder withPassword(final String password) {
        this.password = password;
        return this;
    }

    public HtmlEmailBuilder withSSLOnConnect(final Boolean sslOnConnect) {
        this.sslOnConnect = sslOnConnect;
        return this;
    }

    public HtmlEmailBuilder withStartTlsEnabled(final Boolean startTlsEnabled) {
        this.startTlsEnabled = startTlsEnabled;
        return this;
    }

    public HtmlEmailBuilder withFrom(final String from) {
        this.from = from;
        return this;
    }

    public HtmlEmailBuilder withSubject(final String subject) {
        this.subject = subject;
        return this;
    }

    public HtmlEmailBuilder withMessage(final String message) {
        this.message = message;
        return this;
    }

    public HtmlEmailBuilder withToAddress(final InternetAddress address) {
        this.toList.add(address);
        return this;
    }

    public HtmlEmailBuilder withCcAddress(final InternetAddress address) {
        this.ccList.add(address);
        return this;
    }

    public HtmlEmailBuilder withBccAddress(final InternetAddress address) {
        this.bccList.add(address);
        return this;
    }

    public HtmlEmail build() throws EmailException {

        Assert.isNotBlank(hostname, String.format("Invalid SMTP server (hostname: '%s')", hostname));
        Assert.isNotBlank(from, String.format("Invalid email address (FROM: '%s'", from));

        HtmlEmail email = new HtmlEmail();
        email.setHostName(hostname);
        email.setFrom(from);
        email.setSubject(subject);

        if (StringUtils.isNotBlank(message)) {
            email.setMsg(message);
        }

        if (port > 0) {
            email.setSmtpPort(port);
        }

        if (StringUtils.isNotBlank(username)) {
            email.setAuthenticator(new DefaultAuthenticator(username, password));
        }

        if (sslOnConnect != null) {
            email.setSSLOnConnect(sslOnConnect);
        }

        if (startTlsEnabled != null) {
            email.setStartTLSEnabled(startTlsEnabled);
        }

        if (CollectionUtils.isNotEmpty(toList)) {
            email.setTo(toList);
        }

        if (CollectionUtils.isNotEmpty(ccList)) {
            email.setCc(ccList);
        }

        if (CollectionUtils.isNotEmpty(bccList)) {
            email.setBcc(bccList);
        }
        return email;
    }
}
