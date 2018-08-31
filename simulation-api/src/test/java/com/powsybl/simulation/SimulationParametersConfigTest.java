/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
     * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class SimulationParametersConfigTest {

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

    private MapModuleConfig buildSimpleParamConfig() throws IOException {
        MapModuleConfig paramConfig = platformConfig.createModuleConfig("simulation-parameters");
        paramConfig.setStringProperty("preFaultSimulationStopInstant", "0.1");
        paramConfig.setStringProperty("faultEventInstant", "0.2");
        paramConfig.setStringProperty("generatorFaultShortCircuitDuration", "0.3");
        paramConfig.setStringProperty("postFaultSimulationStopInstant", "0.8");
        paramConfig.setStringProperty("postFaultSimulationStopInstant", "0.8");
        return paramConfig;
    }

    @Test
    public void paramConfigTest() throws IOException {
        MapModuleConfig paramConfig = buildSimpleParamConfig();
        paramConfig.setStringProperty("branchSideOneFaultShortCircuitDuration", "0.35");
        paramConfig.setStringProperty("branchSideTwoFaultShortCircuitDuration", "0.75");
        SimulationParameters parameters = SimulationParameters.load(platformConfig);
        assertEquals(0.1d, parameters.getPreFaultSimulationStopInstant(), 0.0d);
        assertEquals(0.2d, parameters.getFaultEventInstant(), 0.0d);
        assertEquals(0.3d, parameters.getGeneratorFaultShortCircuitDuration(), 0.0d);
        assertEquals(0.8d, parameters.getPostFaultSimulationStopInstant(), 0.0d);
        assertEquals(0.35d, parameters.getBranchSideOneFaultShortCircuitDuration(), 0.0d);
        assertEquals(0.75d, parameters.getBranchSideTwoFaultShortCircuitDuration(), 0.0d);
    }

    @Test
    public void paramConfigDurationMissingTest() throws IOException {
        MapModuleConfig paramConfig = buildSimpleParamConfig();
        try {
            SimulationParameters parameters = SimulationParameters.load(platformConfig);
            fail("expected missing parameters exception");
        } catch (PowsyblException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void paramConfigMissingSideTwoDurationTest() throws IOException {
        MapModuleConfig paramConfig = buildSimpleParamConfig();
        paramConfig.setStringProperty("branchSideOneFaultShortCircuitDuration", "0.35");
        try {
            SimulationParameters parameters = SimulationParameters.load(platformConfig);
            fail("expected missing parameters exception");
        } catch (PowsyblException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void paramConfigMissingSideOneDurationTest() throws IOException {
        MapModuleConfig paramConfig = buildSimpleParamConfig();
        paramConfig.setStringProperty("branchSideTwoFaultShortCircuitDuration", "0.35");
        try {
            SimulationParameters parameters = SimulationParameters.load(platformConfig);
            fail("expected missing parameters exception");
        } catch (PowsyblException e) {
            assertNotNull(e.getMessage());
        }

    }

    @Test
    public void paramConfigCompatibilityBranchDurationParameter() throws IOException {
        MapModuleConfig paramConfig = buildSimpleParamConfig();
        paramConfig.setStringProperty("branchFaultShortCircuitDuration", "0.40");
        SimulationParameters parameters = SimulationParameters.load(platformConfig);
        assertEquals(0.40d, parameters.getBranchSideOneFaultShortCircuitDuration(), 0.0d);
        assertEquals(0.40d, parameters.getBranchSideTwoFaultShortCircuitDuration(), 0.0d);
        assertEquals(0.40d, parameters.getBranchFaultShortCircuitDuration(), 0.0d);
    }

    @Test
    public void paramConfigMixedCompatibilityBranchDurationParameter() throws IOException {
        MapModuleConfig paramConfig = buildSimpleParamConfig();
        paramConfig.setStringProperty("branchSideOneFaultShortCircuitDuration", "0.35");
        paramConfig.setStringProperty("branchSideTwoFaultShortCircuitDuration", "0.75");
        // branchFaultShortCircuitDuration is read only if the two properties branchSideOneFaultShortCircuitDuration and branchSideTwoFaultShortCircuitDuration are not set
        paramConfig.setStringProperty("branchFaultShortCircuitDuration", "0.40");
        SimulationParameters parameters = SimulationParameters.load(platformConfig);
        assertEquals(0.35d, parameters.getBranchSideOneFaultShortCircuitDuration(), 0.0d);
        assertEquals(0.75d, parameters.getBranchSideTwoFaultShortCircuitDuration(), 0.0d);
        assertEquals(0.35d, parameters.getBranchFaultShortCircuitDuration(), 0.0d);
    }


    @Test
    public void paramConfigMixed2CompatibilityBranchDurationParameter() throws IOException {
        MapModuleConfig paramConfig = buildSimpleParamConfig();
        paramConfig.setStringProperty("branchSideTwoFaultShortCircuitDuration", "0.75");
        paramConfig.setStringProperty("branchFaultShortCircuitDuration", "0.40");
        try {
            SimulationParameters parameters = SimulationParameters.load(platformConfig);
            fail("expected missing parameters exception");
        } catch (PowsyblException e) {
            assertNotNull(e.getMessage());
        }
    }

}
