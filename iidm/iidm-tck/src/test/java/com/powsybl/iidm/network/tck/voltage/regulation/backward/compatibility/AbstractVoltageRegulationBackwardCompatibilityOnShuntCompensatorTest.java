/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation.backward.compatibility;

import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.iidm.network.regulation.RegulationMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
@SuppressWarnings("removal") // Removal warnings are ignored. In fact, these tests are here to verify backward compatibility
public abstract class AbstractVoltageRegulationBackwardCompatibilityOnShuntCompensatorTest extends AbstractVoltageRegulationBackwardCompatibilityCommon {

    @Test
    public void testShuntCompensator() {
        // GIVEN
        int targetV = 220;
        double targetDeadband = 5.0;

        ShuntCompensatorAdder adder = voltageLevel.newShuntCompensator()
            .setId("shuntCompensator_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
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

        assertTrue(Double.isNaN(shuntCompensator.getLocalTargetV()));
        assertEquals(targetV, shuntCompensator.getTargetV());
        assertEquals(targetV, shuntCompensator.getRegulatingTargetV());
        assertEquals(targetV, shuntCompensator.getVoltageRegulation().getTargetValue());

        assertTrue(shuntCompensator.isRegulating());
        assertTrue(shuntCompensator.isVoltageRegulatorOn());

        assertEquals(targetDeadband, shuntCompensator.getTargetDeadband());
        assertEquals(targetDeadband, shuntCompensator.getVoltageRegulation().getTargetDeadband());
    }

    @Test
    public void testShuntCompensatorWithDeprecatedAndNewMethods() {
        // GIVEN
        int targetV = 220;
        double targetDeadband = 5.0;

        ShuntCompensatorAdder adder = voltageLevel.newShuntCompensator()
            .setId("shuntCompensator_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
            .setSectionCount(1)
            .newLinearModel()
            .setMaximumSectionCount(1)
            .setBPerSection(3)
            .add()
            .setTargetV(targetV) // deprecated method
            .newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTargetDeadband(targetDeadband)
                .add();
        // WHEN
        ShuntCompensator shuntCompensator = adder.add();
        // THEN
        assertNotNull(shuntCompensator);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            targetV,
            Double.NaN,
            Double.NaN,
            targetDeadband,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            null,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, shuntCompensator);
        assertEquals(shuntCompensator.getTerminal(), shuntCompensator.getRegulatingTerminal());
        assertNull(shuntCompensator.getVoltageRegulation().getTerminal());

        assertEquals(targetV, shuntCompensator.getLocalTargetV());
        assertEquals(targetV, shuntCompensator.getTargetV());
        assertEquals(targetV, shuntCompensator.getRegulatingTargetV());
        assertTrue(Double.isNaN(shuntCompensator.getVoltageRegulation().getTargetValue()));

        assertTrue(shuntCompensator.isRegulating());
        assertTrue(shuntCompensator.isVoltageRegulatorOn());

        assertEquals(targetDeadband, shuntCompensator.getTargetDeadband());
        assertEquals(targetDeadband, shuntCompensator.getVoltageRegulation().getTargetDeadband());
    }

}
