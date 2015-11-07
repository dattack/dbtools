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
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.integrity.beans.ConfigurationMailingListBean;
import com.dattack.dbtools.integrity.beans.ConfigurationSmtpBean;
import com.dattack.dbtools.integrity.beans.NotificationActionBeanVisitor;
import com.dattack.dbtools.integrity.beans.NotificationActionSendMailBean;
import com.dattack.ext.misc.ConfigurationUtil;

/**
 * @author cvarela
 * @since 0.1
 */
public class DefaultNotificationActionBeanVisitor implements NotificationActionBeanVisitor {

    private static final Logger log = LoggerFactory.getLogger(DefaultNotificationActionBeanVisitor.class);

    private final FlightRecorder flightRecorder;

    public DefaultNotificationActionBeanVisitor(final FlightRecorder flightRecorder) {
        this.flightRecorder = flightRecorder;
    }

    @Override
    public void visite(final NotificationActionSendMailBean action) {

        try {

            if (flightRecorder.getConfigurationBean() == null) {
                log.warn("Missing configuration. Please, check your configuration file.");
            } else {
                sendMail(flightRecorder.getConfigurationBean().getConfigurationSmtpBean(), action);
            }

        } catch (final EmailException e) {
            log.error(e.getMessage(), e);
        } catch (final AddressException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void sendMail(final ConfigurationSmtpBean config, final NotificationActionSendMailBean action)
            throws EmailException, AddressException {

        if (config == null) {
            log.warn("Missing SMTP configuration. Please, check your configuration file.");
            return;
        }

        BaseConfiguration baseConfiguration = new BaseConfiguration();
        baseConfiguration.setDelimiterParsingDisabled(true);
        baseConfiguration.setProperty(PropertyNames.TASK_NAME, flightRecorder.getTaskBean().getName());
        baseConfiguration.setProperty(PropertyNames.LOG, flightRecorder.getLog());

        for (ConfigurationMailingListBean item : config.getMailingLists()) {
            baseConfiguration.setProperty(item.getName(), item.getAddressList());
        }

        Email email = new SimpleEmail();
        email.setHostName(config.getHostname());
        email.setSmtpPort(config.getPort());
        email.setAuthenticator(new DefaultAuthenticator(config.getUsername(), config.getPassword()));
        email.setSSLOnConnect(config.isSslOnConnect());
        email.setStartTLSEnabled(config.isStartTLSEnabled());
        email.setFrom(config.getFrom());
        email.setTo(getInternetAddresses(action.getToAddressesList(), baseConfiguration));
        email.setSubject(ConfigurationUtil.interpolate(action.getSubject(), baseConfiguration));
        email.setMsg(ConfigurationUtil.interpolate(action.getMessage(), baseConfiguration));
        email.send();
    }

    private List<InternetAddress> getInternetAddresses(final List<String> addressesAsText,
            final BaseConfiguration baseConfiguration) throws AddressException {

        List<InternetAddress> list = new ArrayList<InternetAddress>();
        for (String to : addressesAsText) {
            String[] addresses = StringUtils.split(ConfigurationUtil.interpolate(to, baseConfiguration), " ,");
            for (String item : addresses) {
                list.add(new InternetAddress(item));
            }
        }

        return list;
    }
}