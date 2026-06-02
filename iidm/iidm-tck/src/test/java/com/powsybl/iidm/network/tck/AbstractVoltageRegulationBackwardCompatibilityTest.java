/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.AcDcConverter;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.BatteryAdder;
import com.powsybl.iidm.network.DcNode;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageSourceConverter;
import com.powsybl.iidm.network.VoltageSourceConverterAdder;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.VscConverterStationAdder;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
@SuppressWarnings("removal") // Removal warnings are ignored. In fact, these tests are here to verify backward compatibility
public abstract class AbstractVoltageRegulationBackwardCompatibilityTest {

    private Network network;
    private VoltageLevel voltageLevel;
    private Terminal remoteTerminal;

    @BeforeEach
    void initNetwork() {
        network = BatteryNetworkFactory.create();
        voltageLevel = network.getVoltageLevel("VLGEN");
        remoteTerminal = network.getBattery("BAT").getTerminal();
    }

    @Test
    void testBattery() {
        // GIVEN
        int targetQ = 10;

        BatteryAdder adder = voltageLevel.newBattery()
            .setId("battery_backwardCompatibility")
            .setConnectableBus("NGEN")
            .setMinP(0)
            .setTargetP(10)
            .setMaxP(20)
            .setTargetQ(targetQ);
        // WHEN
        Battery battery = adder.add();
        // THEN
        assertNotNull(battery);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            Double.NaN,
            targetQ,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            RegulationMode.REACTIVE_POWER,
            false,
            null,
            false);
        checkVoltageRegulationAttributes(expectedAttributes, battery);

        assertEquals(battery.getTerminal(), battery.getRegulatingTerminal());
        assertNull(battery.getVoltageRegulation());

        assertEquals(Double.NaN, battery.getLocalTargetV());
        assertEquals(Double.NaN, battery.getRegulatingTargetV());

        assertEquals(targetQ, battery.getLocalTargetQ());
        assertEquals(targetQ, battery.getTargetQ());
        assertEquals(targetQ, battery.getRegulatingTargetQ());

