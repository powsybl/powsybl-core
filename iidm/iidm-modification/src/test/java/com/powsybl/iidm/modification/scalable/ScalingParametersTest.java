/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class ScalingParametersTest {

    InMemoryPlatformConfig platformConfig;
    FileSystem fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void emptyConstructorTest() {
        ScalingParameters parameters = new ScalingParameters();
        assertEquals(ScalingParameters.DEFAULT_SCALING_CONVENTION, parameters.getScalingConvention());
        assertEquals(ScalingParameters.DEFAULT_CONSTANT_POWER_FACTOR, parameters.isConstantPowerFactor());
        assertEquals(ScalingParameters.DEFAULT_ITERATIVE, parameters.isIterative());
        assertEquals(ScalingParameters.DEFAULT_RECONNECT, parameters.isReconnect());
    }

    @Test
    void fullConstructorTest() {
        ScalingParameters parameters = new ScalingParameters(Scalable.ScalingConvention.LOAD,
                true, true, true);
        assertEquals(Scalable.ScalingConvention.LOAD, parameters.getScalingConvention());
        assertTrue(parameters.isConstantPowerFactor());
        assertTrue(parameters.isIterative());
        assertTrue(parameters.isReconnect());
    }

    @Test
    void settersTest() {
        ScalingParameters parameters = new ScalingParameters()
                .setScalingConvention(Scalable.ScalingConvention.LOAD)
                .setConstantPowerFactor(true)
                .setIterative(true)
                .setReconnect(true);
        assertEquals(Scalable.ScalingConvention.LOAD, parameters.getScalingConvention());
        assertTrue(parameters.isConstantPowerFactor());
        assertTrue(parameters.isIterative());
        assertTrue(parameters.isReconnect());
    }

    @Test
    void loadNoConfigTest() {
        ScalingParameters parameters = ScalingParameters.load(platformConfig);
        assertEquals(ScalingParameters.DEFAULT_SCALING_CONVENTION, parameters.getScalingConvention());
        assertEquals(ScalingParameters.DEFAULT_CONSTANT_POWER_FACTOR, parameters.isConstantPowerFactor());
        assertEquals(ScalingParameters.DEFAULT_ITERATIVE, parameters.isIterative());
        assertEquals(ScalingParameters.DEFAULT_RECONNECT, parameters.isReconnect());
    }

    @Test
    void loadConfigTest() {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("scaling-default-parameters");
        moduleConfig.setStringProperty("scalingConvention", "LOAD");
        moduleConfig.setStringProperty("constantPowerFactor", "true");
        moduleConfig.setStringProperty("iterative", "true");
        moduleConfig.setStringProperty("reconnect", "true");

        ScalingParameters parameters = ScalingParameters.load(platformConfig);
        assertEquals(Scalable.ScalingConvention.LOAD, parameters.getScalingConvention());
        assertTrue(parameters.isConstantPowerFactor());
        assertTrue(parameters.isIterative());
        assertTrue(parameters.isReconnect());
    }
}
