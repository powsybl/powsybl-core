/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryModuleConfigRepository;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.StackedModuleConfigRepository;
import com.powsybl.iidm.network.Country;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.util.EnumSet;

import static com.powsybl.loadflow.LoadFlowParameters.load;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
class LoadFlowParametersConfigTest {

    InMemoryPlatformConfig platformConfig;

    FileSystem fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @Test
    void testConfigValuesAreAppliedAndAbsentKeysFallBackToDefaults() {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-default-parameters");
        moduleConfig.setStringProperty("voltageInitMode", "PREVIOUS_VALUES");
        moduleConfig.setStringProperty("dc", "true");

        LoadFlowParameters parameters = new LoadFlowParameters();
        // a non-default value set before loading must be reset to the hardcoded default if the key is absent
        parameters.setUseReactiveLimits(false);
        load(parameters, platformConfig);

        assertEquals(LoadFlowParameters.VoltageInitMode.PREVIOUS_VALUES, parameters.getVoltageInitMode());
        assertTrue(parameters.isDc());
        assertEquals(LoadFlowParameters.DEFAULT_USE_REACTIVE_LIMITS, parameters.isUseReactiveLimits());
        assertEquals(LoadFlowParameters.DEFAULT_BALANCE_TYPE, parameters.getBalanceType());
        assertEquals(LoadFlowParameters.DEFAULT_COMPONENT_MODE, parameters.getComponentMode());
        assertEquals(LoadFlowParameters.DEFAULT_DC_POWER_FACTOR, parameters.getDcPowerFactor());
        assertNull(parameters.getDebugDir());
    }

    /**
     * Sets every one of the 17 properties to a value different from its default.
     */
    private static LoadFlowParameters nonDefaultParameters() {
        LoadFlowParameters parameters = new LoadFlowParameters()
                .setVoltageInitMode(LoadFlowParameters.VoltageInitMode.DC_VALUES)
                .setTransformerVoltageControlOn(!LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON)
                .setUseReactiveLimits(!LoadFlowParameters.DEFAULT_USE_REACTIVE_LIMITS)
                .setPhaseShifterRegulationOn(!LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON)
                .setTwtSplitShuntAdmittance(!LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE)
                .setShuntCompensatorVoltageControlOn(!LoadFlowParameters.DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON)
                .setReadSlackBus(!LoadFlowParameters.DEFAULT_READ_SLACK_BUS)
                .setWriteSlackBus(!LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS)
                .setDc(!LoadFlowParameters.DEFAULT_DC)
                .setDistributedSlack(!LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK)
                .setBalanceType(LoadFlowParameters.BalanceType.PROPORTIONAL_TO_LOAD)
                .setDcUseTransformerRatio(!LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT)
                .setCountriesToBalance(EnumSet.of(Country.FR))
                .setComponentMode(LoadFlowParameters.ComponentMode.ALL_CONNECTED)
                .setHvdcAcEmulation(!LoadFlowParameters.DEFAULT_HVDC_AC_EMULATION_ON)
                .setDcPowerFactor(0.5)
                .setDebugDir("/tmp/debug");
        // guard: the helper really leaves no property at its default
        LoadFlowParameters defaults = new LoadFlowParameters();
        assertNotEquals(defaults.toMap(), parameters.toMap());
        return parameters;
    }

    @Test
    void testModulePresentWithoutKeysResetsAllPropertiesToDefaults() {
        platformConfig.createModuleConfig("load-flow-default-parameters");

        LoadFlowParameters parameters = nonDefaultParameters();
        load(parameters, platformConfig);

        assertEquals(LoadFlowParameters.DEFAULT_VOLTAGE_INIT_MODE, parameters.getVoltageInitMode());
        assertEquals(LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, parameters.isTransformerVoltageControlOn());
        assertEquals(LoadFlowParameters.DEFAULT_USE_REACTIVE_LIMITS, parameters.isUseReactiveLimits());
        assertEquals(LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON, parameters.isPhaseShifterRegulationOn());
        assertEquals(LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE, parameters.isTwtSplitShuntAdmittance());
        assertEquals(LoadFlowParameters.DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON, parameters.isShuntCompensatorVoltageControlOn());
        assertEquals(LoadFlowParameters.DEFAULT_READ_SLACK_BUS, parameters.isReadSlackBus());
        assertEquals(LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS, parameters.isWriteSlackBus());
        assertEquals(LoadFlowParameters.DEFAULT_DC, parameters.isDc());
        assertEquals(LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK, parameters.isDistributedSlack());
        assertEquals(LoadFlowParameters.DEFAULT_BALANCE_TYPE, parameters.getBalanceType());
        assertEquals(LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT, parameters.isDcUseTransformerRatio());
        assertEquals(LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE, parameters.getCountriesToBalance());
        assertEquals(LoadFlowParameters.DEFAULT_COMPONENT_MODE, parameters.getComponentMode());
        assertEquals(LoadFlowParameters.DEFAULT_HVDC_AC_EMULATION_ON, parameters.isHvdcAcEmulation());
        assertEquals(LoadFlowParameters.DEFAULT_DC_POWER_FACTOR, parameters.getDcPowerFactor());
        assertEquals(LoadFlowParameters.DEFAULT_DEBUG_DIR, parameters.getDebugDir());
        assertEquals(new LoadFlowParameters().toMap(), parameters.toMap());
    }

