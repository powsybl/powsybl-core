/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
class StateEstimationParametersTest {

    private static final String MODULE_NAME = "state-estimation-default-parameters";

    private FileSystem fileSystem;
    private InMemoryPlatformConfig platformConfig;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    private static void checkDefaults(StateEstimationParameters parameters) {
        assertEquals(StateEstimationParameters.DEFAULT_VOLTAGE_INIT_MODE, parameters.getVoltageInitMode());
        assertEquals(StateEstimationParameters.DEFAULT_ZERO_INJECTION_MODE, parameters.getZeroInjectionMode());
        assertEquals(StateEstimationParameters.DEFAULT_BUS_INJECTION_POLICY, parameters.getBusInjectionPolicy());
        assertEquals(StateEstimationParameters.DEFAULT_DUPLICATE_MEASUREMENT_POLICY, parameters.getDuplicateMeasurementPolicy());
        assertEquals(StateEstimationParameters.DEFAULT_UNOBSERVABLE_POLICY, parameters.getUnobservablePolicy());
        assertEquals(StateEstimationParameters.DEFAULT_COMPONENT_MODE, parameters.getComponentMode());
        assertEquals(StateEstimationParameters.DEFAULT_RELATIVE_STANDARD_DEVIATION_FLOOR, parameters.getRelativeStandardDeviationFloor());
        assertEquals(StateEstimationParameters.DEFAULT_RESIDUAL_FLAGGING_THRESHOLD, parameters.getResidualFlaggingThreshold());
        assertEquals(StateEstimationParameters.DEFAULT_WRITE_RESULTS_TO_NETWORK, parameters.isWriteResultsToNetwork());
    }

    @Test
    void readsNothingWhenTheConfigurationDefinesNoModule() {
        checkDefaults(StateEstimationParameters.load(platformConfig));
    }

    @Test
    void leavesTheSettingsAConfigurationDoesNotMention() {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig(MODULE_NAME);
        moduleConfig.setStringProperty("componentMode", "ALL");

        StateEstimationParameters parameters = StateEstimationParameters.load(platformConfig);

        assertEquals(StateEstimationParameters.ComponentMode.ALL, parameters.getComponentMode());
        assertEquals(StateEstimationParameters.DEFAULT_VOLTAGE_INIT_MODE, parameters.getVoltageInitMode());
        assertEquals(StateEstimationParameters.DEFAULT_RESIDUAL_FLAGGING_THRESHOLD, parameters.getResidualFlaggingThreshold());
    }

    @Test
    void readsEverySettingTheDocumentedModuleDefines() {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig(MODULE_NAME);
        moduleConfig.setStringProperty("voltageInitMode", "DC_VALUES");
        moduleConfig.setStringProperty("zeroInjectionMode", "EQUALITY_CONSTRAINT");
        moduleConfig.setStringProperty("busInjectionPolicy", "SUM_METERED");
        moduleConfig.setStringProperty("duplicateMeasurementPolicy", "KEEP_ALL");
        moduleConfig.setStringProperty("unobservablePolicy", "SKIP_COMPONENT");
        moduleConfig.setStringProperty("componentMode", "ALL");
        moduleConfig.setStringProperty("relativeStandardDeviationFloor", "0.01");
        moduleConfig.setStringProperty("residualFlaggingThreshold", "4.5");
        moduleConfig.setStringProperty("writeResultsToNetwork", "true");

        StateEstimationParameters parameters = StateEstimationParameters.load(platformConfig);

        assertEquals(StateEstimationParameters.VoltageInitMode.DC_VALUES, parameters.getVoltageInitMode());
        assertEquals(StateEstimationParameters.ZeroInjectionMode.EQUALITY_CONSTRAINT, parameters.getZeroInjectionMode());
        assertEquals(StateEstimationParameters.BusInjectionPolicy.SUM_METERED, parameters.getBusInjectionPolicy());
        assertEquals(StateEstimationParameters.DuplicateMeasurementPolicy.KEEP_ALL, parameters.getDuplicateMeasurementPolicy());
        assertEquals(StateEstimationParameters.UnobservablePolicy.SKIP_COMPONENT, parameters.getUnobservablePolicy());
        assertEquals(StateEstimationParameters.ComponentMode.ALL, parameters.getComponentMode());
        assertEquals(0.01, parameters.getRelativeStandardDeviationFloor());
        assertEquals(4.5, parameters.getResidualFlaggingThreshold());
        assertTrue(parameters.isWriteResultsToNetwork());
    }

