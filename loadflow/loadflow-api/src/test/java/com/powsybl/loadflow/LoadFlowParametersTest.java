/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Country;
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest.DummyExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.it>}
 */
class LoadFlowParametersTest {

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

    private void checkValues(LoadFlowParameters parameters, LoadFlowParameters.VoltageInitMode voltageInitMode,
                             boolean transformerVoltageControlOn, boolean useReactiveLimits,
                             boolean phaseShifterRegulationOn, boolean twtSplitShuntAdmittance,
                             boolean simulShunt, boolean readSlackBus, boolean writeSlackBus,
                             boolean dc, boolean distributedSlack, LoadFlowParameters.BalanceType balanceType,
                             boolean dcUseTransformerRatio, Set<Country> countriesToBalance,
                             LoadFlowParameters.ConnectedComponentMode computedConnectedComponent,
                             boolean hvdcAcEmulation) {
        assertEquals(parameters.getVoltageInitMode(), voltageInitMode);
        assertEquals(parameters.isTransformerVoltageControlOn(), transformerVoltageControlOn);
        assertEquals(parameters.isPhaseShifterRegulationOn(), phaseShifterRegulationOn);
        assertEquals(parameters.isUseReactiveLimits(), useReactiveLimits);
        assertEquals(parameters.isTwtSplitShuntAdmittance(), twtSplitShuntAdmittance);
        assertEquals(parameters.isShuntCompensatorVoltageControlOn(), simulShunt);
        assertEquals(parameters.isReadSlackBus(), readSlackBus);
        assertEquals(parameters.isWriteSlackBus(), writeSlackBus);
        assertEquals(parameters.isDc(), dc);
        assertEquals(parameters.isDistributedSlack(), distributedSlack);
        assertEquals(parameters.getBalanceType(), balanceType);
        assertEquals(parameters.isDcUseTransformerRatio(), dcUseTransformerRatio);
        assertEquals(parameters.getCountriesToBalance(), countriesToBalance);
        assertEquals(parameters.getConnectedComponentMode(), computedConnectedComponent);
        assertEquals(parameters.isHvdcAcEmulation(), hvdcAcEmulation);
    }

    @Test
    void testNoConfig() {
        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        checkValues(parameters, LoadFlowParameters.DEFAULT_VOLTAGE_INIT_MODE,
                LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_USE_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON,
                LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE,
                LoadFlowParameters.DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_READ_SLACK_BUS,
                LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS,
                LoadFlowParameters.DEFAULT_DC,
                LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK,
                LoadFlowParameters.DEFAULT_BALANCE_TYPE,
                LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT,
                LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE,
                LoadFlowParameters.DEFAULT_CONNECTED_COMPONENT_MODE,
                LoadFlowParameters.DEFAULT_HVDC_AC_EMULATION_ON);
    }

