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

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.GlobalConfiguration;
import com.dattack.dbtools.TemplateHelper;
import com.dattack.dbtools.drules.beans.ConfigurationMailingListBean;
import com.dattack.dbtools.drules.beans.ConfigurationSmtpBean;
import com.dattack.dbtools.drules.beans.NotificationActionBeanVisitor;
import com.dattack.dbtools.drules.beans.NotificationActionSendMailBean;
import com.dattack.dbtools.drules.exceptions.DrulesNestableRuntimeException;
import com.dattack.jtoolbox.commons.configuration.ConfigurationUtil;
import com.dattack.jtoolbox.commons.email.HtmlEmailBuilder;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author cvarela
 * @since 0.1
 */
public class DefaultNotificationActionBeanVisitor implements NotificationActionBeanVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNotificationActionBeanVisitor.class);

    private final FlightRecorder flightRecorder;

    private static Template createTemplate(final NotificationActionSendMailBean bean)
            throws ConfigurationException, IOException {

        if (StringUtils.isNotBlank(bean.getMessageTemplateText())) {
            return TemplateHelper.createTemplate(bean.getMessageTemplateText());
        }

        if (StringUtils.isNotBlank(bean.getMessageTemplateFile())) {
            return TemplateHelper.loadTemplate(bean.getMessageTemplateFile());
        }

        // use default template
        return TemplateHelper
                .loadTemplate(GlobalConfiguration.getProperty(GlobalConfiguration.DRULES_TEMPLATE_EMAIL_KEY));
    }

    private static String formatMessage(final NotificationActionSendMailBean action, final Configuration configuration)
            throws TemplateException, IOException, ConfigurationException {

        final Template template = createTemplate(action);

        final Map<Object, Object> dataModel = new HashMap<>();
        dataModel.putAll(ConfigurationConverter.getMap(configuration));

        final StringWriter outputWriter = new StringWriter();
        template.process(dataModel, outputWriter);
        return outputWriter.toString();
    }

    public DefaultNotificationActionBeanVisitor(final FlightRecorder flightRecorder) {
        this.flightRecorder = flightRecorder;
    }

    private void sendMail(final ConfigurationSmtpBean config, final NotificationActionSendMailBean action)
            throws EmailException, AddressException, ConfigurationException, TemplateException, IOException {

        if (config == null) {
            LOGGER.warn("Missing SMTP configuration. Please, check your configuration file.");
            return;
        }

        final CompositeConfiguration configuration = new CompositeConfiguration();
        configuration.addConfiguration(ThreadContext.getInstance().getConfiguration());
        configuration.setDelimiterParsingDisabled(true);
        configuration.setProperty(PropertyNames.TASK_NAME, flightRecorder.getTaskBean().getName());
        configuration.setProperty(PropertyNames.LOG, flightRecorder.getReport().toString());
        configuration.setProperty(PropertyNames.SUCCESS_ROWS, flightRecorder.getSuccessCounter());
        configuration.setProperty(PropertyNames.ERROR_ROWS, flightRecorder.getErrorCounter());
        configuration.setProperty(PropertyNames.WARNING_ROWS, flightRecorder.getWarningCounter());

        for (final ConfigurationMailingListBean item : config.getMailingLists()) {
            configuration.setProperty(item.getName(), item.getAddressList());
        }

        final HtmlEmailBuilder htmlEmailBuilder = new HtmlEmailBuilder() //
                .withHostName(ConfigurationUtil.interpolate(config.getHostname(), configuration)) //
                .withPort(config.getPort()) //
                .withUsername(ConfigurationUtil.interpolate(config.getUsername(), configuration)) //
                .withPassword(ConfigurationUtil.interpolate(config.getPassword(), configuration)) //
                .withFrom(ConfigurationUtil.interpolate(config.getFrom(), configuration)) //
                .withSubject(ConfigurationUtil.interpolate(action.getSubject(), configuration)) //
                .withMessage(formatMessage(action, configuration)) //
                .withSslOnConnect(config.isSslOnConnect()) //
                .withStartTlsEnabled(config.isStartTlsEnabled()); //

        for (final String to : action.getToAddressesList()) {
            final String[] addresses = StringUtils.split(ConfigurationUtil.interpolate(to, configuration), " ,");
            for (final String item : addresses) {
                htmlEmailBuilder.withToAddress(new InternetAddress(item));
            }
        }

        htmlEmailBuilder.build().send();
    }

    @Override
    public void visit(final NotificationActionSendMailBean action) {

        try {

            if (flightRecorder.getConfigurationBean() == null) {
                LOGGER.warn("Missing SMTP configuration. Please, check your configuration file.");
            } else {
                sendMail(flightRecorder.getConfigurationBean().getConfigurationSmtpBean(), action);
            }

        } catch (final EmailException | AddressException | ConfigurationException | TemplateException | IOException e) {
            throw new DrulesNestableRuntimeException(e);
        }
    }
}
