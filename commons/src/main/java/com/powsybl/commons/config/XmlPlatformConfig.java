/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.exceptions.UncheckedParserConfigurationException;
import com.powsybl.commons.exceptions.UncheckedSaxException;
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
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XmlPlatformConfig extends InMemoryPlatformConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlPlatformConfig.class);

    protected XmlPlatformConfig(FileSystem fileSystem, Path configDir, Path cacheDir) {
        super(fileSystem, configDir, cacheDir);
    }

    public static Optional<PlatformConfig> create(FileSystem fileSystem, Path configDir, Path cacheDir, String configName) {
        Path file = configDir.resolve(configName + ".xml");
        if (Files.exists(file)) {
            LOGGER.info("Platform configuration defined by XML file {}", file);

            XmlPlatformConfig platformConfig = new XmlPlatformConfig(fileSystem, configDir, cacheDir);

            try (InputStream is = Files.newInputStream(file)) {
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
                        ((InMemoryModuleConfigContainer) platformConfig.container).getConfigs()
                                .put(moduleName, new MapModuleConfig(properties, fileSystem));
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (SAXException e) {
                throw new UncheckedSaxException(e);
            } catch (ParserConfigurationException e) {
                throw new UncheckedParserConfigurationException(e);
            }

            return Optional.of(platformConfig);
        } else {
            LOGGER.info("Platform configuration XML file {} not found", file);

            return Optional.empty();
        }
    }
}
