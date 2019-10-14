/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamic.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.FileSystem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.auto.service.AutoService;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtension;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class DynamicSimulationParametersTest {

    InMemoryPlatformConfig platformConfig;
    FileSystem fileSystem;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    private void checkValues(DynamicSimulationParameters parameters, int startTime,
                             int stopTime) {
        assertEquals(parameters.getStartTime(), startTime);
        assertEquals(parameters.getStopTime(), stopTime);
    }

    @Test
    public void testNoConfig() {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        DynamicSimulationParameters.load(parameters, platformConfig);
        checkValues(parameters, DynamicSimulationParameters.DEFAULT_START_TIME,
                DynamicSimulationParameters.DEFAULT_STOP_TIME);
    }

    @Test
    public void checkConfig() throws Exception {
        int startTime = 1;
        int stopTime = 100;

        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("dynamic-simulation-default-parameters");
        moduleConfig.setStringProperty("startTime", Integer.toString(startTime));
        moduleConfig.setStringProperty("stopTime", Integer.toString(stopTime));
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        DynamicSimulationParameters.load(parameters, platformConfig);
        checkValues(parameters, startTime, stopTime);
    }

    @Test
    public void checkSetters() throws Exception {
        int startTime = 1;
        int stopTime = 100;

        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        DynamicSimulationParameters.load(parameters, platformConfig);
        parameters.setStartTime(startTime);
        parameters.setStopTime(stopTime);

        checkValues(parameters, startTime, stopTime);
    }

    @Test
    public void checkClone() throws Exception {
        int startTime = 1;
        int stopTime = 100;
        DynamicSimulationParameters parameters = new DynamicSimulationParameters(startTime, stopTime);
        DynamicSimulationParameters parametersCloned = parameters.copy();
        checkValues(parametersCloned, parameters.getStartTime(), parameters.getStopTime());
    }

    @Test
    public void testExtensions() {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        DummyExtension dummyExtension = new DummyExtension();
        parameters.addExtension(DummyExtension.class, dummyExtension);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensions().contains(dummyExtension));
        assertTrue(parameters.getExtensionByName("dummyExtension") instanceof DummyExtension);
        assertTrue(parameters.getExtension(DummyExtension.class) instanceof DummyExtension);
    }

    @Test
    public void testNoExtensions() {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();

        assertEquals(0, parameters.getExtensions().size());
        assertFalse(parameters.getExtensions().contains(new DummyExtension()));
        assertFalse(parameters.getExtensionByName("dummyExtension") instanceof DummyExtension);
        assertFalse(parameters.getExtension(DummyExtension.class) instanceof DummyExtension);
    }

    @Test
    public void testExtensionFromConfig() {
        DynamicSimulationParameters parameters = DynamicSimulationParameters.load(platformConfig);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensionByName("dummyExtension") instanceof DummyExtension);
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