        assertFalse(battery.isRegulating());
    }

    @Test
    void testGeneratorRemoteVoltageRegulation() {
        // GIVEN
        int remoteTargetV = 220;
        int localTargetV = 110;
        int targetQ = 10;

        GeneratorAdder adder = voltageLevel.newGenerator()
            .setId("generator_backwardCompatibility")
            .setConnectableBus("NGEN")
            .setMinP(0)
            .setTargetP(20)
            .setMaxP(100)
            .setVoltageRegulatorOn(true)
            .setRegulatingTerminal(remoteTerminal)
            .setTargetV(remoteTargetV, localTargetV)
            .setTargetQ(targetQ);
        // WHEN
        Generator generator = adder.add();
        // THEN
        assertNotNull(generator);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            localTargetV,
            targetQ,
            remoteTargetV,
            Double.NaN,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            remoteTerminal,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, generator);

        assertEquals(remoteTerminal, generator.getRegulatingTerminal());
        assertEquals(remoteTerminal, generator.getVoltageRegulation().getTerminal());

        assertEquals(localTargetV, generator.getLocalTargetV());
        assertEquals(localTargetV, generator.getEquivalentLocalTargetV());
        assertEquals(remoteTargetV, generator.getTargetV());
        assertEquals(remoteTargetV, generator.getRegulatingTargetV());
        assertEquals(remoteTargetV, generator.getVoltageRegulation().getTargetValue());

        assertEquals(targetQ, generator.getLocalTargetQ());
        assertEquals(targetQ, generator.getTargetQ());
        assertEquals(targetQ, generator.getRegulatingTargetQ());

        assertTrue(generator.isRegulating());
        assertTrue(generator.isVoltageRegulatorOn());
    }

    @Test
    void testGeneratorLocalVoltageRegulation() {
        // GIVEN
        int remoteTargetV = 220;
        int localTargetV = 110;
        int targetQ = 10;

        GeneratorAdder adder = voltageLevel.newGenerator()
            .setId("generator_backwardCompatibility")
            .setConnectableBus("NGEN")
            .setMinP(0)
            .setTargetP(20)
            .setMaxP(100)
            .setVoltageRegulatorOn(true)
            .setTargetV(remoteTargetV, localTargetV)
            .setTargetQ(targetQ);
        // WHEN
        Generator generator = adder.add();
        // THEN
        assertNotNull(generator);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            localTargetV,
            targetQ,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            null,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, generator);

        assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());
        assertNull(generator.getVoltageRegulation().getTerminal());

        assertEquals(localTargetV, generator.getLocalTargetV());
        assertEquals(localTargetV, generator.getEquivalentLocalTargetV());
        assertEquals(localTargetV, generator.getTargetV());
        assertEquals(localTargetV, generator.getRegulatingTargetV());
        assertEquals(Double.NaN, generator.getVoltageRegulation().getTargetValue());

        assertEquals(targetQ, generator.getLocalTargetQ());
        assertEquals(targetQ, generator.getTargetQ());
        assertEquals(targetQ, generator.getRegulatingTargetQ());

        assertTrue(generator.isRegulating());
        assertTrue(generator.isVoltageRegulatorOn());
    }

    @Test
    void testRatioTapChanger() {
        // GIVEN
        // WHEN
        // THEN
    }

    @Test
    void testShuntCompensator() {
        // GIVEN
        int targetV = 220;
        double targetDeadband = 5.0;

        ShuntCompensatorAdder adder = voltageLevel.newShuntCompensator()
            .setId("shuntCompensator_backwardCompatibility")
            .setConnectableBus("NGEN")
            .setSectionCount(1)
            .newLinearModel()
                .setMaximumSectionCount(1)
                .setBPerSection(3)
                .add()
            .setTargetV(targetV)
            .setVoltageRegulatorOn(true)
            .setRegulatingTerminal(remoteTerminal)
            .setTargetDeadband(targetDeadband);
        // WHEN
        ShuntCompensator shuntCompensator = adder.add();
        // THEN
        assertNotNull(shuntCompensator);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            Double.NaN,
            Double.NaN,
            targetV,
            targetDeadband,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            remoteTerminal,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, shuntCompensator);
        assertEquals(remoteTerminal, shuntCompensator.getRegulatingTerminal());
        assertEquals(remoteTerminal, shuntCompensator.getVoltageRegulation().getTerminal());

        assertEquals(Double.NaN, shuntCompensator.getLocalTargetV());
        assertEquals(targetV, shuntCompensator.getTargetV());
        assertEquals(targetV, shuntCompensator.getRegulatingTargetV());
        assertEquals(targetV, shuntCompensator.getVoltageRegulation().getTargetValue());

        assertTrue(shuntCompensator.isRegulating());
        assertTrue(shuntCompensator.isVoltageRegulatorOn());

        assertEquals(targetDeadband, shuntCompensator.getTargetDeadband());
        assertEquals(targetDeadband, shuntCompensator.getVoltageRegulation().getTargetDeadband());
    }

    @Test
    void testStaticVarCompensator() {
        // GIVEN
        int targetV = 120;
        int targetQ = 10;

        StaticVarCompensatorAdder adder = voltageLevel.newStaticVarCompensator()
            .setId("staticVarCompensator_backwardCompatibility")
            .setConnectableBus("NGEN")
            .setBmin(0)
            .setBmax(1)
            .setRegulating(true)
            .setReactivePowerSetpoint(targetQ)
            .setRegulationMode(RegulationMode.VOLTAGE)
            .setVoltageSetpoint(targetV)
            .setRegulatingTerminal(remoteTerminal);
        // WHEN
        StaticVarCompensator staticVarCompensator = adder.add();
        // THEN
        assertNotNull(staticVarCompensator);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            Double.NaN,
            targetQ,
            targetV,
            Double.NaN,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            remoteTerminal,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, staticVarCompensator);

        assertEquals(remoteTerminal, staticVarCompensator.getRegulatingTerminal());
        assertEquals(remoteTerminal, staticVarCompensator.getVoltageRegulation().getTerminal());

        assertEquals(Double.NaN, staticVarCompensator.getLocalTargetV());
        assertEquals(targetV, staticVarCompensator.getVoltageSetpoint());
        assertEquals(targetV, staticVarCompensator.getRegulatingTargetV());
        assertEquals(targetV, staticVarCompensator.getVoltageRegulation().getTargetValue());

        assertEquals(targetQ, staticVarCompensator.getLocalTargetQ());
        assertEquals(targetQ, staticVarCompensator.getReactivePowerSetpoint());
        assertEquals(targetQ, staticVarCompensator.getRegulatingTargetQ());

        assertTrue(staticVarCompensator.isRegulating());
    }

    @Test
    void testVscConverterStation() {
        // GIVEN
        int targetV = 120;
        int targetQ = 10;

        VscConverterStationAdder adder = voltageLevel.newVscConverterStation()
            .setId("vscConverterStation_backwardCompatibility")
            .setConnectableBus("NGEN")
            .setLossFactor(0.9f)
            .setVoltageRegulatorOn(true)
            .setReactivePowerSetpoint(targetQ)
            .setVoltageSetpoint(targetV)
            .setRegulatingTerminal(remoteTerminal);
        // WHEN
        VscConverterStation vscConverterStation = adder.add();
        // THEN
        assertNotNull(vscConverterStation);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            Double.NaN,
            targetQ,
            targetV,
            Double.NaN,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            remoteTerminal,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, vscConverterStation);

        assertEquals(remoteTerminal, vscConverterStation.getRegulatingTerminal());
        assertEquals(remoteTerminal, vscConverterStation.getVoltageRegulation().getTerminal());

        assertEquals(Double.NaN, vscConverterStation.getLocalTargetV());
        assertEquals(targetV, vscConverterStation.getVoltageSetpoint());
        assertEquals(targetV, vscConverterStation.getRegulatingTargetV());
        assertEquals(targetV, vscConverterStation.getVoltageRegulation().getTargetValue());

        assertEquals(targetQ, vscConverterStation.getLocalTargetQ());
        assertEquals(targetQ, vscConverterStation.getReactivePowerSetpoint());
        assertEquals(targetQ, vscConverterStation.getRegulatingTargetQ());

        assertTrue(vscConverterStation.isVoltageRegulatorOn());
        assertTrue(vscConverterStation.isRegulating());
    }

    @Test
    void testVoltageSourceConverter() {
        // GIVEN
        int targetV = 120;
        int targetQ = 10;

        DcNode dcNode1 = network.newDcNode()
            .setId("dcNode1")
            .setNominalV(500.)
            .add();
        DcNode dcNode2 = network.newDcNode()
            .setId("dcNode2")
            .setNominalV(500.)
            .add();
        VoltageSourceConverterAdder adder = voltageLevel.newVoltageSourceConverter()
            .setId("vscConverterStation_backwardCompatibility")
            .setControlMode(AcDcConverter.ControlMode.V_DC)
            .setTargetVdc(110)
            .setConnectableBus1("NGEN")
            .setConnectableBus2("NGEN")
            .setDcNode1(dcNode1.getId())
            .setDcNode2(dcNode2.getId())
            .setReactivePowerSetpoint(targetQ)
            .setVoltageSetpoint(targetV)
            .setVoltageRegulatorOn(true);
        // WHEN
        VoltageSourceConverter voltageSourceConverter = adder.add();
        // THEN
        assertNotNull(voltageSourceConverter);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            targetV,
            targetQ,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            null,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, voltageSourceConverter);

        assertEquals(targetV, voltageSourceConverter.getLocalTargetV());
        assertEquals(targetV, voltageSourceConverter.getVoltageSetpoint());
        assertEquals(targetV, voltageSourceConverter.getRegulatingTargetV());
        assertEquals(Double.NaN, voltageSourceConverter.getVoltageRegulation().getTargetValue());

        assertEquals(targetQ, voltageSourceConverter.getLocalTargetQ());
        assertEquals(targetQ, voltageSourceConverter.getReactivePowerSetpoint());
        assertEquals(targetQ, voltageSourceConverter.getRegulatingTargetQ());

        assertTrue(voltageSourceConverter.isVoltageRegulatorOn());
        assertTrue(voltageSourceConverter.isRegulating());
    }

    private void checkVoltageRegulationAttributes(VoltageRegulationAttributesToCheck expected, VoltageRegulationHolder actual) {
        assertNotNull(expected);
        assertNotNull(actual);

        assertTrue(actual.isWithMode(expected.mode()));

        if (expected.terminal() != null) {
            assertEquals(expected.terminal().getConnectable().getId(), actual.getRegulatingTerminal().getConnectable().getId());
        } else {
            if (actual.getVoltageRegulation() != null) {
                assertNull(actual.getVoltageRegulation().getTerminal());
            }
        }

        assertEquals(expected.localTargetQ(), actual.getLocalTargetQ());
        assertEquals(expected.localTargetV(), actual.getLocalTargetV());
        if (expected.withVoltageRegulationPresent) {
            assertEquals(expected.targetValue(), actual.getVoltageRegulation().getTargetValue());
        } else {
            assertNull(actual.getVoltageRegulation());
        }

        assertEquals(expected.isRegulating(), actual.isRegulating());
        assertEquals(expected.terminal() != null, actual.isRemoteRegulating());

        if (actual.getVoltageRegulation() != null) {
            assertEquals(expected.targetDeadband(), actual.getVoltageRegulation().getTargetDeadband());

            assertEquals(expected.slope(), actual.getVoltageRegulation().getSlope());
        }
    }

    private record VoltageRegulationAttributesToCheck(
        double localTargetV,
        double localTargetQ,
        double targetValue,
        double targetDeadband,
        double slope,
        RegulationMode mode,
        boolean isRegulating,
        Terminal terminal,
        boolean withVoltageRegulationPresent
    ) {
    }

}
