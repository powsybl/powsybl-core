/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.google.auto.service.AutoService;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.YamlModuleConfigRepository;
import com.powsybl.commons.extensions.AbstractExtension;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class ShortCircuitParametersTest {

    private FileSystem fileSystem;

    private InMemoryPlatformConfig platformConfig;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void testExtensions() {
        ShortCircuitParameters parameters = new ShortCircuitParameters();
        DummyExtension dummyExtension = new DummyExtension();
        parameters.addExtension(DummyExtension.class, dummyExtension);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensions().contains(dummyExtension));
        assertNotNull(parameters.getExtensionByName("dummyExtension"));
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }

    @Test
    public void testNoExtensions() {
        ShortCircuitParameters parameters = new ShortCircuitParameters();

        assertEquals(0, parameters.getExtensions().size());
        assertFalse(parameters.getExtensions().contains(new DummyExtension()));
        assertNull(parameters.getExtensionByName("dummyExtension"));
        assertNull(parameters.getExtension(DummyExtension.class));
    }

    @Test
    public void testExtensionFromConfig() {
        ShortCircuitParameters parameters = ShortCircuitParameters.load(platformConfig);

        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtensionByName("dummyExtension"));
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }

    @Test
    public void testSubTransStudy() {
        ShortCircuitParameters parameters = ShortCircuitParameters.load(platformConfig);
        assertFalse(parameters.isSubTransStudy());

        parameters.setSubTransStudy(true);
        assertTrue(parameters.isSubTransStudy());
    }

    @Test
    public void testStudyType() {
        ShortCircuitParameters parameters = ShortCircuitParameters.load(platformConfig);
        assertEquals(ShortCircuitConstants.StudyType.SYSTEMATIC_STUDY, parameters.getStudyType());

        assertNull(parameters.getEquipment());
    }

    @Test
    public void testWithFeederResult() {
        ShortCircuitParameters parameters = ShortCircuitParameters.load(platformConfig);
        assertTrue(parameters.isWithFeederResult());

        parameters.setWithFeederResult(false);
        assertFalse(parameters.isWithFeederResult());
    }

    @Test
    public void testConfigLoader() throws IOException {
        Path cfgDir = Files.createDirectory(fileSystem.getPath("config"));
        Path cfgFile = cfgDir.resolve("config.yml");

        Files.copy(getClass().getResourceAsStream("/config.yml"), cfgFile);
        PlatformConfig platformConfig = new PlatformConfig(new YamlModuleConfigRepository(cfgFile), cfgDir);
        ShortCircuitParameters parameters = ShortCircuitParameters.load(platformConfig);
        assertTrue(parameters.isSubTransStudy());
        assertEquals(ShortCircuitConstants.StudyType.SELECTIVE_STUDY, parameters.getStudyType());
        assertNotNull(parameters.getEquipment());
        assertEquals("id", parameters.getEquipment());
        assertFalse(parameters.isWithFeederResult());
        assertEquals(ShortCircuitConstants.SelectiveStudyType.BUS_STUDY, parameters.getSelectiveStudyType());
        assertEquals(0, parameters.getFaultResistance(), 0);
        assertEquals(0, parameters.getFaultReactance(), 0);
        assertEquals(5, parameters.getVoltageDropThreshold(), 0);
    }

    private static class DummyExtension extends AbstractExtension<ShortCircuitParameters> {

        @Override
        public String getName() {
            return "dummyExtension";
        }
    }

    @AutoService(ShortCircuitParameters.ConfigLoader.class)
    public static class DummyLoader implements ShortCircuitParameters.ConfigLoader<DummyExtension> {

        @Override
        public DummyExtension load(PlatformConfig platformConfig) {
            return new DummyExtension();
        }

        @Override
        public String getExtensionName() {
            return "dummyExtension";
        }

        @Override
        public String getCategoryName() {
            return "short-circuit-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }
}
