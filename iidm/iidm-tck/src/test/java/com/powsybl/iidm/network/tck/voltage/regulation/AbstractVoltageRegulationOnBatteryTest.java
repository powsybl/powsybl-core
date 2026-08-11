/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.BatteryAdder;
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
public abstract class AbstractVoltageRegulationOnBatteryTest {

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
    void shouldUnregisterTerminalOnRemoveBatteryWithRemoteVoltageRegulation() {
        String batteryId = "removedBattery";
        DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator = new DataVoltageRegulationHolderCreator(batteryId,
            RegulationMode.VOLTAGE,
            true,
            220,
            24.5,
            Double.NaN,
            true);
        Battery battery = createBattery(dataVoltageRegulationHolderCreator);
        assertEquals(1, remoteTerminal.getReferrers().size());
        battery.remove();
        assertNull(network.getBattery(batteryId));
        assertEquals(0, remoteTerminal.getReferrers().size());
    }

    @Test
    void shouldRemoveBatteryOnRemoveBatteryWithoutVoltageRegulation() {
        String batteryId = "removedBattery";
        Battery battery = newBatteryAdder(batteryId).setLocalTargetQ(10).add();
        assertEquals(0, remoteTerminal.getReferrers().size());
        battery.remove();
        assertNull(network.getBattery(batteryId));
        assertEquals(0, remoteTerminal.getReferrers().size());
    }

    // Cases battery regulating
    @ParameterizedTest(name = "{argumentSetName}")
    @MethodSource("provideBatteryRegulating")
    void testBatteryRegulating(DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator, String validationErrorOnRegulatingFalse) {
        Battery battery = createBattery(dataVoltageRegulationHolderCreator);
        assertTrue(battery.isRegulating());
        VoltageRegulation voltageRegulation = battery.getVoltageRegulation();
        if (validationErrorOnRegulatingFalse != null) {
            ValidationException validationException = assertThrows(ValidationException.class, () -> voltageRegulation.setRegulating(false));
            assertEquals(validationErrorOnRegulatingFalse, validationException.getMessage());
        } else {
            voltageRegulation.setRegulating(false);
            assertFalse(battery.isRegulating());
        }
    }

    // Cases missing VoltageRegulation
    @Test
    void testMissingVoltageRegulationOk() {
        // GIVEN
        BatteryAdder batteryAdder = newBatteryAdder("missingVoltageRegulation")
            .setLocalTargetQ(10);
        // WHEN
        Battery battery = batteryAdder.add();
        // THEN
        assertEquals(10, battery.getLocalTargetQ());
        assertFalse(battery.isRegulating());
    }

