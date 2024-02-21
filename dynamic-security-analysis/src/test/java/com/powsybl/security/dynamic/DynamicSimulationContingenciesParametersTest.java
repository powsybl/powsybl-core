/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import com.google.auto.service.AutoService;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
class DynamicSimulationContingenciesParametersTest {

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

    private static void checkValues(DynamicSimulationContingenciesParameters parameters, int startTime,
                                    int stopTime, int startContingenciesStart) {
        assertEquals(parameters.getStartTime(), startTime);
        assertEquals(parameters.getStopTime(), stopTime);
        assertEquals(parameters.getContingenciesStartTime(), startContingenciesStart);
    }

    @Test
    void testNoConfig() {
        DynamicSimulationContingenciesParameters parameters = new DynamicSimulationContingenciesParameters();
        DynamicSimulationContingenciesParameters.load(parameters, platformConfig);
        checkValues(parameters, DynamicSimulationContingenciesParameters.DEFAULT_START_TIME,
                DynamicSimulationContingenciesParameters.DEFAULT_STOP_TIME,
                DynamicSimulationContingenciesParameters.DEFAULT_CONTINGENCIES_START_TIME);
    }

    @Test
    void testConstructorStartTimeAssertion() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new DynamicSimulationContingenciesParameters(-1, 0, 0));
        assertEquals("Start time should be zero or positive", e.getMessage());
    }

    @Test
    void testConstructorStopTimeAssertion() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new DynamicSimulationContingenciesParameters(0, 0, 0));
        assertEquals("Stop time should be greater than start time", e.getMessage());
    }

    @Test
    void testConstructorContingenciesStartTimeAssertion() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new DynamicSimulationContingenciesParameters(0, 10, 12));
        assertEquals("Contingencies start time should be between simulation start and stop time", e.getMessage());
    }

    @Test
    void testStartTimeAssertion() {
        DynamicSimulationContingenciesParameters parameters = new DynamicSimulationContingenciesParameters();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> parameters.setStartTime(-1));
        assertEquals("Start time should be zero or positive", e.getMessage());
    }

    @Test
    void testContingenciesStartTimeAssertion() {
        DynamicSimulationContingenciesParameters parameters = new DynamicSimulationContingenciesParameters();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> parameters.setContingenciesStartTime(-1));
        assertEquals("Contingencies start time should be between simulation start and stop time", e.getMessage());
    }

    @Test
    void testStartTimeContingenciesAssertion() {
        DynamicSimulationContingenciesParameters parameters = new DynamicSimulationContingenciesParameters();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> parameters.setStartTime(6));
        assertEquals("Start time should be lesser than contingencies start time", e.getMessage());
    }

    @Test
    void testStopTimeContingenciesAssertion() {
        DynamicSimulationContingenciesParameters parameters = new DynamicSimulationContingenciesParameters();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> parameters.setStopTime(4));
        assertEquals("Stop time should be greater than contingencies start time", e.getMessage());
    }

    @Test
    void testToString() {
        DynamicSimulationContingenciesParameters parameters = new DynamicSimulationContingenciesParameters(0, 1000, 200);
        assertEquals("{startTime=0, stopTime=1000, contingenciesStartTime=200}", parameters.toString());
    }

    @Test
    void checkConfig() {
        int startTime = 1;
        int stopTime = 100;
        int contingenciesStartTime = 50;

        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("dynamic-simulation-contingencies-default-parameters");
        moduleConfig.setStringProperty("startTime", Integer.toString(startTime));
        moduleConfig.setStringProperty("stopTime", Integer.toString(stopTime));
        moduleConfig.setStringProperty("contingenciesStartTime", Integer.toString(contingenciesStartTime));
        DynamicSimulationContingenciesParameters parameters = new DynamicSimulationContingenciesParameters();
        DynamicSimulationContingenciesParameters.load(parameters, platformConfig);
        checkValues(parameters, startTime, stopTime, contingenciesStartTime);
    }

    @Test
    void checkSetters() {
        int startTime = 1;
        int stopTime = 100;
        int contingenciesStartTime = 50;

        DynamicSimulationContingenciesParameters parameters = new DynamicSimulationContingenciesParameters();
        DynamicSimulationContingenciesParameters.load(parameters, platformConfig);
        parameters.setStartTime(startTime);
        parameters.setStopTime(stopTime);
        parameters.setContingenciesStartTime(contingenciesStartTime);
        checkValues(parameters, startTime, stopTime, contingenciesStartTime);
    }

    @Test
    void checkClone() {
        int startTime = 1;
        int stopTime = 100;
        int contingenciesStartTime = 50;

        DynamicSimulationContingenciesParameters parameters = new DynamicSimulationContingenciesParameters(startTime, stopTime, contingenciesStartTime);
        DynamicSimulationContingenciesParameters parametersCloned = parameters.copy();
        checkValues(parametersCloned, parameters.getStartTime(), parameters.getStopTime(), parameters.getContingenciesStartTime());
    }

    @Test
    void testExtensions() {
        DynamicSimulationContingenciesParameters parameters = new DynamicSimulationContingenciesParameters();
        DummyExtension dummyExtension = new DummyExtension();
        parameters.addExtension(DummyExtension.class, dummyExtension);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensions().contains(dummyExtension));
        assertTrue(parameters.getExtensionByName("dummyExtension") instanceof DummyExtension);
        assertTrue(parameters.getExtension(DummyExtension.class) instanceof DummyExtension);
    }

    @Test
    void testNoExtensions() {
        DynamicSimulationContingenciesParameters parameters = new DynamicSimulationContingenciesParameters();

        assertEquals(0, parameters.getExtensions().size());
        assertFalse(parameters.getExtensions().contains(new DummyExtension()));
        assertFalse(parameters.getExtensionByName("dummyExtension") instanceof DummyExtension);
        assertFalse(parameters.getExtension(DummyExtension.class) instanceof DummyExtension);
    }

    @Test
    void testExtensionFromConfig() {
        DynamicSimulationContingenciesParameters parameters = DynamicSimulationContingenciesParameters.load(platformConfig);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensionByName("dummyExtension") instanceof DummyExtension);
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }

    private static class DummyExtension extends AbstractExtension<DynamicSimulationContingenciesParameters> {

        @Override
        public String getName() {
            return "dummyExtension";
        }
    }

    @AutoService(DynamicSimulationContingenciesParameters.ConfigLoader.class)
    public static class DummyLoader implements DynamicSimulationContingenciesParameters.ConfigLoader<DummyExtension> {

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
            return "dynamic-simulation-contingencies-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }
}
