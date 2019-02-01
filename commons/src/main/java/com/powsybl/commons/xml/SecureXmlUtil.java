/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;

/**
 * XML factories to prevent XML eXternal Entity injection
 * https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#JAXP_DocumentBuilderFactory.2C_SAXParserFactory_and_DOM4J
 *
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public final class SecureXmlUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtil.class);

    private SecureXmlUtil() {
    }

    public static DocumentBuilderFactory createDocumentBuilderFactory() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        setFeature(dbf, "http://apache.org/xml/features/disallow-doctype-decl", true);
        setFeature(dbf, "http://xml.org/sax/features/external-general-entities", false);
        setFeature(dbf, "http://xml.org/sax/features/external-parameter-entities", false);
        setFeature(dbf, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);

        return dbf;
    }

    private static void setFeature(DocumentBuilderFactory dbf, String feature, boolean value) {
        try {
            dbf.setFeature(feature, value);
        } catch (ParserConfigurationException e) {
            LOGGER.warn("The feature '{}' is not supported.", feature);
        }
    }

    public static SchemaFactory createSchemaFactory() {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        setProperty(factory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
        setProperty(factory, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        return factory;
    }

    private static void setProperty(SchemaFactory factory, String property, Object value) {
        try {
            factory.setProperty(property, value);
        } catch (SAXException e) {
            LOGGER.warn("The property '{}' is not supported.", property);
        }
    }

}