    @Test
    void checkConfig() {
        boolean transformerVoltageControlOn = true;
        boolean noGeneratorReactiveLimits = true;
        boolean phaseShifterRegulationOn = true;
        boolean twtSplitShuntAdmittance = true;
        boolean simulShunt = true;
        boolean readSlackBus = true;
        boolean writeSlackBus = true;
        boolean voltageRemoteControl = true;
        boolean dc = true;
        boolean distributedSlack = true;
        LoadFlowParameters.BalanceType balanceType = LoadFlowParameters.BalanceType.PROPORTIONAL_TO_LOAD;
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES;
        boolean dcUseTransformerRatio = true;
        Set<Country> countriesToBalance = new HashSet<>();
        LoadFlowParameters.ConnectedComponentMode computedConnectedComponent = LoadFlowParameters.ConnectedComponentMode.MAIN;
        boolean hvdcAcEmulation = false;

        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-default-parameters");
        moduleConfig.setStringProperty("voltageInitMode", "UNIFORM_VALUES");
        moduleConfig.setStringProperty("transformerVoltageControlOn", Boolean.toString(transformerVoltageControlOn));
        moduleConfig.setStringProperty("noGeneratorReactiveLimits", Boolean.toString(noGeneratorReactiveLimits));
        moduleConfig.setStringProperty("phaseShifterRegulationOn", Boolean.toString(phaseShifterRegulationOn));
        moduleConfig.setStringProperty("twtSplitShuntAdmittance", Boolean.toString(twtSplitShuntAdmittance));
        moduleConfig.setStringProperty("simulShunt", Boolean.toString(simulShunt));
        moduleConfig.setStringProperty("readSlackBus", Boolean.toString(readSlackBus));
        moduleConfig.setStringProperty("writeSlackBus", Boolean.toString(writeSlackBus));
        moduleConfig.setStringProperty("voltageRemoteControl", Boolean.toString(voltageRemoteControl));
        moduleConfig.setStringProperty("dc", Boolean.toString(dc));
        moduleConfig.setStringProperty("distributedSlack", Boolean.toString(dc));
        moduleConfig.setStringProperty("balanceType", balanceType.name());
        moduleConfig.setStringProperty("dcUseTransformerRatio", Boolean.toString(dc));
        moduleConfig.setStringListProperty("countriesToBalance", countriesToBalance.stream().map(e -> e.name()).collect(Collectors.toList()));
        moduleConfig.setStringProperty("computedConnectedComponent", computedConnectedComponent.name());
        moduleConfig.setStringProperty("hvdcAcEmulation", Boolean.toString(hvdcAcEmulation));

        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        checkValues(parameters, voltageInitMode, transformerVoltageControlOn,
                    noGeneratorReactiveLimits, phaseShifterRegulationOn, twtSplitShuntAdmittance, simulShunt, readSlackBus, writeSlackBus,
                    dc, distributedSlack, balanceType, dcUseTransformerRatio, countriesToBalance, computedConnectedComponent, hvdcAcEmulation);
    }

    @Test
    void checkIncompleteConfig() {
        boolean transformerVoltageControlOn = true;
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-default-parameters");
        moduleConfig.setStringProperty("transformerVoltageControlOn", Boolean.toString(transformerVoltageControlOn));
        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        checkValues(parameters, LoadFlowParameters.DEFAULT_VOLTAGE_INIT_MODE,
                transformerVoltageControlOn, LoadFlowParameters.DEFAULT_USE_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON, LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE,
                LoadFlowParameters.DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON, LoadFlowParameters.DEFAULT_READ_SLACK_BUS, LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS,
                LoadFlowParameters.DEFAULT_DC, LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK, LoadFlowParameters.DEFAULT_BALANCE_TYPE,
                LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT, LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE,
                LoadFlowParameters.DEFAULT_CONNECTED_COMPONENT_MODE, LoadFlowParameters.DEFAULT_HVDC_AC_EMULATION_ON);
    }

