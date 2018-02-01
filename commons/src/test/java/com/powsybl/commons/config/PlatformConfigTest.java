/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 *
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class PlatformConfigTest {

    public PlatformConfigTest() {

    }

    @Test
    public void testCustomPlatformConfig() throws IOException, XMLStreamException, SAXException, ParserConfigurationException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path configDir = Files.createDirectory(fileSystem.getPath("config"));
            Properties prop1 = new Properties();
            prop1.setProperty("prop1", "prop1Test");
            prop1.setProperty("prop2", "prop2Test");
            try (Writer w = Files.newBufferedWriter(configDir.resolve("module1.properties"), StandardCharsets.UTF_8)) {
                prop1.store(w, null);
            }
            Properties prop2 = new Properties();
            prop2.setProperty("prop3", "prop3Test");
            try (Writer w = Files.newBufferedWriter(configDir.resolve("module2.properties"), StandardCharsets.UTF_8)) {
                prop2.store(w, null);
            }
            String xmlConfigName = "config";
            Path configFile = configDir.resolve(xmlConfigName + ".xml");
            PropertiesPlatformConfig.writeXml(configDir, configDir.resolve(xmlConfigName + ".xml"));

            PlatformConfig customConfig = PlatformConfig.customConfig(fileSystem, configFile);

            ModuleConfig configMod1 = customConfig.getModuleConfig("module1");
            assertEquals("prop1Test", configMod1.getStringProperty("prop1"));
            assertEquals("prop2Test", configMod1.getStringProperty("prop2"));
            ModuleConfig configMod2 = customConfig.getModuleConfig("module2");
            assertEquals("prop3Test", configMod2.getStringProperty("prop3"));
        }
    }

    @Test(expected = PowsyblException.class)
    public void testPowsyblException() throws IOException, XMLStreamException, SAXException, ParserConfigurationException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path configDir = Files.createDirectory(fileSystem.getPath("config"));
            Properties prop1 = new Properties();
            prop1.setProperty("prop1", "prop1Test");
            prop1.setProperty("prop2", "prop2Test");
            try (Writer w = Files.newBufferedWriter(configDir.resolve("module1.properties"), StandardCharsets.UTF_8)) {
                prop1.store(w, null);
            }
            Properties prop2 = new Properties();
            prop2.setProperty("prop3", "prop3Test");
            try (Writer w = Files.newBufferedWriter(configDir.resolve("module2.properties"), StandardCharsets.UTF_8)) {
                prop2.store(w, null);
            }
            String xmlConfigName = "config";
            Path configFile = configDir.resolve(xmlConfigName + ".xml");
            PropertiesPlatformConfig.writeXml(configDir, configDir.resolve(xmlConfigName + ".xml"));

            PlatformConfig customConfig = PlatformConfig.customConfig(fileSystem, configFile);

            ModuleConfig configMod1 = customConfig.getModuleConfig("module11");
        }
    }
}
