/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public abstract class AbstractVoltageRegulationOnGeneratorTest extends AbstractVoltageRegulationCommon<Generator> {

    @Test
    public void shouldUnregisterTerminalOnRemoveGeneratorWithRemoteVoltageRegulation() {
        String generatorId = "removedGenerator";
        DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator = new DataVoltageRegulationHolderCreator(generatorId,
            RegulationMode.VOLTAGE,
            true,
            220,
            24.5,
            Double.NaN,
            true);
        Generator generator = createGenerator(dataVoltageRegulationHolderCreator);
        assertEquals(1, remoteTerminal.getReferrers().size());
        generator.remove();
        assertNull(network.getGenerator(generatorId));
        assertEquals(0, remoteTerminal.getReferrers().size());
    }

    @Test
    public void shouldRemoveGeneratorOnRemoveGeneratorWithoutVoltageRegulation() {
        String generatorId = "removedGenerator";
        Generator generator = newGeneratorAdder(generatorId).setLocalTargetQ(10).add();
        assertEquals(0, remoteTerminal.getReferrers().size());
        generator.remove();
        assertNull(network.getGenerator(generatorId));
        assertEquals(0, remoteTerminal.getReferrers().size());
    }

    // Cases generator regulating
    @ParameterizedTest(name = "{argumentSetName}")
    @MethodSource("provideGeneratorRegulating")
    public void testGeneratorRegulating(DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator, String validationErrorOnRegulatingFalse) {
        Generator generator = createGenerator(dataVoltageRegulationHolderCreator);
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
    public void testMissingVoltageRegulationOk() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("missingVoltageRegulation")
            .setLocalTargetQ(10);
        // WHEN
        Generator generator = generatorAdder.add();
        // THEN
        assertEquals(10, generator.getLocalTargetQ());
        assertFalse(generator.isRegulating());
    }

    @Test
    public void testMissingVoltageRegulationWithMissingTargetQ() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("missingVoltageRegulation");
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, generatorAdder::add);
        // THEN
        assertEquals("Generator 'missingVoltageRegulation': invalid value (NaN) for localTargetQ (voltageRegulation is not set)", validationException.getMessage());
    }

    // Cases Regulating True, Terminal NUll, Mode VOLTAGE

    @Test
    public void testGeneratorOk() {
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
    public void testGeneratorErrorLocalTargetVMissing() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("LocalVoltageTargetV_missing");
        generatorAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .add();
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, generatorAdder::add);
        // THEN
        assertEquals("Generator 'LocalVoltageTargetV_missing': invalid value (NaN) for localTargetV (voltageRegulation is set with VOLTAGE mode and regulating true and the terminal is unset)",
            validationException.getMessage());
    }

    @Test
    public void testGeneratorErrorLocalTargetVMissingTargetQPresent() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("LocalVoltageTargetV_missing");
        generatorAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .add()
            .setLocalTargetQ(10);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, generatorAdder::add);
        // THEN
        assertEquals("Generator 'LocalVoltageTargetV_missing': invalid value (NaN) for localTargetV (voltageRegulation is set with VOLTAGE mode and regulating true and the terminal is unset)",
            validationException.getMessage());
    }

    @Test
    public void testGeneratorErrorLocalTargetVMissingTargetValuePresent() {
        // GIVEN
        VoltageRegulationAdder<GeneratorAdder> adder = newGeneratorAdder("LocalVoltageTargetV_missing").newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(240);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, adder::add);
        // THEN
        assertEquals("Generator 'LocalVoltageTargetV_missing': Invalid value for voltageRegulation.targetValue, expected NaN when a terminal is not set", validationException.getMessage());
    }

    @Test
    public void testGeneratorErrorTargetValuePresent() {
        // GIVEN
        VoltageRegulationAdder<GeneratorAdder> adder = newGeneratorAdder("ErrorTargetValuePresent_when_terminal_absent")
            .setLocalTargetV(24)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(240);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, adder::add);
        // THEN
        assertEquals("Generator 'ErrorTargetValuePresent_when_terminal_absent': Invalid value for voltageRegulation.targetValue, expected NaN when a terminal is not set",
            validationException.getMessage());
    }

    // Cases Regulating True, Terminal present, Mode VOLTAGE

    @Test
    public void testGeneratorRemoteVoltageRegulatingOk() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("OK_Remote_Voltage");
        VoltageRegulationAdder<GeneratorAdder> voltageRegulationAdder = generatorAdder
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(remoteTerminal)
            .withTargetValue(240);
        // WHEN
        Generator generator = voltageRegulationAdder.add()
            .add();
        // THEN
        VoltageRegulation voltageRegulation = generator.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertEquals(240, voltageRegulation.getTargetValue());
        assertEquals(240, generator.getRegulatingTargetV());
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertEquals(remoteTerminal, voltageRegulation.getTerminal());
        assertTrue(voltageRegulation.isWithTerminal());
        assertTrue(generator.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertTrue(generator.isRemoteRegulating());
    }

    @Test
    public void testGeneratorRemoteVoltageRegulatingErrorMissingTargetValue() {
        // GIVEN
        VoltageRegulationAdder<GeneratorAdder> adder = newGeneratorAdder("Error_Remote_Voltage_Missing_targetValue")
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(remoteTerminal);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, adder::add);
        // THEN
        assertEquals("Generator 'Error_Remote_Voltage_Missing_targetValue': Undefined value for voltageRegulation.targetValue, expected defined value when a terminal is set",
            validationException.getMessage());
    }

    // Cases Regulating false, Terminal NUll, Mode VOLTAGE

    @Test
    public void testGeneratorLocalVoltageRegulatingOffOk() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("OK_Local_Voltage_OFF");
        VoltageRegulationAdder<GeneratorAdder> voltageRegulationAdder = generatorAdder
            .setLocalTargetQ(10)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false);
        // WHEN
        Generator generator = voltageRegulationAdder.add()
            .add();
        // THEN
        VoltageRegulation voltageRegulation = generator.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertEquals(10, generator.getLocalTargetQ());
        assertTrue(Double.isNaN(voltageRegulation.getTargetValue()));
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertFalse(voltageRegulation.isWithTerminal());
        assertFalse(generator.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertTrue(generator.isWithMode(RegulationMode.VOLTAGE));
        assertFalse(generator.isRemoteRegulating());
    }

    @Test
    public void testGeneratorLocalVoltageRegulatingOffErrorMissingTargetQ() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("Error_Local_Voltage_OFF_Missing_TargetQ");
        generatorAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false)
            .add();
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, generatorAdder::add);
        // THEN
        assertEquals("Generator 'Error_Local_Voltage_OFF_Missing_TargetQ': invalid value (NaN) for localTargetQ (voltageRegulation is set with regulating false)", validationException.getMessage());
    }

    // Cases Regulating false, Terminal present, Mode VOLTAGE

    @Test
    public void testGeneratorRemoteVoltageRegulatingOffOk() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("OK_Remote_Voltage_OFF");
        VoltageRegulationAdder<GeneratorAdder> voltageRegulationAdder = generatorAdder
            .setLocalTargetQ(10)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(220)
            .withTerminal(remoteTerminal)
            .withRegulating(false);
        // WHEN
        Generator generator = voltageRegulationAdder.add()
            .add();
        // THEN
        VoltageRegulation voltageRegulation = generator.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertEquals(10, generator.getLocalTargetQ());
        assertEquals(220, voltageRegulation.getTargetValue());
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertTrue(voltageRegulation.isWithTerminal());
        assertFalse(generator.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertTrue(generator.isWithMode(RegulationMode.VOLTAGE));
        assertTrue(generator.isRemoteRegulating());
    }

    @Test
    public void testGeneratorRemoteVoltageRegulatingOffErrorMissingTargetQ() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("Error_Remote_Voltage_OFF_Missing_TargetQ");
        generatorAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(220)
            .withTerminal(remoteTerminal)
            .withRegulating(false)
            .add();
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, generatorAdder::add);
        // THEN
        assertEquals("Generator 'Error_Remote_Voltage_OFF_Missing_TargetQ': invalid value (NaN) for localTargetQ (voltageRegulation is set with regulating false)", validationException.getMessage());
    }

    // RemoveTerminal
    @Test
    public void testRemoveTerminalOnMultiVariantThrowException() {
        // GIVEN
        GeneratorAdder generatorAdder = newGeneratorAdder("Error_removeTerminal_multiVariant_Missing_LocalTargetV");
        int targetValue = 220;
        Generator generator = generatorAdder
            .setLocalTargetV(25)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(targetValue)
            .withTerminal(remoteTerminal)
            .withRegulating(true)
            .add()
            .add();
        VoltageRegulation voltageRegulation = generator.getVoltageRegulation();
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";
        network.getVariantManager().cloneVariant(INITIAL_VARIANT_ID, variant1);
        network.getVariantManager().setWorkingVariant(variant1);
        generator.setLocalTargetV(Double.NaN);
        network.getVariantManager().cloneVariant(variant1, variant2);
        network.getVariantManager().cloneVariant(variant1, variant3);
        network.getVariantManager().setWorkingVariant(INITIAL_VARIANT_ID);
        // WHEN
        PowsyblException powsyblException = assertThrows(PowsyblException.class, () -> voltageRegulation.setTerminal(null, Double.NaN));
        // THEN
        String expectedMessage = "Generator 'Error_removeTerminal_multiVariant_Missing_LocalTargetV': " +
            "Cannot set terminal when there are multiple variants";
        assertEquals(expectedMessage, powsyblException.getMessage());
        network.getVariantManager().getVariantIds().forEach(variantId -> {
            network.getVariantManager().setWorkingVariant(variantId);
            assertEquals(remoteTerminal, generator.getVoltageRegulation().getTerminal());
            assertEquals(targetValue, generator.getVoltageRegulation().getTargetValue());
            assertEquals(targetValue, generator.getRegulatingTargetV());
        });
    }

    @Test
    public void shouldChangeTerminal() {
        this.changeTerminalTest(newGeneratorAdder("changeTerminal").setLocalTargetQ(15.0).add());
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

    @Test
    public void testMergeWithTerminalInMultiVariant() {
        Generator gen = newGeneratorAdder("gen1")
                .setLocalTargetQ(15.0)
                .setLocalTargetV(110.0)
                .add();

        String initialVariantId = network.getVariantManager().getWorkingVariantId();
        String other = "Other";
        network.getVariantManager().cloneVariant(initialVariantId, other);
        network.getVariantManager().setWorkingVariant(other);

        // Creating a VoltageRegulation object with a terminal could be considered as changing the terminal.
        // This is not allowed in multi-variant mode.
        VoltageRegulationBuilder builder = gen.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTargetValue(120)
                .withTerminal(gen.getTerminal())
                .withRegulating(true);
        PowsyblException powsyblException = assertThrows(PowsyblException.class, builder::build);
        assertEquals("Generator 'gen1': Cannot set terminal when there are multiple variants",
                powsyblException.getMessage());

        // But it must be possible to create a voltage regulation in multi-variant mode if the terminal is not changed.
        builder = gen.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(true);
        assertDoesNotThrow(builder::build);
    }

    @Test
    public void testCreateVoltageRegulationInMultiVariant() {
        Generator otherGen = network.getGenerator("GEN");
        Generator gen = newGeneratorAdder("gen3")
                .setLocalTargetQ(15.0)
                .setLocalTargetV(110.0)
                .newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .withTargetValue(120)
                    .withTerminal(otherGen.getTerminal())
                    .withRegulating(true)
                    .add()
                .add();

        String initialVariantId = network.getVariantManager().getWorkingVariantId();
        String other = "Other";
        network.getVariantManager().cloneVariant(initialVariantId, other);
        network.getVariantManager().setWorkingVariant(other);

        // Setting the voltage regulation with the same terminal is allowed
        VoltageRegulationBuilder builder = gen.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTargetValue(130)
                .withTerminal(otherGen.getTerminal())
                .withRegulating(true);

        assertDoesNotThrow(builder::build);

        // But it is not allowed to change the terminal (multi-variant)
        builder = gen.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTargetValue(110)
                .withTerminal(gen.getTerminal())
                .withRegulating(true);

        PowsyblException powsyblException = assertThrows(PowsyblException.class, builder::build);
        assertEquals("Generator 'gen3': Cannot change terminal when there are multiple variants",
                powsyblException.getMessage());
    }

    private Generator createGenerator(DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator) {
        GeneratorAdder generatorAdder = newGeneratorAdder(dataVoltageRegulationHolderCreator.id())
            .setLocalTargetV(dataVoltageRegulationHolderCreator.localTargetV())
            .setLocalTargetQ(dataVoltageRegulationHolderCreator.localTargetQ());
        if (dataVoltageRegulationHolderCreator.mode() != null) {
            generatorAdder.newVoltageRegulation()
                .withRegulating(dataVoltageRegulationHolderCreator.regulating())
                .withMode(dataVoltageRegulationHolderCreator.mode())
                .withTargetValue(dataVoltageRegulationHolderCreator.targetValue())
                .withTerminal(dataVoltageRegulationHolderCreator.remoteTerminal() ? remoteTerminal : null)
                .add();
        }
        return generatorAdder.add();
    }

    private static Stream<Arguments> provideGeneratorRegulating() {
        DataVoltageRegulationHolderCreator regulatingLocalVoltage = new DataVoltageRegulationHolderCreator("regulatingLocalVoltage",
            RegulationMode.VOLTAGE,
            false,
            Double.NaN,
            24.5,
            Double.NaN,
            true);
        DataVoltageRegulationHolderCreator regulatingLocalVoltageWithTargetQ = new DataVoltageRegulationHolderCreator("regulatingLocalVoltageWithTargetQ",
            RegulationMode.VOLTAGE,
            false,
            Double.NaN,
            24.5,
            10,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteVoltage = new DataVoltageRegulationHolderCreator("regulatingRemoteVoltage",
            RegulationMode.VOLTAGE,
            true,
            400,
            Double.NaN,
            Double.NaN,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteVoltageWithLocalTargetV = new DataVoltageRegulationHolderCreator("regulatingRemoteVoltageWithLocalTargetV",
            RegulationMode.VOLTAGE,
            true,
            400,
            24.5,
            Double.NaN,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteVoltageWithLocalTargetVAndTargetQ = new DataVoltageRegulationHolderCreator("regulatingRemoteVoltageWithLocalTargetVAndTargetQ",
            RegulationMode.VOLTAGE,
            true,
            400,
            24.5,
            10,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteVoltageWithTargetQ = new DataVoltageRegulationHolderCreator("regulatingRemoteVoltageWithTargetQ",
            RegulationMode.VOLTAGE,
            true,
            400,
            Double.NaN,
            10,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteReactiveP = new DataVoltageRegulationHolderCreator("regulatingRemoteReactiveP",
            RegulationMode.REACTIVE_POWER,
            true,
            100,
            Double.NaN,
            Double.NaN,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteReactivePWithLocalTargetV = new DataVoltageRegulationHolderCreator("regulatingRemoteReactivePWithLocalTargetV",
            RegulationMode.REACTIVE_POWER,
            true,
            100,
            24.5,
            Double.NaN,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteReactivePWithLocalTargetVAndTargetQ = new DataVoltageRegulationHolderCreator("regulatingRemoteReactivePWithLocalTargetVAndTargetQ",
            RegulationMode.REACTIVE_POWER,
            true,
            100,
            24.5,
            10,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteReactivePWithTargetQ = new DataVoltageRegulationHolderCreator("regulatingRemoteReactivePWithTargetQ",
            RegulationMode.REACTIVE_POWER,
            true,
            100,
            Double.NaN,
            10,
            true);
        return Stream.of(
            addArgumentSet(regulatingLocalVoltage, true),
            addArgumentSet(regulatingLocalVoltageWithTargetQ, false),
            addArgumentSet(regulatingRemoteVoltage, true),
            addArgumentSet(regulatingRemoteVoltageWithLocalTargetV, true),
            addArgumentSet(regulatingRemoteVoltageWithLocalTargetVAndTargetQ, false),
            addArgumentSet(regulatingRemoteVoltageWithTargetQ, false),
            addArgumentSet(regulatingRemoteReactiveP, true),
            addArgumentSet(regulatingRemoteReactivePWithLocalTargetV, true),
            addArgumentSet(regulatingRemoteReactivePWithLocalTargetVAndTargetQ, false),
            addArgumentSet(regulatingRemoteReactivePWithTargetQ, false)
        );
    }

    private static Arguments.@NonNull ArgumentSet addArgumentSet(DataVoltageRegulationHolderCreator regulatingLocalVoltage, boolean withValidationError) {
        String validationError = "Generator '%s': invalid value (NaN) for localTargetQ (voltageRegulation is set with regulating false)";
        return argumentSet(regulatingLocalVoltage.id(),
            regulatingLocalVoltage,
            withValidationError ? String.format(validationError, regulatingLocalVoltage.id()) : null);
    }

}
