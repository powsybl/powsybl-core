/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XmlPlatformConfig extends InMemoryPlatformConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlPlatformConfig.class);

    @Deprecated
    public XmlPlatformConfig(Path configDir, String configName, FileSystem fs)
            throws IOException, SAXException, ParserConfigurationException {
        this(fs, configDir, getDefaultCacheDir(fs), configName);
    }

    public XmlPlatformConfig(FileSystem fileSystem, Path configDir, Path cacheDir, String configName)
            throws IOException, SAXException, ParserConfigurationException {
        super(fileSystem, configDir, cacheDir);

        Path file = configDir.resolve(configName + ".xml");
        if (Files.exists(file)) {
            LOGGER.info("Platform configuration defined by XML file {}", file);
            try (InputStream is = Files.newInputStream(file)) {
                handleInputStream(fileSystem, is);
            }
        } else {
            LOGGER.info("Platform configuration XML file {} not found", file);
        }
    }

    public XmlPlatformConfig(FileSystem fileSystem, Path configDir, Path cacheDir, InputStream configInputStream)
            throws IOException, SAXException, ParserConfigurationException {
        super(fileSystem, configDir, cacheDir);

        if (Objects.nonNull(configInputStream)) {
            LOGGER.info("Platform configuration defined by InputStream");
            handleInputStream(fileSystem, configInputStream);
        } else {
            LOGGER.info("Platform configuration InputStream is null");
        }
    }

    private void handleInputStream(FileSystem fileSystem, InputStream is) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);
        Element root = doc.getDocumentElement();
        root.normalize();
        NodeList moduleNodes = root.getChildNodes();
        for (int i = 0; i < moduleNodes.getLength(); i++) {
            Node moduleNode = moduleNodes.item(i);
            if (moduleNode.getNodeType() == Node.ELEMENT_NODE) {
                String moduleName = moduleNode.getLocalName();
                Map<Object, Object> properties = new HashMap<>();
                NodeList propertyNodes = moduleNode.getChildNodes();
                for (int j = 0; j < propertyNodes.getLength(); j++) {
                    Node propertyNode = propertyNodes.item(j);
                    if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
                        String propertyName = propertyNode.getLocalName();
                        Node child = propertyNode.getFirstChild();
                        String propertyValue = child != null ? child.getTextContent() : "";
                        properties.put(propertyName, propertyValue);
                    }
                }
                ((InMemoryModuleConfigContainer) container).getConfigs().put(moduleName, new MapModuleConfig(properties, fileSystem));
            }
        }
    }

}
