/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.ValidationException;
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
import static com.powsybl.iidm.network.test.BatteryNetworkFactory.VLGEN;
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
public abstract class AbstractVoltageRegulationOnRatioTapChangerTest {

    private Terminal remoteTerminal;
    private Network network;
    private TwoWindingsTransformer twoWindingsTransformer;

    @BeforeEach
    void initNetwork() {
        network = BatteryNetworkFactory.create();
        twoWindingsTransformer = network.getSubstation("P1").newTwoWindingsTransformer()
            .setId("T1")
            .setVoltageLevel1(VLGEN)
            .setVoltageLevel2(VLGEN)
            .setConnectableBus1("NGEN")
            .setConnectableBus2("NGEN")
            .setR(1)
            .setX(2)
            .add();
        remoteTerminal = network.getBattery("BAT").getTerminal();
    }

    @Test
    void shouldUnregisterTerminalOnRemoveRatioTapChangerWithRemoteVoltageRegulation() {
        String ratioTapChangerId = "removedRatioTapChanger";
        DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator = new DataVoltageRegulationHolderCreator(ratioTapChangerId,
            RegulationMode.VOLTAGE,
            true,
            220,
            15,
            true);
        RatioTapChanger ratioTapChanger = createRatioTapChanger(dataVoltageRegulationHolderCreator);
        assertEquals(1, remoteTerminal.getReferrers().size());
        ratioTapChanger.remove();
        assertNull(twoWindingsTransformer.getRatioTapChanger());
        assertEquals(0, remoteTerminal.getReferrers().size());
    }

    @Test
    void shouldRemoveRatioTapChangerOnRemoveRatioTapChangerWithoutVoltageRegulation() {
        RatioTapChanger ratioTapChanger = newRatioTapChangerAdder(false).add();
        assertEquals(0, remoteTerminal.getReferrers().size());
        ratioTapChanger.remove();
        assertNull(twoWindingsTransformer.getRatioTapChanger());
        assertEquals(0, remoteTerminal.getReferrers().size());
    }

    // Cases ratioTapChanger regulating
    @ParameterizedTest(name = "{argumentSetName}")
    @MethodSource("provideRatioTapChangerRegulating")
    void testRatioTapChangerRegulating(DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator, String validationErrorOnRegulatingFalse) {
        RatioTapChanger ratioTapChanger = createRatioTapChanger(dataVoltageRegulationHolderCreator);
        assertTrue(ratioTapChanger.isRegulating());
        VoltageRegulation voltageRegulation = ratioTapChanger.getVoltageRegulation();
        if (validationErrorOnRegulatingFalse != null) {
            ValidationException validationException = assertThrows(ValidationException.class, () -> voltageRegulation.setRegulating(false));
            assertEquals(validationErrorOnRegulatingFalse, validationException.getMessage());
        } else {
            voltageRegulation.setRegulating(false);
            assertFalse(ratioTapChanger.isRegulating());
        }
    }

    // Cases missing VoltageRegulation
    @Test
    void testMissingVoltageRegulationOk() {
        // GIVEN
        RatioTapChangerAdder ratioTapChangerAdder = newRatioTapChangerAdder(false);
        // WHEN
        RatioTapChanger ratioTapChanger = ratioTapChangerAdder.add();
        // THEN
        assertTrue(Double.isNaN(ratioTapChanger.getRegulatingTargetQ()));
        assertTrue(Double.isNaN(ratioTapChanger.getRegulatingTargetV()));
        assertFalse(ratioTapChanger.isRegulating());
    }

    // Cases Regulating True, Terminal NUll, Mode VOLTAGE

    @Test
    void testRatioTapChangerOk() {
        // GIVEN
        RatioTapChangerAdder ratioTapChangerAdder = newRatioTapChangerAdder(true);
        ratioTapChangerAdder
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(remoteTerminal)
            .withTargetValue(240)
            .withTargetDeadband(2)
            .add();
        // WHEN
        RatioTapChanger ratioTapChanger = ratioTapChangerAdder.add();
        // THEN
        assertEquals(240, ratioTapChanger.getRegulatingTargetV());
        assertTrue(ratioTapChanger.isRegulating());
    }

