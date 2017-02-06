/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.config;

import com.google.common.collect.Sets;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
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
import java.nio.file.StandardOpenOption;
import java.util.*;

import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XmlPlatformConfigTest {
    
    public XmlPlatformConfigTest() {
    }

    @Test
    public void moduleConfigTest() throws IOException, XMLStreamException, SAXException, ParserConfigurationException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path cfgDir = Files.createDirectory(fileSystem.getPath("config"));
            Properties prop1 = new Properties();
            prop1.setProperty("s", "hello");
            prop1.setProperty("i", Integer.toString(3));
            prop1.setProperty("b", Boolean.FALSE.toString());
            prop1.setProperty("d", Double.toString(2.3));
            prop1.setProperty("c", ArrayList.class.getName());
            prop1.setProperty("sl1", "a,b,c");
            prop1.setProperty("sl2", "a:b:c");
            prop1.setProperty("e", StandardOpenOption.APPEND.name());
            prop1.setProperty("el", StandardOpenOption.APPEND + ","  + StandardOpenOption.CREATE);
            Path p = fileSystem.getPath("/tmp");
            Path p2 = fileSystem.getPath("/home");
            prop1.setProperty("p", p.toString());
            prop1.setProperty("pl", p.toString() + ":" + p2.toString());
            prop1.setProperty("pl2", p.toString() + "," + p2.toString());
            try (Writer w = Files.newBufferedWriter(cfgDir.resolve("mod.properties"), StandardCharsets.UTF_8)) {
                prop1.store(w, null);
            }
            PropertiesPlatformConfig propsConfig = new PropertiesPlatformConfig(cfgDir, fileSystem);
            ModuleConfig modConfig = propsConfig.getModuleConfig("mod");
            assertEquals("hello", modConfig.getStringProperty("s"));
            assertEquals("oups", modConfig.getStringProperty("s2", "oups"));
            try {
                modConfig.getStringProperty("s2");
                fail();
            } catch (Exception e) {
            }
            assertEquals(3, modConfig.getIntProperty("i"));
            try {
                modConfig.getIntProperty("i2");
                fail();
            } catch (Exception e) {
            }
            assertNull(modConfig.getOptionalIntProperty("i2"));
            assertFalse(modConfig.getOptionalIntegerProperty("i2").isPresent());
            assertEquals(4, modConfig.getIntProperty("i2", 4));
            assertFalse(modConfig.getBooleanProperty("b"));
            try {
                modConfig.getBooleanProperty("b2");
                fail();
            } catch (Exception e) {
            }
            assertNull(modConfig.getOptinalBooleanProperty("b2"));
            assertFalse(modConfig.getOptionalBooleanProperty("b2").isPresent());
            assertTrue(modConfig.getBooleanProperty("b2", true));
            assertEquals(2.3d, modConfig.getDoubleProperty("d"), 0d);
            try {
                modConfig.getDoubleProperty("d2");
                fail();
            } catch (Exception e) {
            }            
            assertEquals(4.5d, modConfig.getDoubleProperty("d2", 4.5d), 0d);
            assertEquals(ArrayList.class, modConfig.getClassProperty("c", List.class));
            assertEquals(Arrays.asList("a", "b", "c"), modConfig.getStringListProperty("sl1"));
            assertEquals(Arrays.asList("a", "b", "c"), modConfig.getStringListProperty("sl2"));
            try {
                modConfig.getStringListProperty("sl3");
                fail();
            } catch (Exception e) {
            }
            assertEquals(StandardOpenOption.APPEND, modConfig.getEnumProperty("e", StandardOpenOption.class));
            try {
                modConfig.getEnumProperty("e2", StandardOpenOption.class);
                fail();
            } catch (Exception e) {
            }
            assertEquals(EnumSet.of(StandardOpenOption.APPEND, StandardOpenOption.CREATE), modConfig.getEnumSetProperty("el", StandardOpenOption.class));
            try {
                modConfig.getEnumSetProperty("el2", StandardOpenOption.class);
                fail();
            } catch (Exception e) {
            }
            assertEquals(p, modConfig.getPathProperty("p"));
            try {
                modConfig.getPathProperty("p2");
                fail();
            } catch (Exception e) {
            }
            assertEquals(Arrays.asList(p, p2), modConfig.getPathListProperty("pl"));
            assertEquals(Arrays.asList(p, p2), modConfig.getPathListProperty("pl2"));
            try {
                modConfig.getPathListProperty("pl3");
                fail();
            } catch (Exception e) {
            }
            assertEquals(Sets.newHashSet("p", "b", "c", "s", "d", "e", "el", "pl2", "sl2", "sl1", "i", "pl"), modConfig.getPropertyNames());
            assertTrue(modConfig.hasProperty("p"));
        }
    }

    @Test
    public void properties2XmlConvertionTest() throws IOException, XMLStreamException, SAXException, ParserConfigurationException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path cfgDir = Files.createDirectory(fileSystem.getPath("config"));
            Properties prop1 = new Properties();
            prop1.setProperty("a", "hello");
            prop1.setProperty("b", "bye");
            try (Writer w = Files.newBufferedWriter(cfgDir.resolve("mod1.properties"), StandardCharsets.UTF_8)) {
                prop1.store(w, null);
            }
            Properties prop2 = new Properties();
            prop2.setProperty("c", "thanks");
            try (Writer w = Files.newBufferedWriter(cfgDir.resolve("mod2.properties"), StandardCharsets.UTF_8)) {
                prop2.store(w, null);
            }
            PropertiesPlatformConfig propsConfig = new PropertiesPlatformConfig(cfgDir, fileSystem);
            assertEquals("hello", propsConfig.getModuleConfig("mod1").getStringProperty("a"));
            assertEquals("bye", propsConfig.getModuleConfig("mod1").getStringProperty("b"));
            assertEquals("thanks", propsConfig.getModuleConfig("mod2").getStringProperty("c"));
            String xmlConfigName = "config";
            PropertiesPlatformConfig.writeXml(cfgDir, cfgDir.resolve(xmlConfigName + ".xml"));
            
            XmlPlatformConfig xmlConfig = new XmlPlatformConfig(cfgDir, xmlConfigName, fileSystem);
            assertEquals("hello", xmlConfig.getModuleConfig("mod1").getStringProperty("a"));
            assertEquals("bye", xmlConfig.getModuleConfig("mod1").getStringProperty("b"));
            assertEquals("thanks", xmlConfig.getModuleConfig("mod2").getStringProperty("c"));
        }
    }
    
}
