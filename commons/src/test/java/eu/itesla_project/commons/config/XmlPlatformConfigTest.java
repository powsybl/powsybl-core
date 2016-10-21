/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.config;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XmlPlatformConfigTest {
    
    public XmlPlatformConfigTest() {
    }

    @Test
    public void moduleConfigTest() throws IOException, XMLStreamException, SAXException, ParserConfigurationException {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        try (FileSystem fileSystem = ShrinkWrapFileSystems.newFileSystem(archive)) {
            Path cfgDir = fileSystem.getPath("config");
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
            Assert.assertTrue(modConfig.getStringProperty("s").equals("hello"));
            Assert.assertTrue(modConfig.getStringProperty("s2", "oups").equals("oups"));
            try {
                modConfig.getStringProperty("s2");
                Assert.fail();
            } catch (Exception e) {
            }
            Assert.assertTrue(modConfig.getIntProperty("i") == 3);
            try {
                modConfig.getIntProperty("i2");
                Assert.fail();
            } catch (Exception e) {
            }
            Assert.assertTrue(modConfig.getOptionalIntProperty("i2") == null);
            Assert.assertTrue(modConfig.getIntProperty("i2", 4) == 4);
            Assert.assertFalse(modConfig.getBooleanProperty("b"));
            try {
                modConfig.getBooleanProperty("b2");
                Assert.fail();
            } catch (Exception e) {
            }
            Assert.assertTrue(modConfig.getBooleanProperty("b2", true));
            Assert.assertTrue(modConfig.getDoubleProperty("d") == 2.3);
            try {
                modConfig.getDoubleProperty("d2");
                Assert.fail();
            } catch (Exception e) {
            }            
            Assert.assertTrue(modConfig.getDoubleProperty("d2", 4.5) == 4.5);
            Assert.assertTrue(modConfig.getClassProperty("c", List.class) == ArrayList.class);
            Assert.assertTrue(modConfig.getStringListProperty("sl1").equals(Arrays.asList("a", "b", "c")));
            Assert.assertTrue(modConfig.getStringListProperty("sl2").equals(Arrays.asList("a", "b", "c")));
            try {
                modConfig.getStringListProperty("sl3");
                Assert.fail();
            } catch (Exception e) {
            }
            Assert.assertTrue(modConfig.getEnumProperty("e", StandardOpenOption.class) == StandardOpenOption.APPEND);
            try {
                modConfig.getEnumProperty("e2", StandardOpenOption.class);
                Assert.fail();
            } catch (Exception e) {
            }
            Assert.assertTrue(modConfig.getEnumSetProperty("el", StandardOpenOption.class).equals(EnumSet.of(StandardOpenOption.APPEND, StandardOpenOption.CREATE)));
            try {
                modConfig.getEnumSetProperty("el2", StandardOpenOption.class);
                Assert.fail();
            } catch (Exception e) {
            }
            Assert.assertTrue(modConfig.getPathProperty("p").equals(p));
            try {
                modConfig.getPathProperty("p2");
                Assert.fail();
            } catch (Exception e) {
            }
            Assert.assertTrue(modConfig.getPathListProperty("pl").equals(Arrays.asList(p, p2)));             
            Assert.assertTrue(modConfig.getPathListProperty("pl2").equals(Arrays.asList(p, p2)));             
            try {
                modConfig.getPathListProperty("pl3");
                Assert.fail();
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void properties2XmlConvertionTest() throws IOException, XMLStreamException, SAXException, ParserConfigurationException {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        try (FileSystem fileSystem = ShrinkWrapFileSystems.newFileSystem(archive)) {
            Path cfgDir = fileSystem.getPath("config");
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
            Assert.assertTrue(propsConfig.getModuleConfig("mod1").getStringProperty("a").equals("hello"));
            Assert.assertTrue(propsConfig.getModuleConfig("mod1").getStringProperty("b").equals("bye"));
            Assert.assertTrue(propsConfig.getModuleConfig("mod2").getStringProperty("c").equals("thanks"));
            String xmlConfigName = "config";
            PropertiesPlatformConfig.writeXml(cfgDir, cfgDir.resolve(xmlConfigName + ".xml"));
            
            XmlPlatformConfig xmlConfig = new XmlPlatformConfig(cfgDir, xmlConfigName, fileSystem);
            Assert.assertTrue(xmlConfig.getModuleConfig("mod1").getStringProperty("a").equals("hello"));
            Assert.assertTrue(xmlConfig.getModuleConfig("mod1").getStringProperty("b").equals("bye"));
            Assert.assertTrue(xmlConfig.getModuleConfig("mod2").getStringProperty("c").equals("thanks"));            
        }
    }
    
}
