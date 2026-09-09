/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation.backward.compatibility;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
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
public abstract class AbstractVoltageRegulationBackwardCompatibilityOnRatioTapChangerTest extends AbstractVoltageRegulationBackwardCompatibilityCommon {

    @Test
    public void testRatioTapChanger() {
        // GIVEN
        int targetValue = 120;
        int targetV = 220;
        double targetDeadband = 5.0;

        TwoWindingsTransformer t2wt = network.getSubstation("P1").newTwoWindingsTransformer()
            .setId("twoWindingsTransformer_backwardCompatibility")
            .setVoltageLevel1("VLGEN")
            .setVoltageLevel2("VLGEN")
            .setConnectableBus1(LOCAL_BUS)
            .setConnectableBus2(LOCAL_BUS)
            .setR(2)
            .setX(1)
            .add();
        Terminal terminalLeg1 = t2wt.getTerminal1();
        RatioTapChangerAdder adder = t2wt.newRatioTapChanger()
            .setTapPosition(0)
            .beginStep().setRho(1).endStep()
            .setLoadTapChangingCapabilities(true)
            .setRegulating(true)
            .setRegulationMode(RegulationMode.REACTIVE_POWER)
            .setTargetDeadband(targetDeadband)
            .setTargetV(targetV)
            .setRegulationValue(targetValue)
            .setRegulationTerminal(terminalLeg1);
        // WHEN
        RatioTapChanger ratioTapChanger = adder.add();
        // THEN
        assertNotNull(ratioTapChanger);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            Double.NaN,
            Double.NaN,
            targetValue,
            targetDeadband,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            terminalLeg1,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, ratioTapChanger);

        assertNull(ratioTapChanger.getTerminal());
        assertEquals(terminalLeg1, ratioTapChanger.getRegulatingTerminal());

        assertTrue(Double.isNaN(ratioTapChanger.getLocalTargetV()));
        assertEquals(targetValue, ratioTapChanger.getTargetV());
        assertEquals(targetValue, ratioTapChanger.getRegulatingTargetV());
        assertEquals(targetValue, ratioTapChanger.getVoltageRegulation().getTargetValue());

        assertTrue(Double.isNaN(ratioTapChanger.getLocalTargetQ()));
        assertTrue(Double.isNaN(ratioTapChanger.getRegulatingTargetQ()));

        assertTrue(ratioTapChanger.isRegulating());
    }

}