    @Test
    void testMissingVoltageRegulationWithMissingTargetQ() {
        // GIVEN
        BatteryAdder batteryAdder = newBatteryAdder("missingVoltageRegulation");
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, batteryAdder::add);
        // THEN
        assertEquals("Battery 'missingVoltageRegulation': invalid value (NaN) for localTargetQ (voltageRegulation is not set)", validationException.getMessage());
    }

    // Cases Regulating True, Terminal NUll, Mode VOLTAGE

    @Test
    void testBatteryOk() {
        // GIVEN
        BatteryAdder batteryAdder = newBatteryAdder("ErrorTargetValuePresent_when_terminal_absent");
        batteryAdder
            .setLocalTargetV(24)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .add();
        // WHEN
        Battery battery = batteryAdder.add();
        // THEN
        assertEquals(24, battery.getLocalTargetV());
        assertTrue(battery.isRegulating());
    }

    @Test
    void testBatteryErrorLocalTargetVMissing() {
        // GIVEN
        BatteryAdder batteryAdder = newBatteryAdder("LocalVoltageTargetV_missing");
        batteryAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .add();
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, batteryAdder::add);
        // THEN
        assertEquals("Battery 'LocalVoltageTargetV_missing': invalid value (NaN) for localTargetV (voltageRegulation is set with VOLTAGE mode and regulating true and the terminal is unset)",
            validationException.getMessage());
    }

    @Test
    void testBatteryErrorLocalTargetVMissingTargetQPresent() {
        // GIVEN
        BatteryAdder batteryAdder = newBatteryAdder("LocalVoltageTargetV_missing");
        batteryAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .add()
            .setLocalTargetQ(10);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, batteryAdder::add);
        // THEN
        assertEquals("Battery 'LocalVoltageTargetV_missing': invalid value (NaN) for localTargetV (voltageRegulation is set with VOLTAGE mode and regulating true and the terminal is unset)",
            validationException.getMessage());
    }

    @Test
    void testBatteryErrorLocalTargetVMissingTargetValuePresent() {
        // GIVEN
        VoltageRegulationAdder<BatteryAdder> adder = newBatteryAdder("LocalVoltageTargetV_missing").newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(240);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, adder::add);
        // THEN
        assertEquals("Battery 'LocalVoltageTargetV_missing': Invalid value for voltageRegulation.targetValue, expected NaN when a terminal is not set", validationException.getMessage());
    }

    @Test
    void testBatteryErrorTargetValuePresent() {
        // GIVEN
        VoltageRegulationAdder<BatteryAdder> adder = newBatteryAdder("ErrorTargetValuePresent_when_terminal_absent")
            .setLocalTargetV(24)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(240);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, adder::add);
        // THEN
        assertEquals("Battery 'ErrorTargetValuePresent_when_terminal_absent': Invalid value for voltageRegulation.targetValue, expected NaN when a terminal is not set",
            validationException.getMessage());
    }

    // Cases Regulating True, Terminal present, Mode VOLTAGE

    @Test
    void testBatteryRemoteVoltageRegulatingOk() {
        // GIVEN
        BatteryAdder batteryAdder = newBatteryAdder("OK_Remote_Voltage");
        VoltageRegulationAdder<BatteryAdder> voltageRegulationAdder = batteryAdder
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(remoteTerminal)
            .withTargetValue(240);
        // WHEN
        Battery battery = voltageRegulationAdder.add()
            .add();
        // THEN
        VoltageRegulation voltageRegulation = battery.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertEquals(240, voltageRegulation.getTargetValue());
        assertEquals(240, battery.getRegulatingTargetV());
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertEquals(remoteTerminal, voltageRegulation.getTerminal());
        assertTrue(voltageRegulation.isWithTerminal());
        assertTrue(battery.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertTrue(battery.isRemoteRegulating());
    }

    @Test
    void testBatteryRemoteVoltageRegulatingErrorMissingTargetValue() {
        // GIVEN
        VoltageRegulationAdder<BatteryAdder> adder = newBatteryAdder("Error_Remote_Voltage_Missing_targetValue")
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(remoteTerminal);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, adder::add);
        // THEN
        assertEquals("Battery 'Error_Remote_Voltage_Missing_targetValue': Undefined value for voltageRegulation.targetValue, expected defined value when a terminal is set",
            validationException.getMessage());
    }

    // Cases Regulating false, Terminal NUll, Mode VOLTAGE

    @Test
    void testBatteryLocalVoltageRegulatingOffOk() {
        // GIVEN
        BatteryAdder batteryAdder = newBatteryAdder("OK_Local_Voltage_OFF");
        VoltageRegulationAdder<BatteryAdder> voltageRegulationAdder = batteryAdder
            .setLocalTargetQ(10)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false);
        // WHEN
        Battery battery = voltageRegulationAdder.add()
            .add();
        // THEN
        VoltageRegulation voltageRegulation = battery.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertEquals(10, battery.getLocalTargetQ());
        assertTrue(Double.isNaN(voltageRegulation.getTargetValue()));
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertFalse(voltageRegulation.isWithTerminal());
        assertFalse(battery.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertTrue(battery.isWithMode(RegulationMode.VOLTAGE));
        assertFalse(battery.isRemoteRegulating());
    }

    @Test
    void testBatteryLocalVoltageRegulatingOffErrorMissingTargetQ() {
        // GIVEN
        BatteryAdder batteryAdder = newBatteryAdder("Error_Local_Voltage_OFF_Missing_TargetQ");
        batteryAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false)
            .add();
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, batteryAdder::add);
        // THEN
        assertEquals("Battery 'Error_Local_Voltage_OFF_Missing_TargetQ': invalid value (NaN) for localTargetQ (voltageRegulation is set with regulating false)", validationException.getMessage());
    }

    // Cases Regulating false, Terminal present, Mode VOLTAGE

    @Test
    void testBatteryRemoteVoltageRegulatingOffOk() {
        // GIVEN
        BatteryAdder batteryAdder = newBatteryAdder("OK_Remote_Voltage_OFF");
        VoltageRegulationAdder<BatteryAdder> voltageRegulationAdder = batteryAdder
            .setLocalTargetQ(10)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(220)
            .withTerminal(remoteTerminal)
            .withRegulating(false);
        // WHEN
        Battery battery = voltageRegulationAdder.add()
            .add();
        // THEN
        VoltageRegulation voltageRegulation = battery.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertEquals(10, battery.getLocalTargetQ());
        assertEquals(220, voltageRegulation.getTargetValue());
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertTrue(voltageRegulation.isWithTerminal());
        assertFalse(battery.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertTrue(battery.isWithMode(RegulationMode.VOLTAGE));
        assertTrue(battery.isRemoteRegulating());
    }

    @Test
    void testBatteryRemoteVoltageRegulatingOffErrorMissingTargetQ() {
        // GIVEN
        BatteryAdder batteryAdder = newBatteryAdder("Error_Remote_Voltage_OFF_Missing_TargetQ");
        batteryAdder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(220)
            .withTerminal(remoteTerminal)
            .withRegulating(false)
            .add();
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, batteryAdder::add);
        // THEN
        assertEquals("Battery 'Error_Remote_Voltage_OFF_Missing_TargetQ': invalid value (NaN) for localTargetQ (voltageRegulation is set with regulating false)", validationException.getMessage());
    }

    // RemoveTerminal
    @Test
    void testRemoveTerminalOnMultiVariantWithLocalTargetV() {
        // GIVEN
        BatteryAdder batteryAdder = newBatteryAdder("OK_removeTerminal_multiVariant_With_LocalTargetV");
        int localTargetV = 25;
        Battery battery = batteryAdder
            .setLocalTargetV(localTargetV)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(220)
            .withTerminal(remoteTerminal)
            .withRegulating(true)
            .add()
            .add();
        VoltageRegulation voltageRegulation = battery.getVoltageRegulation();
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
            assertNull(battery.getVoltageRegulation().getTerminal());
            assertTrue(Double.isNaN(battery.getVoltageRegulation().getTargetValue()));
            assertEquals(localTargetV, battery.getRegulatingTargetV());
        });
    }

    @Test
    void testRemoveTerminalOnMultiVariantMissingLocalTargetV() {
        // GIVEN
        BatteryAdder batteryAdder = newBatteryAdder("Error_removeTerminal_multiVariant_Missing_LocalTargetV");
        int targetValue = 220;
        Battery battery = batteryAdder
            .setLocalTargetV(25)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(targetValue)
            .withTerminal(remoteTerminal)
            .withRegulating(true)
            .add()
            .add();
        VoltageRegulation voltageRegulation = battery.getVoltageRegulation();
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";
        network.getVariantManager().cloneVariant(INITIAL_VARIANT_ID, variant1);
        network.getVariantManager().setWorkingVariant(variant1);
        battery.setLocalTargetV(Double.NaN);
        network.getVariantManager().cloneVariant(variant1, variant2);
        network.getVariantManager().cloneVariant(variant1, variant3);
        network.getVariantManager().setWorkingVariant(INITIAL_VARIANT_ID);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, () -> voltageRegulation.setTerminal(null, Double.NaN));
        // THEN
        String expectedMessage = "Battery 'Error_removeTerminal_multiVariant_Missing_LocalTargetV': " +
            "Trying to remove the regulating terminal from the voltageRegulation of Error_removeTerminal_multiVariant_Missing_LocalTargetV " +
            "but the next variants are missing local target values [variant2, variant1, variant3]";
        assertEquals(expectedMessage, validationException.getMessage());
        network.getVariantManager().getVariantIds().forEach(variantId -> {
            network.getVariantManager().setWorkingVariant(variantId);
            assertEquals(remoteTerminal, battery.getVoltageRegulation().getTerminal());
            assertEquals(targetValue, battery.getVoltageRegulation().getTargetValue());
            assertEquals(targetValue, battery.getRegulatingTargetV());
        });
    }

    private BatteryAdder newBatteryAdder(String id) {
        return voltageLevel.newBattery()
            .setId(id)
            .setBus("NGEN")
            .setConnectableBus("NGEN")
            .setMinP(12.0)
            .setMaxP(120.0)
            .setTargetP(100.0);
    }

    private Battery createBattery(DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator) {
        BatteryAdder batteryAdder = newBatteryAdder(dataVoltageRegulationHolderCreator.id())
            .setLocalTargetV(dataVoltageRegulationHolderCreator.localTargetV())
            .setLocalTargetQ(dataVoltageRegulationHolderCreator.localTargetQ());
        if (dataVoltageRegulationHolderCreator.mode() != null) {
            batteryAdder.newVoltageRegulation()
                .withRegulating(dataVoltageRegulationHolderCreator.regulating())
                .withMode(dataVoltageRegulationHolderCreator.mode())
                .withTargetValue(dataVoltageRegulationHolderCreator.targetValue())
                .withTerminal(dataVoltageRegulationHolderCreator.remoteTerminal() ? remoteTerminal : null)
                .add();
        }
        return batteryAdder.add();
    }

    private static Stream<Arguments> provideBatteryRegulating() {
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
        String validationError = "Battery '%s': invalid value (NaN) for localTargetQ (voltageRegulation is set with regulating false)";
        return argumentSet(regulatingLocalVoltage.id(),
            regulatingLocalVoltage,
            withValidationError ? String.format(validationError, regulatingLocalVoltage.id()) : null);
    }
}
