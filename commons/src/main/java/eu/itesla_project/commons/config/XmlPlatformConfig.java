/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XmlPlatformConfig extends InMemoryPlatformConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlPlatformConfig.class);

    public XmlPlatformConfig(Path configDir, String configName, FileSystem fs) throws IOException, SAXException, ParserConfigurationException {
        super(fs);
        Path file = configDir.resolve(configName + ".xml");
        if (Files.exists(file)) {
            LOGGER.info("Platform configuration defined by XML file {}", file);
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
                        configs.put(moduleName, new MapModuleConfig(properties, fs));
                    }
                }
            }
        } else {
            LOGGER.info("Platform configuration XML file {} not found", file);
        }
    }
    
}