    @Test
    void testRatioTapChangerErrorTargetValuePresent() {
        // GIVEN
        VoltageRegulationAdder<RatioTapChangerAdder> adder = newRatioTapChangerAdder(true)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(240);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, adder::add);
        // THEN
        assertEquals("2 windings transformer 'T1': The current regulationMode is VOLTAGE but allowed modes are [] when isRemote = false",
            validationException.getMessage());
    }

    // Cases Regulating True, Terminal present, Mode VOLTAGE

    @Test
    void testRatioTapChangerRemoteVoltageRegulatingOk() {
        // GIVEN
        RatioTapChangerAdder ratioTapChangerAdder = newRatioTapChangerAdder(true);
        VoltageRegulationAdder<RatioTapChangerAdder> voltageRegulationAdder = ratioTapChangerAdder
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(remoteTerminal)
            .withTargetValue(240)
            .withTargetDeadband(2);
        // WHEN
        RatioTapChanger ratioTapChanger = voltageRegulationAdder.add()
            .add();
        // THEN
        VoltageRegulation voltageRegulation = ratioTapChanger.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertEquals(240, voltageRegulation.getTargetValue());
        assertEquals(240, ratioTapChanger.getRegulatingTargetV());
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertEquals(remoteTerminal, voltageRegulation.getTerminal());
        assertTrue(voltageRegulation.isWithTerminal());
        assertTrue(ratioTapChanger.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertTrue(ratioTapChanger.isRemoteRegulating());
    }

    @Test
    void testRatioTapChangerRemoteVoltageRegulatingErrorMissingTargetValue() {
        // GIVEN
        VoltageRegulationAdder<RatioTapChangerAdder> adder = newRatioTapChangerAdder(true)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(remoteTerminal);
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, adder::add);
        // THEN
        assertEquals("2 windings transformer 'T1': Undefined value for voltageRegulation.targetValue, expected defined value when a terminal is set",
            validationException.getMessage());
    }

    // Cases Regulating false, Terminal NUll, Mode VOLTAGE

    @Test
    void testRatioTapChangerLocalVoltageRegulatingOffOk() {
        // GIVEN
        RatioTapChangerAdder ratioTapChangerAdder = newRatioTapChangerAdder(false);
        VoltageRegulationAdder<RatioTapChangerAdder> voltageRegulationAdder = ratioTapChangerAdder
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false);
        // WHEN
        RatioTapChanger ratioTapChanger = voltageRegulationAdder.add()
            .add();
        // THEN
        VoltageRegulation voltageRegulation = ratioTapChanger.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertTrue(Double.isNaN(ratioTapChanger.getRegulatingTargetQ()));
        assertTrue(Double.isNaN(ratioTapChanger.getRegulatingTargetV()));
        assertTrue(Double.isNaN(voltageRegulation.getTargetValue()));
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertFalse(voltageRegulation.isWithTerminal());
        assertFalse(ratioTapChanger.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertTrue(ratioTapChanger.isWithMode(RegulationMode.VOLTAGE));
        assertFalse(ratioTapChanger.isRemoteRegulating());
    }

    // Cases Regulating false, Terminal present, Mode VOLTAGE

    @Test
    void testRatioTapChangerRemoteVoltageRegulatingOffOk() {
        // GIVEN
        RatioTapChangerAdder ratioTapChangerAdder = newRatioTapChangerAdder(false);
        VoltageRegulationAdder<RatioTapChangerAdder> voltageRegulationAdder = ratioTapChangerAdder
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(220)
            .withTerminal(remoteTerminal)
            .withRegulating(false);
        // WHEN
        RatioTapChanger ratioTapChanger = voltageRegulationAdder.add()
            .add();
        // THEN
        VoltageRegulation voltageRegulation = ratioTapChanger.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertTrue(Double.isNaN(ratioTapChanger.getLocalTargetQ()));
        assertEquals(220, voltageRegulation.getTargetValue());
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertTrue(voltageRegulation.isWithTerminal());
        assertFalse(ratioTapChanger.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertTrue(ratioTapChanger.isWithMode(RegulationMode.VOLTAGE));
        assertTrue(ratioTapChanger.isRemoteRegulating());
    }

    // RemoveTerminal
    @Test
    void testRemoveTerminalOnMultiVariantWithLocalTargetV() {
        // GIVEN
        RatioTapChangerAdder ratioTapChangerAdder = newRatioTapChangerAdder(true);
        int localTargetV = 25;
        RatioTapChanger ratioTapChanger = ratioTapChangerAdder
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(220)
            .withTerminal(remoteTerminal)
            .withRegulating(true)
            .withTargetDeadband(2)
            .add()
            .add();
        VoltageRegulation voltageRegulation = ratioTapChanger.getVoltageRegulation();
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
            assertNull(ratioTapChanger.getVoltageRegulation().getTerminal());
            assertTrue(Double.isNaN(ratioTapChanger.getVoltageRegulation().getTargetValue()));
            assertTrue(Double.isNaN(ratioTapChanger.getRegulatingTargetQ()));
            assertTrue(Double.isNaN(ratioTapChanger.getRegulatingTargetV()));
        });
    }

    private RatioTapChangerAdder newRatioTapChangerAdder(boolean loadTapChangingCapabilities) {
        return twoWindingsTransformer.newRatioTapChanger()
            .setTapPosition(0)
            .setLoadTapChangingCapabilities(loadTapChangingCapabilities)
            .beginStep()
                .setRho(0.9)
                .endStep();
    }

    private RatioTapChanger createRatioTapChanger(DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator) {
        RatioTapChangerAdder ratioTapChangerAdder = newRatioTapChangerAdder(true);
        if (dataVoltageRegulationHolderCreator.mode() != null) {
            ratioTapChangerAdder.newVoltageRegulation()
                .withRegulating(dataVoltageRegulationHolderCreator.regulating())
                .withMode(dataVoltageRegulationHolderCreator.mode())
                .withTargetValue(dataVoltageRegulationHolderCreator.targetValue())
                .withTerminal(dataVoltageRegulationHolderCreator.remoteTerminal() ? remoteTerminal : null)
                .withTargetDeadband(dataVoltageRegulationHolderCreator.targetDeadband())
                .add();
        }
        ratioTapChangerAdder.setLoadTapChangingCapabilities(dataVoltageRegulationHolderCreator.regulating());
        return ratioTapChangerAdder.add();
    }

    private static Stream<Arguments> provideRatioTapChangerRegulating() {
        DataVoltageRegulationHolderCreator regulatingRemoteVoltage = new DataVoltageRegulationHolderCreator("regulatingRemoteVoltage",
            RegulationMode.VOLTAGE,
            true,
            400,
            2,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteVoltageWithTargetQ = new DataVoltageRegulationHolderCreator("regulatingRemoteVoltageWithTargetQ",
            RegulationMode.VOLTAGE,
            true,
            400,
            2,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteReactiveP = new DataVoltageRegulationHolderCreator("regulatingRemoteReactiveP",
            RegulationMode.REACTIVE_POWER,
            true,
            100,
            2,
            true);
        DataVoltageRegulationHolderCreator regulatingRemoteReactivePWithTargetQ = new DataVoltageRegulationHolderCreator("regulatingRemoteReactivePWithTargetQ",
            RegulationMode.REACTIVE_POWER,
            true,
            100,
            2,
            true);
        return Stream.of(
            addArgumentSet(regulatingRemoteVoltage, false),
            addArgumentSet(regulatingRemoteVoltageWithTargetQ, false),
            addArgumentSet(regulatingRemoteReactiveP, false),
            addArgumentSet(regulatingRemoteReactivePWithTargetQ, false)
        );
    }

    private static Arguments.@NonNull ArgumentSet addArgumentSet(DataVoltageRegulationHolderCreator regulatingLocalVoltage, boolean withValidationError) {
        String validationError = "RatioTapChanger '%s': invalid value (NaN) for localTargetQ (voltageRegulation is set with regulating false)";
        return argumentSet(regulatingLocalVoltage.id(),
            regulatingLocalVoltage,
            withValidationError ? String.format(validationError, regulatingLocalVoltage.id()) : null);
    }
}
