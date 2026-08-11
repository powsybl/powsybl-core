/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorAdder;
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

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public abstract class AbstractVoltageRegulationOnShuntCompensatorTest {

    private VoltageLevel voltageLevel;
    private Terminal remoteTerminal;
    private Network network;

    @BeforeEach
    void initNetwork() {
        network = BatteryNetworkFactory.create();
        voltageLevel = network.getVoltageLevel("VLGEN");
        remoteTerminal = network.getBattery("BAT").getTerminal();
    }

    @Test
    void shouldUnregisterTerminalOnRemoveShuntCompensatorWithRemoteVoltageRegulation() {
        String shuntCompensatorId = "removedShuntCompensator";
        DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator = new DataVoltageRegulationHolderCreator(shuntCompensatorId,
            RegulationMode.VOLTAGE,
            true,
            220,
            24.5,
            Double.NaN,
            10,
            Double.NaN,
            true);
        ShuntCompensator shuntCompensator = createShuntCompensator(dataVoltageRegulationHolderCreator);
        assertEquals(1, remoteTerminal.getReferrers().size());
        shuntCompensator.remove();
        assertNull(network.getShuntCompensator(shuntCompensatorId));
        assertEquals(0, remoteTerminal.getReferrers().size());
    }

    @Test
    void shouldRemoveShuntCompensatorOnRemoveShuntCompensatorWithoutVoltageRegulation() {
        String shuntCompensatorId = "removedShuntCompensator";
        ShuntCompensator shuntCompensator = newShuntCompensatorAdder(shuntCompensatorId).add();
        assertEquals(0, remoteTerminal.getReferrers().size());
        shuntCompensator.remove();
        assertNull(network.getShuntCompensator(shuntCompensatorId));
        assertEquals(0, remoteTerminal.getReferrers().size());
    }

    // Cases shuntCompensator regulating
    @ParameterizedTest(name = "{argumentSetName}")
    @MethodSource("provideShuntCompensatorRegulating")
    void testShuntCompensatorRegulating(DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator, String validationErrorOnRegulatingFalse) {
        ShuntCompensator shuntCompensator = createShuntCompensator(dataVoltageRegulationHolderCreator);
        assertTrue(shuntCompensator.isRegulating());
        VoltageRegulation voltageRegulation = shuntCompensator.getVoltageRegulation();
        if (validationErrorOnRegulatingFalse != null) {
            ValidationException validationException = assertThrows(ValidationException.class, () -> voltageRegulation.setRegulating(false));
            assertEquals(validationErrorOnRegulatingFalse, validationException.getMessage());
        } else {
            voltageRegulation.setRegulating(false);
            assertFalse(shuntCompensator.isRegulating());
        }
    }

    // Cases missing VoltageRegulation
    @Test
    void testMissingVoltageRegulationOk() {
        // GIVEN
        ShuntCompensatorAdder shuntCompensatorAdder = newShuntCompensatorAdder("missingVoltageRegulation")
            .setLocalTargetQ(10);
        // WHEN
        ShuntCompensator shuntCompensator = shuntCompensatorAdder.add();
        // THEN
        assertTrue(Double.isNaN(shuntCompensator.getLocalTargetQ()));
        assertFalse(shuntCompensator.isRegulating());
    }

    // Cases Regulating True, Terminal NUll, Mode VOLTAGE

    @Test
    void testShuntCompensatorOk() {
        // GIVEN
        ShuntCompensatorAdder shuntCompensatorAdder = newShuntCompensatorAdder("ErrorTargetValuePresent_when_terminal_absent");
        shuntCompensatorAdder
            .setLocalTargetV(24)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetDeadband(10)
            .add();
        // WHEN
        ShuntCompensator shuntCompensator = shuntCompensatorAdder.add();
        // THEN
        assertEquals(24, shuntCompensator.getLocalTargetV());
        assertTrue(shuntCompensator.isRegulating());
    }

    @Test
    void testShuntCompensatorErrorLocalTargetVMissing() {
        // GIVEN
        ShuntCompensatorAdder shuntCompensatorAdder = newShuntCompensatorAdder("LocalVoltageTargetV_missing");
        shuntCompensatorAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetDeadband(10)
            .add();
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, shuntCompensatorAdder::add);
        // THEN
        assertEquals("Shunt compensator 'LocalVoltageTargetV_missing': invalid value (NaN) for localTargetV (voltageRegulation is set with VOLTAGE mode and regulating true and the terminal is unset)",
            validationException.getMessage());
    }

    @Test
    void testShuntCompensatorErrorLocalTargetVMissingTargetQPresent() {
        // GIVEN
        ShuntCompensatorAdder shuntCompensatorAdder = newShuntCompensatorAdder("LocalVoltageTargetV_missing");
        shuntCompensatorAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetDeadband(10)
            .add()
            .setLocalTargetQ(10);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, shuntCompensatorAdder::add);
        // THEN
        assertEquals("Shunt compensator 'LocalVoltageTargetV_missing': invalid value (NaN) for localTargetV (voltageRegulation is set with VOLTAGE mode and regulating true and the terminal is unset)",
            validationException.getMessage());
    }

    @Test
    void testShuntCompensatorErrorLocalTargetVMissingTargetValuePresent() {
        // GIVEN
        VoltageRegulationAdder<ShuntCompensatorAdder> adder = newShuntCompensatorAdder("LocalVoltageTargetV_missing").newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(240)
            .withTargetDeadband(10);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, adder::add);
        // THEN
        assertEquals("Shunt compensator 'LocalVoltageTargetV_missing': Invalid value for voltageRegulation.targetValue, expected NaN when a terminal is not set", validationException.getMessage());
    }

    @Test
    void testShuntCompensatorErrorTargetValuePresent() {
        // GIVEN
        VoltageRegulationAdder<ShuntCompensatorAdder> adder = newShuntCompensatorAdder("ErrorTargetValuePresent_when_terminal_absent")
            .setLocalTargetV(24)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(240)
            .withTargetDeadband(10);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, adder::add);
        // THEN
        assertEquals("Shunt compensator 'ErrorTargetValuePresent_when_terminal_absent': Invalid value for voltageRegulation.targetValue, expected NaN when a terminal is not set",
            validationException.getMessage());
    }

    // Cases Regulating True, Terminal present, Mode VOLTAGE

    @Test
    void testShuntCompensatorRemoteVoltageRegulatingOk() {
        // GIVEN
        ShuntCompensatorAdder shuntCompensatorAdder = newShuntCompensatorAdder("OK_Remote_Voltage");
        VoltageRegulationAdder<ShuntCompensatorAdder> voltageRegulationAdder = shuntCompensatorAdder
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(remoteTerminal)
            .withTargetValue(240)
            .withTargetDeadband(10);
        // WHEN
        ShuntCompensator shuntCompensator = voltageRegulationAdder.add()
            .add();
        // THEN
        VoltageRegulation voltageRegulation = shuntCompensator.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertEquals(240, voltageRegulation.getTargetValue());
        assertEquals(10, voltageRegulation.getTargetDeadband());
        assertEquals(240, shuntCompensator.getRegulatingTargetV());
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertEquals(remoteTerminal, voltageRegulation.getTerminal());
        assertTrue(voltageRegulation.isWithTerminal());
        assertTrue(shuntCompensator.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertTrue(shuntCompensator.isRemoteRegulating());
    }

    @Test
    void testShuntCompensatorRemoteVoltageRegulatingErrorMissingTargetValue() {
        // GIVEN
        VoltageRegulationAdder<ShuntCompensatorAdder> adder = newShuntCompensatorAdder("Error_Remote_Voltage_Missing_targetValue")
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(remoteTerminal)
            .withTargetDeadband(10);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, adder::add);
        // THEN
        assertEquals("Shunt compensator 'Error_Remote_Voltage_Missing_targetValue': Undefined value for voltageRegulation.targetValue, expected defined value when a terminal is set",
            validationException.getMessage());
    }

    // Cases Regulating false, Terminal NUll, Mode VOLTAGE

    @Test
    void testShuntCompensatorLocalVoltageRegulatingOffOk() {
        // GIVEN
        ShuntCompensatorAdder shuntCompensatorAdder = newShuntCompensatorAdder("OK_Local_Voltage_OFF");
        VoltageRegulationAdder<ShuntCompensatorAdder> voltageRegulationAdder = shuntCompensatorAdder
            .setLocalTargetQ(10)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false);
        // WHEN
        ShuntCompensator shuntCompensator = voltageRegulationAdder.add()
            .add();
        // THEN
        VoltageRegulation voltageRegulation = shuntCompensator.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertTrue(Double.isNaN(shuntCompensator.getLocalTargetQ()));
        assertTrue(Double.isNaN(voltageRegulation.getTargetValue()));
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertFalse(voltageRegulation.isWithTerminal());
        assertFalse(shuntCompensator.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertTrue(shuntCompensator.isWithMode(RegulationMode.VOLTAGE));
        assertFalse(shuntCompensator.isRemoteRegulating());
    }

    @Test
    void testShuntCompensatorLocalVoltageRegulatingOffNoError() {
        // GIVEN
        ShuntCompensatorAdder shuntCompensatorAdder = newShuntCompensatorAdder("Ok_Local_Voltage_OFF_Missing_TargetQ");
        shuntCompensatorAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false)
            .add();
        // WHEN
        ShuntCompensator shuntCompensator = shuntCompensatorAdder.add();
        // THEN
        assertNotNull(shuntCompensator);
        assertEquals("Ok_Local_Voltage_OFF_Missing_TargetQ", shuntCompensator.getId());
        VoltageRegulation voltageRegulation = shuntCompensator.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertTrue(shuntCompensator.isWithMode(RegulationMode.VOLTAGE));
        assertFalse(shuntCompensator.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertFalse(shuntCompensator.isRegulating());
        assertTrue(Double.isNaN(shuntCompensator.getLocalTargetQ()));
    }

    // Cases Regulating false, Terminal present, Mode VOLTAGE

    @Test
    void testShuntCompensatorRemoteVoltageRegulatingOffOk() {
        // GIVEN
        ShuntCompensatorAdder shuntCompensatorAdder = newShuntCompensatorAdder("OK_Remote_Voltage_OFF");
        VoltageRegulationAdder<ShuntCompensatorAdder> voltageRegulationAdder = shuntCompensatorAdder
            .setLocalTargetQ(10)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(220)
            .withTerminal(remoteTerminal)
            .withRegulating(false);
        // WHEN
        ShuntCompensator shuntCompensator = voltageRegulationAdder.add()
            .add();
        // THEN
        VoltageRegulation voltageRegulation = shuntCompensator.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertTrue(Double.isNaN(shuntCompensator.getLocalTargetQ()));
        assertEquals(220, voltageRegulation.getTargetValue());
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertTrue(voltageRegulation.isWithTerminal());
        assertFalse(shuntCompensator.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertTrue(shuntCompensator.isWithMode(RegulationMode.VOLTAGE));
        assertTrue(shuntCompensator.isRemoteRegulating());
    }

    @Test
    void testShuntCompensatorRemoteVoltageRegulatingOffOkWithNoLocalTargetQ() {
        // GIVEN
        ShuntCompensatorAdder shuntCompensatorAdder = newShuntCompensatorAdder("Ok_Remote_Voltage_OFF_Missing_TargetQ");
        shuntCompensatorAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(220)
            .withTerminal(remoteTerminal)
            .withRegulating(false)
            .add();
        // WHEN
        ShuntCompensator shuntCompensator = shuntCompensatorAdder.add();
        // THEN
        VoltageRegulation voltageRegulation = shuntCompensator.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertTrue(Double.isNaN(shuntCompensator.getLocalTargetQ()));
        assertEquals(220, voltageRegulation.getTargetValue());
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertTrue(voltageRegulation.isWithTerminal());
        assertFalse(shuntCompensator.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertTrue(shuntCompensator.isWithMode(RegulationMode.VOLTAGE));
        assertTrue(shuntCompensator.isRemoteRegulating());
    }

    // RemoveTerminal
    @Test
    void testRemoveTerminalOnMultiVariantWithLocalTargetV() {
        // GIVEN
        ShuntCompensatorAdder shuntCompensatorAdder = newShuntCompensatorAdder("OK_removeTerminal_multiVariant_With_LocalTargetV");
        int localTargetV = 25;
        ShuntCompensator shuntCompensator = shuntCompensatorAdder
            .setLocalTargetV(localTargetV)
            .newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTargetValue(220)
                .withTerminal(remoteTerminal)
                .withRegulating(true)
                .withTargetDeadband(10)
                .add()
            .add();
        VoltageRegulation voltageRegulation = shuntCompensator.getVoltageRegulation();
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";
        network.getVariantManager().cloneVariant(INITIAL_VARIANT_ID, variant1);
        network.getVariantManager().cloneVariant(INITIAL_VARIANT_ID, variant2);
        network.getVariantManager().cloneVariant(INITIAL_VARIANT_ID, variant3);
        // WHEN
        voltageRegulation.setTerminal(null, Double.NaN);
        // THEN
        network.getVariantManager().getVariantIds().forEach(variantId -> {
            network.getVariantManager().setWorkingVariant(variantId);
            assertNull(shuntCompensator.getVoltageRegulation().getTerminal());
            assertTrue(Double.isNaN(shuntCompensator.getVoltageRegulation().getTargetValue()));
            assertEquals(localTargetV, shuntCompensator.getRegulatingTargetV());
        });
    }

    @Test
    void testRemoveTerminalOnMultiVariantMissingLocalTargetV() {
        // GIVEN
        ShuntCompensatorAdder shuntCompensatorAdder = newShuntCompensatorAdder("Error_removeTerminal_multiVariant_Missing_LocalTargetV");
        int targetValue = 220;
        ShuntCompensator shuntCompensator = shuntCompensatorAdder
            .setLocalTargetV(25)
            .newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTargetValue(targetValue)
                .withTerminal(remoteTerminal)
                .withRegulating(true)
                .withTargetDeadband(10)
                .add()
            .add();
        VoltageRegulation voltageRegulation = shuntCompensator.getVoltageRegulation();
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";
        network.getVariantManager().cloneVariant(INITIAL_VARIANT_ID, variant1);
        network.getVariantManager().setWorkingVariant(variant1);
        shuntCompensator.setLocalTargetV(Double.NaN);
        network.getVariantManager().cloneVariant(variant1, variant2);
        network.getVariantManager().cloneVariant(variant1, variant3);
        network.getVariantManager().setWorkingVariant(INITIAL_VARIANT_ID);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, () -> voltageRegulation.setTerminal(null, Double.NaN));
        // THEN
        String expectedMessage = "Shunt compensator 'Error_removeTerminal_multiVariant_Missing_LocalTargetV': " +
            "Trying to remove the regulating terminal from the voltageRegulation of Error_removeTerminal_multiVariant_Missing_LocalTargetV " +
            "but the next variants are missing local target values [variant2, variant1, variant3]";
        assertEquals(expectedMessage, validationException.getMessage());
        network.getVariantManager().getVariantIds().forEach(variantId -> {
            network.getVariantManager().setWorkingVariant(variantId);
            assertEquals(remoteTerminal, shuntCompensator.getVoltageRegulation().getTerminal());
            assertEquals(targetValue, shuntCompensator.getVoltageRegulation().getTargetValue());
            assertEquals(targetValue, shuntCompensator.getRegulatingTargetV());
        });
    }

    private ShuntCompensatorAdder newShuntCompensatorAdder(String id) {
        return voltageLevel.newShuntCompensator()
            .setId(id)
            .setBus("NGEN")
            .setConnectableBus("NGEN")
            .setSectionCount(1)
            .newLinearModel()
                .setBPerSection(5.0)
                .setMaximumSectionCount(10)
                .add();
    }

    private ShuntCompensator createShuntCompensator(DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator) {
        ShuntCompensatorAdder shuntCompensatorAdder = newShuntCompensatorAdder(dataVoltageRegulationHolderCreator.id())
            .setLocalTargetV(dataVoltageRegulationHolderCreator.localTargetV())
            .setLocalTargetQ(dataVoltageRegulationHolderCreator.localTargetQ());
        if (dataVoltageRegulationHolderCreator.mode() != null) {
            shuntCompensatorAdder.newVoltageRegulation()
                .withRegulating(dataVoltageRegulationHolderCreator.regulating())
                .withMode(dataVoltageRegulationHolderCreator.mode())
                .withTargetValue(dataVoltageRegulationHolderCreator.targetValue())
                .withTerminal(dataVoltageRegulationHolderCreator.remoteTerminal() ? remoteTerminal : null)
                .withTargetDeadband(dataVoltageRegulationHolderCreator.targetDeadband())
                .add();
        }
        return shuntCompensatorAdder.add();
    }

    private static Stream<Arguments> provideShuntCompensatorRegulating() {
        DataVoltageRegulationHolderCreator regulatingLocalVoltage = new DataVoltageRegulationHolderCreator("regulatingLocalVoltage",
            RegulationMode.VOLTAGE,
            false,
            Double.NaN,
            24.5,
            Double.NaN,
            10,
            Double.NaN,
            true);
        DataVoltageRegulationHolderCreator regulatingLocalVoltageWithTargetQ = new DataVoltageRegulationHolderCreator("regulatingLocalVoltageWithTargetQ",
            RegulationMode.VOLTAGE,
            false,
            Double.NaN,
            24.5,
            10,
            10,
            Double.NaN,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteVoltage = new DataVoltageRegulationHolderCreator("regulatingRemoteVoltage",
            RegulationMode.VOLTAGE,
            true,
            400,
            Double.NaN,
            Double.NaN,
            10,
            Double.NaN,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteVoltageWithLocalTargetV = new DataVoltageRegulationHolderCreator("regulatingRemoteVoltageWithLocalTargetV",
            RegulationMode.VOLTAGE,
            true,
            400,
            24.5,
            Double.NaN,
            10,
            Double.NaN,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteVoltageWithLocalTargetVAndTargetQ = new DataVoltageRegulationHolderCreator("regulatingRemoteVoltageWithLocalTargetVAndTargetQ",
            RegulationMode.VOLTAGE,
            true,
            400,
            24.5,
            10,
            10,
            Double.NaN,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteVoltageWithTargetQ = new DataVoltageRegulationHolderCreator("regulatingRemoteVoltageWithTargetQ",
            RegulationMode.VOLTAGE,
            true,
            400,
            Double.NaN,
            10,
            10,
            Double.NaN,
            true);
        return Stream.of(
            addArgumentSet(regulatingLocalVoltage),
            addArgumentSet(regulatingLocalVoltageWithTargetQ),
            addArgumentSet(regulatingRemoteVoltage),
            addArgumentSet(regulatingRemoteVoltageWithLocalTargetV),
            addArgumentSet(regulatingRemoteVoltageWithLocalTargetVAndTargetQ),
            addArgumentSet(regulatingRemoteVoltageWithTargetQ)
        );
    }

    private static Arguments.@NonNull ArgumentSet addArgumentSet(DataVoltageRegulationHolderCreator regulatingLocalVoltage) {
        return argumentSet(regulatingLocalVoltage.id(),
            regulatingLocalVoltage,
            null);
    }
}
