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
import org.junit.jupiter.api.Disabled;
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

    // Cases Regulating false, Terminal NUll, Mode VOLTAGE

    // Cases Regulating false, Terminal present, Mode VOLTAGE

    @Disabled("TODO MSA check me")
    @Test
    void testGeneratorWithVoltageRegulation() {
        // GIVEN
        Generator generator = network.getGenerator("GEN");
        generator.setLocalTargetQ(Double.NaN);
        // WHEN
        VoltageRegulation voltageRegulation = generator.getVoltageRegulation();
        ValidationException exception = assertThrows(ValidationException.class, () -> voltageRegulation.setRegulating(false));
        // THEN
        assertEquals("Generator 'GEN': invalid value (NaN) for localTargetQ (voltageRegulation is set with regulating false)", exception.getMessage());
        assertTrue(voltageRegulation.isRegulating());
    }

    @Disabled("TODO MSA check me")
    @Test
    void testGeneratorWithVoltageRegulationRemoteTransformIntoLocal() {
        // GIVEN
        Terminal terminalFromRemoteGen = network.getGenerator("GEN").getTerminal();
        Generator generator = voltageLevel.newGenerator()
            .setId("GEN_With_remote_terminal")
            .setBus("NBAT")
            .setConnectableBus("NBAT")
            .setLocalTargetV(24)
            .setMinP(10)
            .setMaxP(120)
            .setTargetP(100)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(400)
            .withTerminal(terminalFromRemoteGen)
            .add()
            .add();
        assertEquals(400, generator.getRegulatingTargetV());
        // WHEN
        generator.getVoltageRegulation().removeTerminal();
        // THEN
        assertEquals(Double.NaN, generator.getRegulatingTargetQ());
        assertEquals(24, generator.getRegulatingTargetV());
    }

    @Disabled("TODO MSA check me")
    @Test
    void testDifferentTerminalButSameConnectableBus() {
        // GIVEN
        Terminal terminalFromRemoteGen = network.getGenerator("GEN").getTerminal();
        VoltageLevel voltageLevelGen = terminalFromRemoteGen.getVoltageLevel();
        Generator generator = voltageLevelGen.newGenerator()
            .setId("GEN_With_terminal_same_connectable_bus")
            .setBus("NGEN")
            .setConnectableBus("NGEN")
            .setMinP(10)
            .setMaxP(120)
            .setTargetP(100)
            .setLocalTargetV(123)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(400)
            .withTerminal(terminalFromRemoteGen)
            .add()
            .add();
        // WHEN
        // THEN
        assertTrue(generator.isRemoteRegulating());

    }

    @Disabled("TODO MSA check me")
    @Test
    void testMsa() {
        // GIVEN
        Terminal terminalFromGen = network.getGenerator("GEN").getTerminal();
        Terminal terminalFromBat = network.getBattery("BAT").getTerminal();
        // WHEN
//        voltageLevel.newGenerator()
//            .setId("GEN_12987")
//            .setBus("NBAT")
//            .setConnectableBus("NBAT")
//            .setMinP(-999.0)
//            .setMaxP(999.0)
//            .setTargetP(100.0)
//            .setTargetV(230)
//            .newVoltageRegulation()
//                .withMode(RegulationMode.VOLTAGE)
//                .withTerminal(terminalFromGen)
//                .withTargetValue(400)
//                .add()
//            .add();
//        network.getGenerator("GEN").getVoltageRegulation().setTerminal(terminalFromGen, 210);
//        network.getGenerator("GEN").getVoltageRegulation().removeTerminal();
//        network.getGenerator("GEN").getVoltageRegulation().setTargetValue(200);
//        network.getGenerator("GEN").setTargetQ(12);
//        network.getGenerator("GEN").getVoltageRegulation().setRegulating(false);
        network.getGenerator("GEN").getVoltageRegulation().setTargetValue(123);
        // THEN

        // case newEquipment -> The Terminal can only be remote
//        voltageLevel.newGenerator()
//            .setId("GEN_12987_MSA")
//            .setBus("NBAT")
//            .setConnectableBus("NBAT")
//            .setMinP(-999.0)
//            .setMaxP(999.0)
//            .setTargetP(100.0)
//            .setTargetV(230)
//            .newVoltageRegulation()
//                .withMode(RegulationMode.VOLTAGE)
//                .withTerminal(terminalFromGen)
//                .withTargetValue(400)
//                .add()
//            .add();

        // case equipment already created -> The Terminal can be remote or local
//        network.getGenerator("GEN").getVoltageRegulation().setTerminal(terminalFromGen);
//        network.getGenerator("GEN").getVoltageRegulation().setTerminal(terminalFromBat);

        // case missing LocalTargetV with VOLTAGE regulating true
        ValidationException validationException = assertThrows(ValidationException.class, () -> voltageLevel.newGenerator()
            .setId("GEN_134_MSA")
            .setBus("NBAT")
            .setConnectableBus("NBAT")
            .setMinP(12.0)
            .setMaxP(120.0)
            .setTargetP(100.0)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .add()
            .add());
        assertEquals("Generator 'GEN_134_MSA': invalid value (NaN) for localTargetV (VoltageRegulation with VOLTAGE mode)", validationException.getMessage());
        // case missing LocalTargetQ with VOLTAGE regulating false
        validationException = assertThrows(ValidationException.class, () -> voltageLevel.newGenerator()
            .setId("GEN_134_MSA")
            .setBus("NBAT")
            .setConnectableBus("NBAT")
            .setMinP(12.0)
            .setMaxP(120.0)
            .setTargetP(100.0)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false)
            .add()
            .add());
        assertEquals("Generator 'GEN_134_MSA': invalid value (NaN) for targetQ (VoltageRegulation not set or set with regulating=false)", validationException.getMessage());
        // case missing LocalTargetQ without voltageRegulation
        validationException = assertThrows(ValidationException.class, () -> voltageLevel.newGenerator()
            .setId("GEN_134_MSA")
            .setBus("NBAT")
            .setConnectableBus("NBAT")
            .setMinP(12.0)
            .setMaxP(120.0)
            .setTargetP(100.0)
            .add());
        assertEquals("Generator 'GEN_134_MSA': invalid value (NaN) for localTargetQ (voltageRegulation is not set)", validationException.getMessage());
        // case missing LocalTargetQ without voltageRegulation
        validationException = assertThrows(ValidationException.class, () -> voltageLevel.newGenerator()
            .setId("GEN_134_MSA")
            .setBus("NBAT")
            .setConnectableBus("NBAT")
            .setMinP(12.0)
            .setMaxP(120.0)
            .setTargetP(100.0)
            .newVoltageRegulation()
            .withMode(RegulationMode.REACTIVE_POWER)
            .add()
            .add());
        assertEquals("Generator 'GEN_134_MSA': invalid value (NaN) for localTargetQ (voltageRegulation is set with REACTIVE_POWER mode)", validationException.getMessage());
        // case negative LocalTargetV
        validationException = assertThrows(ValidationException.class, () -> voltageLevel.newGenerator()
            .setId("GEN_134_MSA")
            .setBus("NBAT")
            .setConnectableBus("NBAT")
            .setMinP(12.0)
            .setMaxP(120.0)
            .setTargetP(100.0)
            .setTargetV(-24.5)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .add()
            .add());
        assertEquals("Generator 'GEN_134_MSA': invalid value (-24.5) for localTargetV (must be positive)", validationException.getMessage());
        // case missing targetValue for remoteTerminal
        validationException = assertThrows(ValidationException.class, () -> voltageLevel.newGenerator()
            .setId("GEN_134_MSA")
            .setBus("NBAT")
            .setConnectableBus("NBAT")
            .setMinP(12.0)
            .setMaxP(120.0)
            .setTargetP(100.0)
            .setTargetV(-24.5)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(terminalFromGen)
            .add()
            .add());
        assertEquals("Generator 'GEN_134_MSA': Undefined value for voltageRegulation.targetValue (remote terminal)", validationException.getMessage());
    }

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
