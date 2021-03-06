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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.script.ScriptException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.dbtools.GlobalConfiguration;
import com.dattack.dbtools.drules.beans.ConfigurationBean;
import com.dattack.dbtools.drules.beans.DrulesBean;
import com.dattack.dbtools.drules.beans.DrulesParser;
import com.dattack.dbtools.drules.beans.EventActionEvalJsBean;
import com.dattack.dbtools.drules.beans.Identifier;
import com.dattack.dbtools.drules.beans.JoinBean;
import com.dattack.dbtools.drules.beans.NotificationActionBean;
import com.dattack.dbtools.drules.beans.NotificationActionBeanVisitor;
import com.dattack.dbtools.drules.beans.NotificationBean;
import com.dattack.dbtools.drules.beans.NotificationEventBean;
import com.dattack.dbtools.drules.beans.RowCheckBean;
import com.dattack.dbtools.drules.beans.SourceBean;
import com.dattack.dbtools.drules.beans.TaskBean;
import com.dattack.dbtools.drules.exceptions.DrulesNestableException;
import com.dattack.dbtools.drules.exceptions.IdentifierNotFoundException;
import com.dattack.jtoolbox.concurrent.SimpleThreadFactory.ThreadFactoryBuilder;
import com.dattack.jtoolbox.script.JavaScriptEngine;
import com.dattack.jtoolbox.util.CollectionUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public class DrulesEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrulesEngine.class);

    private final String drulesFilename;

    private DrulesBean drulesBean;

    private static ThreadFactory createThreadFactory() {
        return new ThreadFactoryBuilder() //
                .withThreadNamePrefix("source") //
                .withUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                    @Override
                    public void uncaughtException(final Thread thread, final Throwable throwable) {
                        LOGGER.error("Uncaught exception throwed by thread '{}': {}", thread.getName(),
                                throwable.getMessage());
                    }
                }).build();
    }

    private static void execute(final TaskBean taskBean, final ConfigurationBean configurationBean)
            throws ConfigurationException, DrulesNestableException {

        LOGGER.info("Integrity task (Task ID: {}, Task name: {}): STARTED", taskBean.getId(), taskBean.getName());

        // start the flight recorder
        final FlightRecorder flightRecorder = new FlightRecorder(taskBean, configurationBean);

        executeJsEvals(taskBean);

        // executes the source' statements and retrieves the ResultSets to check
        final SourceResultGroup sourceResultGroup = getSourceResultsList(taskBean.getSources());

        try {
            // execute checks
            executeRowChecks(taskBean, sourceResultGroup, flightRecorder);
        } finally {

            // execute global checks
            // TODO: execute global checks

            // process the flight recorder and execute notifications
            if (taskBean.getNotification() != null) {
                executeNotifications(taskBean.getNotification(), flightRecorder);
            } else {
                final String notificationsFile = GlobalConfiguration
                        .getProperty(GlobalConfiguration.DRULES_NOTIFICATIONS_FILE_KEY);
                if (StringUtils.isNotBlank(notificationsFile)) {
                    final NotificationBean notification = DrulesParser.parseNotificationBean(notificationsFile);
                    executeNotifications(notification, flightRecorder);
                }
            }

            sourceResultGroup.close();

            LOGGER.info("Integrity task (Task ID: {}, Task name: {}): COMPLETED", taskBean.getId(), taskBean.getName());
        }
    }

    private static void executeJsEvals(final TaskBean taskBean) throws DrulesNestableException {

        if (CollectionUtils.isNotEmpty(taskBean.getEvalList())) {
            for (final EventActionEvalJsBean item : taskBean.getEvalList()) {
                try {
                    final Object value = JavaScriptEngine.eval(item.getExpression());
                    ThreadContext.getInstance().setProperty(item.getName(), value);
                } catch (final ScriptException e) {
                    throw new DrulesNestableException(e);
                }
            }
        }
    }

    private static void executeNotification(final NotificationEventBean bean, final FlightRecorder flightRecorder) {

        final NotificationActionBeanVisitor visitor = new DefaultNotificationActionBeanVisitor(flightRecorder);
        for (final NotificationActionBean action : bean.getActionList()) {
            action.accept(visitor);
        }
    }

    private static void executeNotifications(final NotificationBean notificationBean,
            final FlightRecorder flightRecorder) {

        if (flightRecorder.hasErrors() && notificationBean.getOnError() != null) {
            executeNotification(notificationBean.getOnError(), flightRecorder);

        } else if (flightRecorder.hasWarnings() && notificationBean.getOnWarning() != null) {
            executeNotification(notificationBean.getOnWarning(), flightRecorder);

        } else if (notificationBean.getOnSuccess() != null) {
            executeNotification(notificationBean.getOnSuccess(), flightRecorder);
        }
    }

    private static void executeRowChecks(final TaskBean taskBean, final SourceResultGroup sourceResultList,
            final FlightRecorder flightRecorder) throws IllegalArgumentException, DrulesNestableException {

        for (final RowCheckBean rowCheck : taskBean.getRowChecks()) {
            // TODO: clone the sourceResultList to execute more than one loop
            if (taskBean.getRowChecks().size() > 1) {
                throw new IllegalArgumentException("TODO: clone the sourceResultList to execute more than one loop");
            }

            for (final JoinBean joinBean : rowCheck.getJoinList()) {
                JoinStrategyFactory.getInstance().create(joinBean, sourceResultList).execute(flightRecorder);
            }
        }
    }

    private static ConfigurationBean getConfigurationBean() throws ConfigurationException, DrulesNestableException {

        return DrulesParser.parseConfigurationBean(
                GlobalConfiguration.getProperty(GlobalConfiguration.DRULES_CONFIGURATION_FILE_KEY));
    }

    private static SourceResultGroup getSourceResultsList(final List<SourceBean> sourceList)
            throws DrulesNestableException {

        final ExecutorService executorService = Executors.newCachedThreadPool(createThreadFactory());

        final List<Future<SourceResult>> futureList = new ArrayList<>();

        for (final SourceBean sourceBean : sourceList) {
            futureList.add(executorService.submit(new SourceExecutor(sourceBean,
                    ConfigurationUtils.cloneConfiguration(ThreadContext.getInstance().getConfiguration()))));
        }

        final SourceResultGroup sourceResultList = new SourceResultGroup();

        for (final Future<SourceResult> future : futureList) {
            try {
                sourceResultList.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new DrulesNestableException(e);
            }
        }
        executorService.shutdown();

        return sourceResultList;
    }

    public DrulesEngine(final String drulesFilename, final Configuration initialConfiguration) {
        this.drulesFilename = drulesFilename;
        ThreadContext.getInstance().setInitialConfiguration(initialConfiguration);
        ThreadContext.getInstance().setProperty(PropertyNames.EXECUTION_ID, System.currentTimeMillis());
    }

    /**
     * Executes the task defined in the file whose identifier matches the indicated.
     *
     * @param taskId
     *            the task identifier
     * @param initialConfiguration
     *            properties required to perform the task
     */
    public void execute(final Identifier taskId) throws DrulesNestableException {

        try {
            final TaskBean taskBean = getDrulesBean().getTask(taskId);
            if (taskBean == null) {
                throw new IdentifierNotFoundException(TaskBean.class, taskId);
            }

            final ConfigurationBean configurationBean = getConfigurationBean();

            LOGGER.info("SMTP hostname: " + configurationBean.getConfigurationSmtpBean().getHostname().toString());

            execute(taskBean, configurationBean);

        } catch (final ConfigurationException e) {
            throw new DrulesNestableException(e);
        }
    }

    private synchronized DrulesBean getDrulesBean() throws DrulesNestableException {
        if (drulesBean == null) {
            drulesBean = DrulesParser.parseIntegrityBean(drulesFilename);
        }
        return drulesBean;
    }

    public List<Identifier> listTasks() throws DrulesNestableException {
        final List<TaskBean> taskBeanList = getDrulesBean().getTaskBeanList();
        final List<Identifier> identifierList = new ArrayList<>(taskBeanList.size());
        for (final TaskBean taskBean : taskBeanList) {
            identifierList.add(taskBean.getId());
        }
        return identifierList;
    }
}
