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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author cvarela
 * @since 0.1
 */
public final class JAXBParser {
    
    private JAXBParser() {
        // static class
    }

    public static IntegrityBean parse(final String filename) throws JAXBException, SAXException, ParserConfigurationException, FileNotFoundException {

        File file = new File(filename);
        JAXBContext jaxbContext = JAXBContext.newInstance(IntegrityBean.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        
        SAXParserFactory spf = SAXParserFactory.newInstance();

        spf.setXIncludeAware(true);
        spf.setNamespaceAware(true);
        spf.setValidating(true); // Not required for JAXB/XInclude

        XMLReader xr = (XMLReader) spf.newSAXParser().getXMLReader();
        SAXSource source = new SAXSource(xr, new InputSource(new FileInputStream(file)));
        
        return (IntegrityBean) jaxbUnmarshaller.unmarshal(source);
    }
}
