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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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

import com.dattack.ext.io.FilesystemUtils;
import com.dattack.ext.io.IOUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public final class JAXBParser {

	private JAXBParser() {
		// static class
	}

	public static IntegrityBean parseIntegrityBean(final String filename)
			throws JAXBException, SAXException, ParserConfigurationException, FileNotFoundException {
		return (IntegrityBean) parse(filename, IntegrityBean.class);
	}

	public static ConfigurationBean parseConfigurationBean(final String filename)
			throws JAXBException, SAXException, ParserConfigurationException, FileNotFoundException {
		return (ConfigurationBean) parse(filename, ConfigurationBean.class);
	}

	private static Object parse(final String filename, final Class<?> clazz)
			throws JAXBException, SAXException, ParserConfigurationException, FileNotFoundException {

		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setXIncludeAware(true);
		spf.setNamespaceAware(true);
		spf.setValidating(true);

		XMLReader xmlreader = spf.newSAXParser().getXMLReader();
//		xmlreader.setEntityResolver(new EntityResolver() {
//			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
//				// TODO: Check if systemId really references root.dtd
//				return new InputSource(JAXBParser.class.getResourceAsStream("integrity-1.0.dtd"));
//			}
//		});

		InputSource input = new InputSource(new FileInputStream(FilesystemUtils.locate(filename)));
		Source source = new SAXSource(xmlreader, input);

		JAXBContext ctx = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = ctx.createUnmarshaller();
		return unmarshaller.unmarshal(source);
	}
}