    @Test
    void testStaleDeprecatedKeyIsIgnoredWhenNewNameIsPresent() {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-default-parameters");
        moduleConfig.setStringProperty("componentMode", "MAIN_CONNECTED");
        moduleConfig.setStringProperty("connectedComponentMode", "MAIN"); // not a ComponentMode name
        moduleConfig.setStringProperty("useReactiveLimits", "false");
        moduleConfig.setStringProperty("noGeneratorReactiveLimits", "1"); // not a boolean
        moduleConfig.setStringProperty("twtSplitShuntAdmittance", "true");
        moduleConfig.setStringProperty("specificCompatibility", "1");
        moduleConfig.setStringProperty("shuntCompensatorVoltageControlOn", "true");
        moduleConfig.setStringProperty("simulShunt", "1");

        LoadFlowParameters parameters = new LoadFlowParameters();
        load(parameters, platformConfig);

        assertEquals(LoadFlowParameters.ComponentMode.MAIN_CONNECTED, parameters.getComponentMode());
        assertFalse(parameters.isUseReactiveLimits());
        assertTrue(parameters.isTwtSplitShuntAdmittance());
        assertTrue(parameters.isShuntCompensatorVoltageControlOn());
    }

    @Test
    void testNeitherAliasNorNewNameGivesDefault() {
        platformConfig.createModuleConfig("load-flow-default-parameters");

        LoadFlowParameters parameters = new LoadFlowParameters()
                .setUseReactiveLimits(!LoadFlowParameters.DEFAULT_USE_REACTIVE_LIMITS)
                .setTwtSplitShuntAdmittance(!LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE)
                .setShuntCompensatorVoltageControlOn(!LoadFlowParameters.DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON)
                .setComponentMode(LoadFlowParameters.ComponentMode.ALL_CONNECTED);
        load(parameters, platformConfig);

        assertEquals(LoadFlowParameters.DEFAULT_USE_REACTIVE_LIMITS, parameters.isUseReactiveLimits());
        assertEquals(LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE, parameters.isTwtSplitShuntAdmittance());
        assertEquals(LoadFlowParameters.DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON, parameters.isShuntCompensatorVoltageControlOn());
        assertEquals(LoadFlowParameters.DEFAULT_COMPONENT_MODE, parameters.getComponentMode());
    }

    @Test
    void testNoModuleLeavesParametersUntouched() {
        // without the module, loading does not touch the object at all (this pins the constructor, not the reset)
        LoadFlowParameters parameters = new LoadFlowParameters();
        load(parameters, platformConfig);
        assertEquals(new LoadFlowParameters().toMap(), parameters.toMap());
    }

    @Test
    void testDeprecatedAliases() {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-default-parameters");
        moduleConfig.setStringProperty("noGeneratorReactiveLimits", "true");
        moduleConfig.setStringProperty("specificCompatibility", "true");
        moduleConfig.setStringProperty("simulShunt", "true");
        moduleConfig.setStringProperty("connectedComponentMode", "ALL_CONNECTED");

        LoadFlowParameters parameters = new LoadFlowParameters();
        load(parameters, platformConfig);

        // old names are honoured, noGeneratorReactiveLimits being negated
        assertFalse(parameters.isUseReactiveLimits());
        assertTrue(parameters.isTwtSplitShuntAdmittance());
        assertTrue(parameters.isShuntCompensatorVoltageControlOn());
        assertEquals(LoadFlowParameters.ComponentMode.ALL_CONNECTED, parameters.getComponentMode());

        // new names win over old names
        moduleConfig.setStringProperty("useReactiveLimits", "true");
        moduleConfig.setStringProperty("twtSplitShuntAdmittance", "false");
        moduleConfig.setStringProperty("shuntCompensatorVoltageControlOn", "false");
        moduleConfig.setStringProperty("componentMode", "MAIN_SYNCHRONOUS");

        parameters = new LoadFlowParameters();
        load(parameters, platformConfig);

        assertTrue(parameters.isUseReactiveLimits());
        assertFalse(parameters.isTwtSplitShuntAdmittance());
        assertFalse(parameters.isShuntCompensatorVoltageControlOn());
        assertEquals(LoadFlowParameters.ComponentMode.MAIN_SYNCHRONOUS, parameters.getComponentMode());
    }

    @Test
    void testUserConfigurationOverridesRuntimeConfigurationOverridesHardcodedDefaults() {
        InMemoryModuleConfigRepository userRepository = new InMemoryModuleConfigRepository(fileSystem);
        userRepository.createModuleConfig("load-flow-default-parameters")
                .setStringProperty("voltageInitMode", "UNIFORM_VALUES");
        InMemoryModuleConfigRepository runtimeRepository = new InMemoryModuleConfigRepository(fileSystem);
        MapModuleConfig runtimeModule = runtimeRepository.createModuleConfig("load-flow-default-parameters");
        runtimeModule.setStringProperty("voltageInitMode", "PREVIOUS_VALUES");
        runtimeModule.setStringProperty("dc", "true");

        // same stack as the one assembled by ClassicPlatformConfigProvider: user configuration above runtime configuration
        PlatformConfig stacked = new PlatformConfig(new StackedModuleConfigRepository(userRepository, runtimeRepository),
                fileSystem.getPath("/config"));
        LoadFlowParameters parameters = new LoadFlowParameters();
        load(parameters, stacked);

        // user wins over runtime,
        assertEquals(LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES, parameters.getVoltageInitMode());
        // runtime wins over hardcoded default,
        assertTrue(parameters.isDc());
        // hardcoded default when nobody defines the key
        assertEquals(LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK, parameters.isDistributedSlack());
    }
}
