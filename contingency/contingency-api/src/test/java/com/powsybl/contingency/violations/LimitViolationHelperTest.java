/**
 * Copyright (c) 2026, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.violations;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Landry Huet {@literal <landry.huet at supergrid-institute.com>}
 */
class LimitViolationHelperTest {

    private static final String LINE_ID = "NHV1_NHV2_1";

    @Test
    void branchViolationWithoutSideThrows() {
        Network network = EurostagTutorialExample1Factory.create();
        LimitViolation violation = currentViolation().build();

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> LimitViolationHelper.getVoltageLevel(violation, network));
        assertEquals("Violation on '" + LINE_ID + "' has no side.", e.getMessage());
    }

    @Test
    void branchViolationWithSideReturnsTerminalVoltageLevel() {
        Network network = EurostagTutorialExample1Factory.create();
        LimitViolation violationSide1 = currentViolation().side(TwoSides.ONE).build();
        LimitViolation violationSide2 = currentViolation().side(TwoSides.TWO).build();

        assertEquals("VLHV1", LimitViolationHelper.getVoltageLevel(violationSide1, network).orElseThrow().getId());
        assertEquals("VLHV2", LimitViolationHelper.getVoltageLevel(violationSide2, network).orElseThrow().getId());
    }

    private static LimitViolationBuilder currentViolation() {
        return new LimitViolationBuilder()
                .subject(LINE_ID)
                .current()
                .duration(Integer.MAX_VALUE)
                .limit(500)
                .value(600);
    }
}
