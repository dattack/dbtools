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

import java.io.FileNotFoundException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.script.ScriptException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.dattack.dbtools.GlobalConfiguration;
import com.dattack.dbtools.drules.beans.ConfigurationBean;
import com.dattack.dbtools.drules.beans.EventActionEvalJSBean;
import com.dattack.dbtools.drules.beans.Identifier;
import com.dattack.dbtools.drules.beans.DrulesBean;
import com.dattack.dbtools.drules.beans.JAXBParser;
import com.dattack.dbtools.drules.beans.JoinBean;
import com.dattack.dbtools.drules.beans.NotificationActionBean;
import com.dattack.dbtools.drules.beans.NotificationActionBeanVisitor;
import com.dattack.dbtools.drules.beans.NotificationBean;
import com.dattack.dbtools.drules.beans.NotificationEventBean;
import com.dattack.dbtools.drules.beans.RowCheckBean;
import com.dattack.dbtools.drules.beans.SourceBean;
import com.dattack.dbtools.drules.beans.TaskBean;
import com.dattack.dbtools.drules.exceptions.IdentifierNotFoundException;
import com.dattack.ext.concurrent.ThreadFactoryBuilder;
import com.dattack.ext.script.JavaScriptEngine;
import com.dattack.ext.util.CollectionUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public class DrulesEngine {

	private static final Logger log = LoggerFactory.getLogger(DrulesEngine.class);

	private ThreadFactory createThreadFactory() {
		return new ThreadFactoryBuilder() //
				.withNamePrefix("source") //
				.withUncaughtExceptionHandler(new UncaughtExceptionHandler() {

					@Override
					public void uncaughtException(final Thread t, final Throwable e) {
						log.error("Uncaught exception throwed by thread '{}': {}", t.getName(), e.getMessage());
					}
				}).build();
	}

	public void execute(final String integrityFilename, final Identifier taskId,
			final Configuration initialConfiguration) throws InterruptedException, ExecutionException {

		try {

			ThreadContext.getInstance().setInitialConfiguration(initialConfiguration);

			DrulesBean drulesBean = JAXBParser.parseIntegrityBean(integrityFilename);

			TaskBean taskBean = drulesBean.getTask(taskId);
			if (taskBean == null) {
				throw new IdentifierNotFoundException(TaskBean.class, taskId);
			}

			ConfigurationBean configurationBean = getConfigurationBean();
			
			log.info("SMTP hostname: " + configurationBean.getConfigurationSmtpBean().getHostname().toString());

			execute(taskBean, configurationBean);

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private ConfigurationBean getConfigurationBean() throws FileNotFoundException, JAXBException, SAXException,
			ParserConfigurationException, ConfigurationException {

		return JAXBParser.parseConfigurationBean(GlobalConfiguration
				.getProperty(GlobalConfiguration.DRULES_CONFIGURATION_FILE_KEY));
	}

	private void execute(final TaskBean taskBean, final ConfigurationBean configurationBean)
			throws InterruptedException, ExecutionException, ConfigurationException, FileNotFoundException,
			JAXBException, SAXException, ParserConfigurationException {

		log.info("Integrity task (Task ID: {}, Task name: {}): STARTED", taskBean.getId(), taskBean.getName());

		// start the flight recorder
		final FlightRecorder flightRecorder = new FlightRecorder(taskBean, configurationBean);

		executeJSEvals(taskBean);

		// executes the source' statements and retrieves the ResultSets to check
		SourceResultGroup sourceResultGroup = getSourceResultsList(taskBean.getSources());

		// execute checks
		executeRowChecks(taskBean, sourceResultGroup, flightRecorder);

		// execute global checks
		// TODO: execute global checks

		// process the flight recorder and execute notifications
		if (taskBean.getNotification() != null) {
			executeNotifications(taskBean.getNotification(), flightRecorder);
		} else {
			String notificationsFile = GlobalConfiguration
					.getProperty(GlobalConfiguration.DRULES_NOTIFICATIONS_FILE_KEY);
			if (StringUtils.isNotBlank(notificationsFile)) {
				NotificationBean notification = JAXBParser.parseNotificationBean(notificationsFile);
				executeNotifications(notification, flightRecorder);
			}
		}

		sourceResultGroup.close();

		log.info("Integrity task (Task ID: {}, Task name: {}): COMPLETED", taskBean.getId(), taskBean.getName());
	}

	private void executeJSEvals(final TaskBean taskBean) {

		if (CollectionUtils.isNotEmpty(taskBean.getEvalList())) {
			for (EventActionEvalJSBean item : taskBean.getEvalList()) {
				try {
					Object value = JavaScriptEngine.eval(item.getExpression());
					ThreadContext.getInstance().setProperty(item.getName(), value);
				} catch (ScriptException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void executeNotification(final NotificationEventBean bean, final FlightRecorder flightRecorder) {

		NotificationActionBeanVisitor visitor = new DefaultNotificationActionBeanVisitor(flightRecorder);
		for (final NotificationActionBean action : bean.getActionList()) {
			action.accept(visitor);
		}
	}

	private void executeNotifications(final NotificationBean notificationBean, final FlightRecorder flightRecorder) {

		if (flightRecorder.hasErrors() && notificationBean.getOnError() != null) {
			executeNotification(notificationBean.getOnError(), flightRecorder);

		} else if (flightRecorder.hasWarnings() && notificationBean.getOnWarning() != null) {
			executeNotification(notificationBean.getOnWarning(), flightRecorder);

		} else if (notificationBean.getOnSuccess() != null) {
			executeNotification(notificationBean.getOnSuccess(), flightRecorder);
		}
	}

	private void executeRowChecks(final TaskBean taskBean, final SourceResultGroup sourceResultList,
			final FlightRecorder flightRecorder) {

		for (RowCheckBean rowCheck : taskBean.getRowChecks()) {
			// TODO: clone the sourceResultList to execute more than one loop
			if (taskBean.getRowChecks().size() > 1) {
				throw new RuntimeException("TODO: clone the sourceResultList to execute more than one loop");
			}

			for (JoinBean joinBean : rowCheck.getJoinList()) {
				JoinStrategyFactory.getInstance().create(joinBean, sourceResultList).execute(flightRecorder);
			}
		}
	}

	private SourceResultGroup getSourceResultsList(final List<SourceBean> sourceList) throws InterruptedException,
			ExecutionException {

		ExecutorService executorService = Executors.newCachedThreadPool(createThreadFactory());

		List<Future<SourceResult>> futureList = new ArrayList<Future<SourceResult>>();

		for (final SourceBean sourceBean : sourceList) {
			futureList.add(executorService.submit(new SourceExecutor(sourceBean, ConfigurationUtils
					.cloneConfiguration(ThreadContext.getInstance().getConfiguration()))));
		}

		SourceResultGroup sourceResultList = new SourceResultGroup();

		for (Future<SourceResult> future : futureList) {
			sourceResultList.add(future.get());
		}
		executorService.shutdown();

		return sourceResultList;
	}
}
