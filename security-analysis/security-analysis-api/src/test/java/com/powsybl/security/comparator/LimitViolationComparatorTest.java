/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import com.powsybl.contingency.violations.LimitViolation;
import com.powsybl.contingency.violations.LimitViolationType;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class LimitViolationComparatorTest {

    @Test
    void compare() {
        LimitViolation line1Violation1 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100.0)
            .side(TwoSides.ONE)
            .build();
        LimitViolation line1Violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100.0)
            .side(TwoSides.TWO)
            .build();
        LimitViolation line2Violation1 = LimitViolation.builder()
            .subject("NHV1_NHV2_2")
            .type(LimitViolationType.CURRENT)
            .limit(900.0)
            .reduction(0.95f)
            .value(950.0)
            .side(TwoSides.ONE)
            .build();
        LimitViolation line2Violation2 = LimitViolation.builder()
            .subject(EurostagTutorialExample1Factory.NHV1_NHV2_2)
            .type(LimitViolationType.CURRENT)
            .operationalLimitsGroupId("group_2")
            .limit(900.0)
            .reduction(0.95f)
            .value(950.0)
            .side(TwoSides.ONE)
            .build();
        LimitViolation line2Violation3 = LimitViolation.builder()
            .subject(EurostagTutorialExample1Factory.NHV1_NHV2_2)
            .type(LimitViolationType.CURRENT)
            .operationalLimitsGroupId("group_1")
            .limit(900.0)
            .reduction(0.95f)
            .value(950.0)
            .side(TwoSides.ONE)
            .build();
        LimitViolation vl1Violation1 = LimitViolation.builder()
            .subject("VL1")
            .type(LimitViolationType.HIGH_VOLTAGE)
            .limit(200.0)
            .value(250.0)
            .build();
        LimitViolation vl1Violation2 = LimitViolation.builder()
            .subject("VL1")
            .type(LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT)
            .limit(200.0)
            .value(250.0)
            .build();
        LimitViolation line1AcPViolation = LimitViolation.builder()
            .subject(EurostagTutorialExample1Factory.NHV1_NHV2_1)
            .type(LimitViolationType.ACTIVE_POWER)
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100.0)
            .side(TwoSides.ONE)
            .build();
        LimitViolation line2AppViolation = LimitViolation.builder()
            .subject(EurostagTutorialExample1Factory.NHV1_NHV2_2)
            .type(LimitViolationType.APPARENT_POWER)
            .limit(900.0)
            .reduction(0.95f)
            .value(950.0)
            .side(TwoSides.ONE)
            .build();

        List<LimitViolation> violations = Arrays.asList(line1Violation2, vl1Violation1, line2Violation2, line2Violation3, line2Violation1, line1Violation1, vl1Violation2, line1AcPViolation, line2AppViolation);
        Collections.sort(violations, new LimitViolationComparator());

        assertEquals(line1AcPViolation, violations.get(0));
        assertEquals(line1Violation1, violations.get(1));
        assertEquals(line1Violation2, violations.get(2));
        assertEquals(line2AppViolation, violations.get(3));
        assertEquals(line2Violation1, violations.get(4));
        assertEquals(line2Violation3, violations.get(5));
        assertEquals(line2Violation2, violations.get(6));
        assertEquals(vl1Violation1, violations.get(7));
        assertEquals(vl1Violation2, violations.get(8));
    }
}
