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

import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.dattack.dbtools.drules.exceptions.DrulesNestableException;
import com.dattack.dbtools.drules.exceptions.DrulesParserException;
import com.dattack.ext.io.FilesystemUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public final class DrulesParser {

    private static Object parse(final String filename, final Class<?> clazz) throws DrulesNestableException {

        final SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setXIncludeAware(true);
        spf.setNamespaceAware(true);
        spf.setValidating(true);

        try (final FileInputStream fileInputStream = new FileInputStream(FilesystemUtils.locate(filename))) {
            final InputSource input = new InputSource(fileInputStream);
            final XMLReader xmlreader = spf.newSAXParser().getXMLReader();
            final Source source = new SAXSource(xmlreader, input);

            final JAXBContext ctx = JAXBContext.newInstance(clazz);
            final Unmarshaller unmarshaller = ctx.createUnmarshaller();
            return unmarshaller.unmarshal(source);
        } catch (JAXBException | IOException | SAXException | ParserConfigurationException e) {
            throw new DrulesParserException(e);
        }
    }

    public static ConfigurationBean parseConfigurationBean(final String filename) throws DrulesNestableException {

        if (filename == null) {
            throw new IllegalArgumentException(
                    "The 'ConfigurationBean XML' filename can't be null. Check your configuration");
        }
        return (ConfigurationBean) parse(filename, ConfigurationBean.class);
    }

    public static DrulesBean parseIntegrityBean(final String filename) throws DrulesNestableException {

        if (filename == null) {
            throw new IllegalArgumentException("The 'DrulesBean XML' filename can't be null. Check your configuration");
        }
        return (DrulesBean) parse(filename, DrulesBean.class);
    }

    public static NotificationBean parseNotificationBean(final String filename) throws DrulesNestableException {

        if (filename == null) {
            throw new IllegalArgumentException(
                    "The 'NotificationBean XML' filename can't be null. Check your configuration");
        }
        return (NotificationBean) parse(filename, NotificationBean.class);
    }

    private DrulesParser() {
        // static class
    }
}
