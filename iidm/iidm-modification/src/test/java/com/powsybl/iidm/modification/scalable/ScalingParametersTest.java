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
import java.util.Set;

import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.*;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.ScalingType.DELTA_P;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.ScalingType.TARGET_P;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
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
        assertEquals(ScalingParameters.DEFAULT_PRIORITY, parameters.getPriority());
        assertEquals(ScalingParameters.DEFAULT_RECONNECT, parameters.isReconnect());
        assertEquals(ScalingParameters.DEFAULT_ALLOWS_GENERATOR_OUT_OF_ACTIVE_POWER_LIMITS, parameters.isAllowsGeneratorOutOfActivePowerLimits());
    }

    @Test
    void fullConstructorTest() {
        ScalingParameters parameters = new ScalingParameters(Scalable.ScalingConvention.LOAD,
                true, true, RESPECT_OF_VOLUME_ASKED, true);
        assertEquals(Scalable.ScalingConvention.LOAD, parameters.getScalingConvention());
        assertTrue(parameters.isConstantPowerFactor());
        assertEquals(RESPECT_OF_VOLUME_ASKED, parameters.getPriority());
        assertTrue(parameters.isReconnect());
        assertTrue(parameters.isAllowsGeneratorOutOfActivePowerLimits());
    }

    @Test
    void fullSecondConstructorTest() {
        ScalingParameters parameters = new ScalingParameters(Scalable.ScalingConvention.LOAD,
            true, true, RESPECT_OF_VOLUME_ASKED, true,
            DELTA_P);
        assertEquals(Scalable.ScalingConvention.LOAD, parameters.getScalingConvention());
        assertTrue(parameters.isConstantPowerFactor());
        assertEquals(RESPECT_OF_VOLUME_ASKED, parameters.getPriority());
        assertTrue(parameters.isReconnect());
        assertTrue(parameters.isAllowsGeneratorOutOfActivePowerLimits());
        assertEquals(DELTA_P, parameters.getScalingType());
    }

    @Test
    void settersTest() {
        ScalingParameters parameters = new ScalingParameters()
                .setScalingConvention(Scalable.ScalingConvention.LOAD)
                .setConstantPowerFactor(true)
                .setPriority(RESPECT_OF_VOLUME_ASKED)
                .setReconnect(true)
                .setAllowsGeneratorOutOfActivePowerLimits(true)
                .setScalingType(TARGET_P);
        assertEquals(Scalable.ScalingConvention.LOAD, parameters.getScalingConvention());
        assertTrue(parameters.isConstantPowerFactor());
        assertEquals(RESPECT_OF_VOLUME_ASKED, parameters.getPriority());
        assertTrue(parameters.isReconnect());
        assertTrue(parameters.isAllowsGeneratorOutOfActivePowerLimits());
        assertEquals(TARGET_P, parameters.getScalingType());
    }

    @Test
    void loadNoConfigTest() {
        ScalingParameters parameters = ScalingParameters.load(platformConfig);
        assertEquals(ScalingParameters.DEFAULT_SCALING_CONVENTION, parameters.getScalingConvention());
        assertEquals(ScalingParameters.DEFAULT_CONSTANT_POWER_FACTOR, parameters.isConstantPowerFactor());
        assertEquals(ScalingParameters.DEFAULT_PRIORITY, parameters.getPriority());
        assertEquals(ScalingParameters.DEFAULT_RECONNECT, parameters.isReconnect());
        assertEquals(ScalingParameters.DEFAULT_ALLOWS_GENERATOR_OUT_OF_ACTIVE_POWER_LIMITS, parameters.isAllowsGeneratorOutOfActivePowerLimits());
    }

    @Test
    void loadConfigTest() {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("scaling-default-parameters");
        moduleConfig.setStringProperty("scalingConvention", "LOAD");
        moduleConfig.setStringProperty("constantPowerFactor", "true");
        moduleConfig.setStringProperty("priority", "RESPECT_OF_VOLUME_ASKED");
        moduleConfig.setStringProperty("reconnect", "true");
        moduleConfig.setStringProperty("allowsGeneratorOutOfActivePowerLimits", "true");
        moduleConfig.setStringProperty("ignoredInjectionIds", "id1,id2");

        ScalingParameters parameters = ScalingParameters.load(platformConfig);
        assertEquals(Scalable.ScalingConvention.LOAD, parameters.getScalingConvention());
        assertTrue(parameters.isConstantPowerFactor());
        assertEquals(RESPECT_OF_VOLUME_ASKED, parameters.getPriority());
        assertTrue(parameters.isReconnect());
        assertTrue(parameters.isAllowsGeneratorOutOfActivePowerLimits());
        assertEquals(Set.of("id1", "id2"), parameters.getIgnoredInjectionIds());
    }

    /**
     * This test will have to be deleted when the depreciated methods are deleted.
     */
    @Test
    void depreciatedMethodsTest() {
        ScalingParameters parameters = new ScalingParameters()
            .setIterative(true);
        assertTrue(parameters.isIterative());

        parameters = new ScalingParameters()
            .setIterative(false);
        assertFalse(parameters.isIterative());

        parameters = new ScalingParameters(Scalable.ScalingConvention.LOAD,
            true, true, true, true);
        assertEquals(Scalable.ScalingConvention.LOAD, parameters.getScalingConvention());
        assertTrue(parameters.isConstantPowerFactor());
        assertEquals(RESPECT_OF_VOLUME_ASKED, parameters.getPriority());
        assertTrue(parameters.isReconnect());
        assertTrue(parameters.isAllowsGeneratorOutOfActivePowerLimits());

        parameters = new ScalingParameters(Scalable.ScalingConvention.LOAD,
            true, true, false, true);
        assertEquals(Scalable.ScalingConvention.LOAD, parameters.getScalingConvention());
        assertTrue(parameters.isConstantPowerFactor());
        assertEquals(ONESHOT, parameters.getPriority());
        assertTrue(parameters.isReconnect());
        assertTrue(parameters.isAllowsGeneratorOutOfActivePowerLimits());
    }
}
