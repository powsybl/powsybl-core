/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.violations;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class LimitViolationTest {

    @Test
    void testToString() {
        LimitViolation limitViolation1 = new LimitViolationBuilder()
            .subject("testId")
            .type(LimitViolationType.HIGH_VOLTAGE)
            .limitName("high")
            .duration(Integer.MAX_VALUE)
            .limit(420)
            .value(500)
            .build();
        LimitViolation limitViolation2 = LimitViolation.builder()
            .subject("testId")
            .type(LimitViolationType.CURRENT)
            .duration(6300)
            .limit(1000)
            .value(1100)
            .side(TwoSides.ONE)
            .build();
        LimitViolation limitViolation3 = LimitViolation.builder()
            .subject("testId")
            .type(LimitViolationType.APPARENT_POWER)
            .duration(6300)
            .limit(1000)
            .value(1100)
            .side3()
            .build();
        LimitViolation limitViolation4 = LimitViolation.builder()
            .subject("testId")
            .type(LimitViolationType.LOW_VOLTAGE)
            .limit(1000)
            .reduction(1)
            .value(1100)
            .violationLocation(new BusBreakerViolationLocation(List.of("busId1", "busId2")))
            .build();
        String expected1 = "Subject id: testId, Subject name: null, limitType: HIGH_VOLTAGE, limit: 420.0, limitName: high, acceptableDuration: 2147483647, limitReduction: 1.0, value: 500.0, side: null, voltageLocation: null";
        String expected2 = "Subject id: testId, Subject name: null, limitType: CURRENT, limit: 1000.0, limitName: null, acceptableDuration: 6300, limitReduction: 1.0, value: 1100.0, side: ONE, voltageLocation: null";
        String expected3 = "Subject id: testId, Subject name: null, limitType: APPARENT_POWER, limit: 1000.0, limitName: null, acceptableDuration: 6300, limitReduction: 1.0, value: 1100.0, side: THREE, voltageLocation: null";
        String expected4 = "Subject id: testId, Subject name: null, limitType: LOW_VOLTAGE, limit: 1000.0, limitName: null, acceptableDuration: 2147483647, limitReduction: 1.0, value: 1100.0, side: null, voltageLocation: BusBreakerViolationLocation{busIds='[busId1, busId2]'}";
        assertEquals(expected1, limitViolation1.toString());
        assertEquals(expected2, limitViolation2.toString());
        assertEquals(expected3, limitViolation3.toString());
        assertEquals(expected4, limitViolation4.toString());

    }
}