    @Test
    void refusesAValueOutsideTheEnum() {
        platformConfig.createModuleConfig(MODULE_NAME).setStringProperty("voltageInitMode", "FLAT_START");
        assertThrows(IllegalArgumentException.class, () -> StateEstimationParameters.load(platformConfig));
    }

    @Test
    void refusesAConfigurationThatIsNotThere() {
        assertThrows(NullPointerException.class, () -> StateEstimationParameters.load(null));
    }

    @Test
    void readsTheDefaultPlatformConfiguration() {
        checkDefaults(StateEstimationParameters.load());
    }

    @Test
    void attachesTheImplementationsOwnSettings() {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig(StateEstimationProviderMock.MODULE_CONFIG_NAME);
        moduleConfig.setStringProperty(StateEstimationProviderMock.TOLERANCE, "1e-6");
        moduleConfig.setStringProperty(StateEstimationProviderMock.MAX_ITERATIONS, "12");

        StateEstimationParameters parameters = StateEstimationParameters.load(platformConfig);

        MockSpecificParameters specific = parameters.getExtension(MockSpecificParameters.class);
        assertNotNull(specific, "the provider on the classpath should have contributed its settings");
        assertEquals(1e-6, specific.getTolerance());
        assertEquals(12, specific.getMaxIterations());
    }

    @Test
    void leavesTheImplementationsOwnDefaultsWhenNothingConfiguresThem() {
        MockSpecificParameters specific = StateEstimationParameters.load(platformConfig)
                .getExtension(MockSpecificParameters.class);

        assertEquals(MockSpecificParameters.DEFAULT_TOLERANCE, specific.getTolerance());
        assertEquals(MockSpecificParameters.DEFAULT_MAX_ITERATIONS, specific.getMaxIterations());
    }

    @Test
    void setsAndChains() {
        StateEstimationParameters parameters = new StateEstimationParameters();

        StateEstimationParameters returned = parameters
                .setVoltageInitMode(StateEstimationParameters.VoltageInitMode.PREVIOUS_VALUES)
                .setRelativeStandardDeviationFloor(0.0)
                .setWriteResultsToNetwork(true);

        assertSame(parameters, returned);
        assertEquals(StateEstimationParameters.VoltageInitMode.PREVIOUS_VALUES, parameters.getVoltageInitMode());
        assertEquals(0.0, parameters.getRelativeStandardDeviationFloor());
        assertTrue(parameters.isWriteResultsToNetwork());
    }

    @Test
    void refusesAWeightingThatWouldDistortTheEstimate() {
        StateEstimationParameters parameters = new StateEstimationParameters();

        IllegalArgumentException negativeFloor = assertThrows(IllegalArgumentException.class,
                () -> parameters.setRelativeStandardDeviationFloor(-0.01));
        assertTrue(negativeFloor.getMessage().contains("-0.01"),
                "the message should name the value that was rejected: " + negativeFloor.getMessage());

        assertThrows(IllegalArgumentException.class, () -> parameters.setResidualFlaggingThreshold(0.0));
        assertThrows(IllegalArgumentException.class, () -> parameters.setResidualFlaggingThreshold(-1.0));

        assertEquals(StateEstimationParameters.DEFAULT_RELATIVE_STANDARD_DEVIATION_FLOOR,
                parameters.getRelativeStandardDeviationFloor());
        assertEquals(StateEstimationParameters.DEFAULT_RESIDUAL_FLAGGING_THRESHOLD,
                parameters.getResidualFlaggingThreshold());
    }
}