    @Test
    void checkDefaultPlatformConfig() {
        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters);
        checkValues(parameters, LoadFlowParameters.DEFAULT_VOLTAGE_INIT_MODE,
                LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, LoadFlowParameters.DEFAULT_USE_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON, LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE,
                LoadFlowParameters.DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON, LoadFlowParameters.DEFAULT_READ_SLACK_BUS, LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS,
                LoadFlowParameters.DEFAULT_DC, LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK, LoadFlowParameters.DEFAULT_BALANCE_TYPE,
                LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT, LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE,
                LoadFlowParameters.DEFAULT_CONNECTED_COMPONENT_MODE, LoadFlowParameters.DEFAULT_HVDC_AC_EMULATION_ON);
    }

    @Test
    void checkConstructorByVoltageInitMode() {
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;
        LoadFlowParameters parameters = new LoadFlowParameters()
                .setVoltageInitMode(voltageInitMode);
        checkValues(parameters, voltageInitMode, LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_USE_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON, LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE,
                LoadFlowParameters.DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON, LoadFlowParameters.DEFAULT_READ_SLACK_BUS, LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS,
                LoadFlowParameters.DEFAULT_DC, LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK, LoadFlowParameters.DEFAULT_BALANCE_TYPE,
                LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT, LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE,
                LoadFlowParameters.DEFAULT_CONNECTED_COMPONENT_MODE, LoadFlowParameters.DEFAULT_HVDC_AC_EMULATION_ON);
    }

    @Test
    void checkConstructorByVoltageInitModeAndTransformerVoltageControlOn() {
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;
        boolean transformerVoltageControlOn = true;
        LoadFlowParameters parameters = new LoadFlowParameters()
                .setVoltageInitMode(voltageInitMode)
                .setTransformerVoltageControlOn(transformerVoltageControlOn);
        checkValues(parameters, voltageInitMode, true, LoadFlowParameters.DEFAULT_USE_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON,
                LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE,
                LoadFlowParameters.DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_READ_SLACK_BUS,
                LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS,
                LoadFlowParameters.DEFAULT_DC,
                LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK,
                LoadFlowParameters.DEFAULT_BALANCE_TYPE,
                LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT,
                LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE,
                LoadFlowParameters.DEFAULT_CONNECTED_COMPONENT_MODE,
                LoadFlowParameters.DEFAULT_HVDC_AC_EMULATION_ON);
    }

    @Test
    void checkConstructorByLoadFlowParameters() {
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;
        LoadFlowParameters parameters = new LoadFlowParameters()
                .setVoltageInitMode(voltageInitMode);
        checkValues(parameters, voltageInitMode, LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_USE_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON,
                LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE,
                LoadFlowParameters.DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_READ_SLACK_BUS,
                LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS,
                LoadFlowParameters.DEFAULT_DC,
                LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK,
                LoadFlowParameters.DEFAULT_BALANCE_TYPE,
                LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT,
                LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE,
                LoadFlowParameters.DEFAULT_CONNECTED_COMPONENT_MODE,
                LoadFlowParameters.DEFAULT_HVDC_AC_EMULATION_ON);

        LoadFlowParameters parameters1 = new LoadFlowParameters(parameters);
        parameters1.setDc(true);
        parameters1.setDistributedSlack(false);
        parameters1.setBalanceType(LoadFlowParameters.BalanceType.PROPORTIONAL_TO_LOAD);
        checkValues(parameters1, voltageInitMode,
                LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_USE_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON,
                LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE,
                LoadFlowParameters.DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_READ_SLACK_BUS,
                LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS,
                true,
                false,
                LoadFlowParameters.BalanceType.PROPORTIONAL_TO_LOAD,
                LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT,
                LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE,
                LoadFlowParameters.DEFAULT_CONNECTED_COMPONENT_MODE,
                LoadFlowParameters.DEFAULT_HVDC_AC_EMULATION_ON);
    }

    @Test
    void checkSetters() {
        boolean transformerVoltageControlOn = true;
        boolean useReactiveLimits = false;
        boolean phaseShifterRegulationOn = true;
        boolean twtSplitShuntAdmittance = true;
        boolean simulShunt = true;
        boolean readSlackBus = true;
        boolean writeSlackBus = true;
        boolean dc = true;
        boolean distributedSlack = false;
        LoadFlowParameters.BalanceType balanceType = LoadFlowParameters.BalanceType.PROPORTIONAL_TO_LOAD;
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;
        boolean dcUseTransformerRatio = true;
        Set<Country> countriesToBalance = new HashSet<>();
        LoadFlowParameters.ConnectedComponentMode computedConnectedComponent = LoadFlowParameters.ConnectedComponentMode.MAIN;
        boolean hvdcAcEmulation = false;

        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        parameters.setUseReactiveLimits(useReactiveLimits)
                .setPhaseShifterRegulationOn(phaseShifterRegulationOn)
                .setTransformerVoltageControlOn(transformerVoltageControlOn)
                .setVoltageInitMode(voltageInitMode)
                .setTwtSplitShuntAdmittance(twtSplitShuntAdmittance)
                .setShuntCompensatorVoltageControlOn(simulShunt)
                .setReadSlackBus(readSlackBus)
                .setWriteSlackBus(writeSlackBus)
                .setDc(dc)
                .setDistributedSlack(distributedSlack)
                .setBalanceType(balanceType)
                .setHvdcAcEmulation(hvdcAcEmulation);

        checkValues(parameters, voltageInitMode, transformerVoltageControlOn, useReactiveLimits,
                    phaseShifterRegulationOn, twtSplitShuntAdmittance, simulShunt, readSlackBus, writeSlackBus,
                    dc, distributedSlack, balanceType, dcUseTransformerRatio, countriesToBalance, computedConnectedComponent, hvdcAcEmulation);
    }

    @Test
    void checkClone() {
        boolean transformerVoltageControlOn = true;
        boolean useReactiveLimits = false;
        boolean phaseShifterRegulationOn = true;
        boolean twtSplitShuntAdmittance = true;
        boolean simulShunt = true;
        boolean readSlackBus = true;
        boolean writeSlackBus = true;
        boolean dc = true;
        boolean distributedSlack = false;
        LoadFlowParameters.BalanceType balanceType = LoadFlowParameters.BalanceType.PROPORTIONAL_TO_LOAD;
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES;
        boolean dcUseTransformerRatio = true;
        Set<Country> countriesToBalance = new HashSet<>();
        LoadFlowParameters.ConnectedComponentMode computedConnectedComponent = LoadFlowParameters.ConnectedComponentMode.MAIN;
        boolean hvdcAcEmulation = false;
        LoadFlowParameters parameters = new LoadFlowParameters()
                .setVoltageInitMode(voltageInitMode)
                .setTransformerVoltageControlOn(transformerVoltageControlOn)
                .setUseReactiveLimits(useReactiveLimits)
                .setPhaseShifterRegulationOn(phaseShifterRegulationOn)
                .setTwtSplitShuntAdmittance(twtSplitShuntAdmittance)
                .setShuntCompensatorVoltageControlOn(simulShunt)
                .setReadSlackBus(readSlackBus)
                .setWriteSlackBus(writeSlackBus)
                .setDc(dc)
                .setDistributedSlack(distributedSlack)
                .setBalanceType(balanceType)
                .setDcUseTransformerRatio(dcUseTransformerRatio)
                .setCountriesToBalance(countriesToBalance)
                .setConnectedComponentMode(computedConnectedComponent)
                .setHvdcAcEmulation(hvdcAcEmulation);
        LoadFlowParameters parametersCloned = parameters.copy();
        checkValues(parametersCloned, parameters.getVoltageInitMode(), parameters.isTransformerVoltageControlOn(),
                parameters.isUseReactiveLimits(), parameters.isPhaseShifterRegulationOn(), parameters.isTwtSplitShuntAdmittance(),
                parameters.isShuntCompensatorVoltageControlOn(), parameters.isReadSlackBus(), parameters.isWriteSlackBus(),
                parameters.isDc(), parameters.isDistributedSlack(), parameters.getBalanceType(), parameters.isDcUseTransformerRatio(),
                parameters.getCountriesToBalance(), parameters.getConnectedComponentMode(), parameters.isHvdcAcEmulation());
    }

    @Test
    void testExtensions() {
        LoadFlowParameters parameters = new LoadFlowParameters();
        DummyExtension dummyExtension = new DummyExtension();
        parameters.addExtension(DummyExtension.class, dummyExtension);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensions().contains(dummyExtension));
        assertTrue(parameters.getExtensionByName("dummy-extension") instanceof DummyExtension);
        assertTrue(parameters.getExtension(DummyExtension.class) instanceof DummyExtension);
    }

    @Test
    void testCopyWithExtension() {
        LoadFlowParameters parameters = new LoadFlowParameters();
        DummyExtension dummyExtension = new DummyExtension();
        parameters.addExtension(DummyExtension.class, dummyExtension);
        LoadFlowParameters copy = parameters.copy();
        assertEquals(1, copy.getExtensions().size());
        Extension<LoadFlowParameters> copiedExt = copy.getExtensionByName("dummy-extension");
        assertSame(parameters, dummyExtension.getExtendable());
        assertSame(copy, copiedExt.getExtendable());
    }

    @Test
    void testNoExtensions() {
        LoadFlowParameters parameters = new LoadFlowParameters();

        assertEquals(0, parameters.getExtensions().size());
        assertFalse(parameters.getExtensions().contains(new DummyExtension()));
        assertFalse(parameters.getExtensionByName("dummy-extension") instanceof DummyExtension);
        assertFalse(parameters.getExtension(DummyExtension.class) instanceof DummyExtension);
    }

    @Test
    void testExtensionFromConfig() {
        LoadFlowParameters parameters = LoadFlowParameters.load(platformConfig);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensionByName("dummy-extension") instanceof DummyExtension);
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }
}
