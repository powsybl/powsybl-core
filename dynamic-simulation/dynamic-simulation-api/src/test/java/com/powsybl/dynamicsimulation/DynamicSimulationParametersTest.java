/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation;

import java.io.IOException;
import java.nio.file.FileSystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.auto.service.AutoService;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class DynamicSimulationParametersTest {

    private InMemoryPlatformConfig platformConfig;
    private FileSystem fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    private static void checkValues(DynamicSimulationParameters parameters, double startTime, double stopTime, String debugDir) {
        assertEquals(parameters.getStartTime(), startTime);
        assertEquals(parameters.getStopTime(), stopTime);
        assertEquals(parameters.getDebugDir(), debugDir);
    }

    @Test
    void testNoConfig() {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        DynamicSimulationParameters.load(parameters, platformConfig);
        checkValues(parameters, DynamicSimulationParameters.DEFAULT_START_TIME,
                DynamicSimulationParameters.DEFAULT_STOP_TIME,
                DynamicSimulationParameters.DEFAULT_DEBUG_DIR);
    }

    @Test
    void testConstructorStartTimeAsssertion() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new DynamicSimulationParameters(-1, 0));
        assertTrue(e.getMessage().contains("Start time should be zero or positive"));
    }

    @Test
    void testConstructorStopTimeAsssertion() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new DynamicSimulationParameters(0, 0));
        assertTrue(e.getMessage().contains("Stop time should be greater than start time"));
    }

    @Test
    void testStartTimeAsssertion() {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> parameters.setStartTime(-1));
        assertTrue(e.getMessage().contains("Start time should be zero or positive"));
    }

    @Test
    void testStopTimeAsssertion() {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> parameters.setStopTime(0));
        assertTrue(e.getMessage().contains("Stop time should be greater than start time"));
    }

    @Test
    void testToString() {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters(0, 40.55);
        assertEquals("{startTime=0.0, stopTime=40.55, debugDir=null}", parameters.toString());
    }

    @Test
    void checkConfig() {
        int startTime = 1;
        int stopTime = 100;
        String debugDir = "debugDir";

        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("dynamic-simulation-default-parameters");
        moduleConfig.setStringProperty("startTime", Integer.toString(startTime));
        moduleConfig.setStringProperty("stopTime", Integer.toString(stopTime));
        moduleConfig.setStringProperty("debugDir", debugDir);
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        DynamicSimulationParameters.load(parameters, platformConfig);
        checkValues(parameters, startTime, stopTime, debugDir);
    }

    @Test
    void checkSetters() {
        int startTime = 1;
        int stopTime = 100;
        String debugDir = "debugDir";

        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        DynamicSimulationParameters.load(parameters, platformConfig);
        parameters.setStartTime(startTime);
        parameters.setStopTime(stopTime);
        parameters.setDebugDir(debugDir);

        checkValues(parameters, startTime, stopTime, debugDir);
    }

    @Test
    void checkClone() {
        int startTime = 1;
        int stopTime = 100;
        String debugDir = "debugDir";
        DynamicSimulationParameters parameters = new DynamicSimulationParameters(startTime, stopTime, debugDir);
        DynamicSimulationParameters parametersCloned = parameters.copy();
        checkValues(parametersCloned, parameters.getStartTime(), parameters.getStopTime(), parameters.getDebugDir());
    }

    @Test
    void testExtensions() {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        DummyExtension dummyExtension = new DummyExtension();
        parameters.addExtension(DummyExtension.class, dummyExtension);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensions().contains(dummyExtension));
        assertInstanceOf(DummyExtension.class, parameters.getExtensionByName("dummyExtension"));
        assertInstanceOf(DummyExtension.class, parameters.getExtension(DummyExtension.class));
    }

    @Test
    void testNoExtensions() {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();

        assertEquals(0, parameters.getExtensions().size());
        assertFalse(parameters.getExtensions().contains(new DummyExtension()));
        assertFalse(parameters.getExtensionByName("dummyExtension") instanceof DummyExtension);
        assertNull(parameters.getExtension(DummyExtension.class));
    }

    @Test
    void testExtensionFromConfig() {
        DynamicSimulationParameters parameters = DynamicSimulationParameters.load(platformConfig);

        assertEquals(1, parameters.getExtensions().size());
        assertInstanceOf(DummyExtension.class, parameters.getExtensionByName("dummyExtension"));
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }

    private static class DummyExtension extends AbstractExtension<DynamicSimulationParameters> {

        @Override
        public String getName() {
            return "dummyExtension";
        }
    }

    @AutoService(DynamicSimulationParameters.ConfigLoader.class)
    public static class DummyLoader implements DynamicSimulationParameters.ConfigLoader<DummyExtension> {

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
            return "dynamic-simulation-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }
}
