/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public abstract class AbstractVoltageRegulationTest {

    private Network network;
    private VoltageLevel voltageLevel;
    private Terminal remoteTerminal;

    @BeforeEach
    void initNetwork() {
        network = BatteryNetworkFactory.create();
        voltageLevel = network.getVoltageLevel("VLGEN");
        remoteTerminal = network.getBattery("BAT").getTerminal();
    }

    // Cases generator regulating
    @ParameterizedTest(name = "{argumentSetName}")
    @MethodSource("provideGeneratorRegulating")
    void testGeneratorRegulating(DataGeneratorCreator dataGeneratorCreator, String validationErrorOnRegulatingFalse) {
        Generator generator = createGenerator(dataGeneratorCreator);
        assertTrue(generator.isRegulating());
        VoltageRegulation voltageRegulation = generator.getVoltageRegulation();
        if (validationErrorOnRegulatingFalse != null) {
            ValidationException validationException = assertThrows(ValidationException.class, () -> voltageRegulation.setRegulating(false));
            assertEquals(validationErrorOnRegulatingFalse, validationException.getMessage());
        } else {
            voltageRegulation.setRegulating(false);
            assertFalse(generator.isRegulating());
        }
    }

    // Cases missing VoltageRegulation
    @Test
    void testMissingVoltageRegulationOk() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("missingVoltageRegulation")
            .setTargetQ(10);
        // WHEN
        Generator generator = generatorAdder.add();
        // THEN
        assertEquals(10, generator.getLocalTargetQ());
        assertFalse(generator.isRegulating());
    }

    @Test
    void testMissingVoltageRegulationWithMissingTargetQ() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("missingVoltageRegulation");
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, generatorAdder::add);
        // THEN
        assertEquals("Generator 'missingVoltageRegulation': invalid value (NaN) for localTargetQ (voltageRegulation is not set)", validationException.getMessage());
    }

    // Cases Regulating True, Terminal NUll, Mode VOLTAGE

    @Test
    void testGeneratorOk() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("ErrorTargetValuePresent_when_terminal_absent");
        generatorAdder
            .setLocalTargetV(24)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .add();
        // WHEN
        Generator generator = generatorAdder.add();
        // THEN
        assertEquals(24, generator.getLocalTargetV());
        assertTrue(generator.isRegulating());
    }

    @Test
    void testGeneratorErrorLocalTargetVMissing() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("LocalVoltageTargetV_missing");
        generatorAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .add();
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, generatorAdder::add);
        // THEN
        assertEquals("Generator 'LocalVoltageTargetV_missing': invalid value (NaN) for localTargetV (voltageRegulation is set with VOLTAGE mode and regulating true and unset terminal)", validationException.getMessage());
    }

    @Test
    void testGeneratorErrorLocalTargetVMissingTargetQPresent() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("LocalVoltageTargetV_missing");
        generatorAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .add()
            .setTargetQ(10);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, generatorAdder::add);
        // THEN
        assertEquals("Generator 'LocalVoltageTargetV_missing': invalid value (NaN) for localTargetV (voltageRegulation is set with VOLTAGE mode and regulating true and unset terminal)", validationException.getMessage());
    }

    @Test
    void testGeneratorErrorLocalTargetVMissingTargetValuePresent() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("LocalVoltageTargetV_missing");
        VoltageRegulationAdder<GeneratorAdder> voltageRegulationAdder = generatorAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(240);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, voltageRegulationAdder::add);
        // THEN
        assertEquals("Generator 'LocalVoltageTargetV_missing': Invalid value for voltageRegulation.targetValue, expected NaN when a terminal is not set", validationException.getMessage());
    }

    @Test
    void testGeneratorErrorTargetValuePresent() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("ErrorTargetValuePresent_when_terminal_absent");
        VoltageRegulationAdder<GeneratorAdder> voltageRegulationAdder = generatorAdder
            .setLocalTargetV(24)
            .newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTargetValue(240);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, voltageRegulationAdder::add);
        // THEN
        assertEquals("Generator 'ErrorTargetValuePresent_when_terminal_absent': Invalid value for voltageRegulation.targetValue, expected NaN when a terminal is not set", validationException.getMessage());
    }

    // Cases Regulating True, Terminal present, Mode VOLTAGE
    // TODO MSA add tests

    // Cases Regulating false, Terminal NUll, Mode VOLTAGE
    // TODO MSA add tests

    // Cases Regulating false, Terminal present, Mode VOLTAGE
    // TODO MSA add tests

    private GeneratorAdder newGeneratorAdder(String id) {
        return voltageLevel.newGenerator()
            .setId(id)
            .setBus("NGEN")
            .setConnectableBus("NGEN")
            .setMinP(12.0)
            .setMaxP(120.0)
            .setTargetP(100.0);
    }


    record DataGeneratorCreator(
        String id,
        RegulationMode mode,
        boolean remoteTerminal,
        double targetValue,
        double localTargetV,
        double localTargetQ,
        boolean regulating
    ) { }

    private Generator createGenerator(DataGeneratorCreator dataGeneratorCreator) {
        GeneratorAdder generatorAdder = newGeneratorAdder(dataGeneratorCreator.id())
            .setLocalTargetV(dataGeneratorCreator.localTargetV())
            .setTargetQ(dataGeneratorCreator.localTargetQ());
        if (dataGeneratorCreator.mode() != null) {
            generatorAdder.newVoltageRegulation()
                .withRegulating(dataGeneratorCreator.regulating())
                .withMode(dataGeneratorCreator.mode())
                .withTargetValue(dataGeneratorCreator.targetValue())
                .withTerminal(dataGeneratorCreator.remoteTerminal() ? remoteTerminal : null)
                .add();
        }
        return generatorAdder.add();
    }

    private static Stream<Arguments> provideGeneratorRegulating() {
        DataGeneratorCreator regulatingLocalVoltage = new DataGeneratorCreator("regulatingLocalVoltage",
            RegulationMode.VOLTAGE,
            false,
            Double.NaN,
            24.5,
            Double.NaN,
            true);
        DataGeneratorCreator regulatingLocalVoltageWithTargetQ = new DataGeneratorCreator("regulatingLocalVoltageWithTargetQ",
            RegulationMode.VOLTAGE,
            false,
            Double.NaN,
            24.5,
            10,
            true);
        DataGeneratorCreator regulatingRemoteVoltage = new DataGeneratorCreator("regulatingRemoteVoltage",
            RegulationMode.VOLTAGE,
            true,
            400,
            Double.NaN,
            Double.NaN,
            true);
        DataGeneratorCreator regulatingRemoteVoltageWithLocalTargetV = new DataGeneratorCreator("regulatingRemoteVoltageWithLocalTargetV",
            RegulationMode.VOLTAGE,
            true,
            400,
            24.5,
            Double.NaN,
            true);
        DataGeneratorCreator regulatingRemoteVoltageWithLocalTargetVAndTargetQ = new DataGeneratorCreator("regulatingRemoteVoltageWithLocalTargetVAndTargetQ",
            RegulationMode.VOLTAGE,
            true,
            400,
            24.5,
            10,
            true);
        DataGeneratorCreator regulatingRemoteVoltageWithTargetQ = new DataGeneratorCreator("regulatingRemoteVoltageWithTargetQ",
            RegulationMode.VOLTAGE,
            true,
            400,
            Double.NaN,
            10,
            true);
        DataGeneratorCreator regulatingRemoteReactiveP = new DataGeneratorCreator("regulatingRemoteReactiveP",
            RegulationMode.REACTIVE_POWER,
            true,
            100,
            Double.NaN,
            Double.NaN,
            true);
        DataGeneratorCreator regulatingRemoteReactivePWithLocalTargetV = new DataGeneratorCreator("regulatingRemoteReactivePWithLocalTargetV",
            RegulationMode.REACTIVE_POWER,
            true,
            100,
            24.5,
            Double.NaN,
            true);
        DataGeneratorCreator regulatingRemoteReactivePWithLocalTargetVAndTargetQ = new DataGeneratorCreator("regulatingRemoteReactivePWithLocalTargetVAndTargetQ",
            RegulationMode.REACTIVE_POWER,
            true,
            100,
            24.5,
            10,
            true);
        DataGeneratorCreator regulatingRemoteReactivePWithTargetQ = new DataGeneratorCreator("regulatingRemoteReactivePWithTargetQ",
            RegulationMode.REACTIVE_POWER,
            true,
            100,
            Double.NaN,
            10,
            true);
        return Stream.of(
            addArgumentSet(regulatingLocalVoltage, true),
            addArgumentSet(regulatingLocalVoltageWithTargetQ, false),
            addArgumentSet(regulatingRemoteVoltage, false),
            addArgumentSet(regulatingRemoteVoltageWithLocalTargetV, false),
            addArgumentSet(regulatingRemoteVoltageWithLocalTargetVAndTargetQ, false),
            addArgumentSet(regulatingRemoteVoltageWithTargetQ, false),
            addArgumentSet(regulatingRemoteReactiveP, false),
            addArgumentSet(regulatingRemoteReactivePWithLocalTargetV, false),
            addArgumentSet(regulatingRemoteReactivePWithLocalTargetVAndTargetQ, false),
            addArgumentSet(regulatingRemoteReactivePWithTargetQ, false)
        );
    }

    private static Arguments.@NonNull ArgumentSet addArgumentSet(DataGeneratorCreator regulatingLocalVoltage, boolean withValidationError) {
        String validationError = "Generator '%s': invalid value (NaN) for localTargetQ (voltageRegulation is set with regulating false)";
        return argumentSet(regulatingLocalVoltage.id(),
            regulatingLocalVoltage,
            withValidationError ? String.format(validationError, regulatingLocalVoltage.id()) : null);
    }
}
