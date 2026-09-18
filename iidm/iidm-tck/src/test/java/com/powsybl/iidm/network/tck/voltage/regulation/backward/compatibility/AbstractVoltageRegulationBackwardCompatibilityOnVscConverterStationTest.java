/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation.backward.compatibility;

import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.VscConverterStationAdder;
import com.powsybl.iidm.network.regulation.RegulationMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
@SuppressWarnings("removal") // Removal warnings are ignored. In fact, these tests are here to verify backward compatibility
public abstract class AbstractVoltageRegulationBackwardCompatibilityOnVscConverterStationTest extends AbstractVoltageRegulationBackwardCompatibilityCommon {

    @Test
    public void testVscConverterStation() {
        // GIVEN
        int targetV = 120;
        int targetQ = 10;

        VscConverterStationAdder adder = voltageLevel.newVscConverterStation()
            .setId("vscConverterStation_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
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

        assertTrue(Double.isNaN(vscConverterStation.getLocalTargetV()));
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
    public void testVscConverterStationWithNewAndDeprecatedMethods() {
        // GIVEN
        int localTargetV = 120;
        int targetValue = 220;
        int localTargetQ = 10;

        VscConverterStationAdder adder = voltageLevel.newVscConverterStation()
            .setId("vscConverterStation_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
            .setLossFactor(0.9f)
            .setReactivePowerSetpoint(localTargetQ) // Deprecated method
            .setVoltageSetpoint(localTargetV) // Deprecated method
            .newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTerminal(remoteTerminal)
                .withTargetValue(targetValue)
                .add();
        // WHEN
        VscConverterStation vscConverterStation = adder.add();
        // THEN
        assertNotNull(vscConverterStation);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            localTargetV,
            localTargetQ,
            targetValue,
            Double.NaN,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            remoteTerminal,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, vscConverterStation);

        assertEquals(remoteTerminal, vscConverterStation.getRegulatingTerminal());
        assertEquals(remoteTerminal, vscConverterStation.getVoltageRegulation().getTerminal());

        assertEquals(localTargetV, vscConverterStation.getLocalTargetV());
        assertEquals(targetValue, vscConverterStation.getVoltageSetpoint());
        assertEquals(targetValue, vscConverterStation.getRegulatingTargetV());
        assertEquals(targetValue, vscConverterStation.getVoltageRegulation().getTargetValue());

        assertEquals(localTargetQ, vscConverterStation.getLocalTargetQ());
        assertEquals(localTargetQ, vscConverterStation.getReactivePowerSetpoint());
        assertEquals(localTargetQ, vscConverterStation.getRegulatingTargetQ());

        assertTrue(vscConverterStation.isVoltageRegulatorOn());
        assertTrue(vscConverterStation.isRegulating());
    }

}
