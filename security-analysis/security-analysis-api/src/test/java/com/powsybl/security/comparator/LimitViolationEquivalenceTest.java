/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.comparator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.powsybl.iidm.network.TwoSides;
import org.junit.jupiter.api.Test;

import com.powsybl.contingency.violations.LimitViolation;
import com.powsybl.contingency.violations.LimitViolationType;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class LimitViolationEquivalenceTest {

    @Test
    void equivalent() {
        LimitViolationEquivalence violationEquivalence = new LimitViolationEquivalence(0.1);

        LimitViolation violation1 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100)
            .side(TwoSides.ONE)
            .build();

        LimitViolation violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100)
            .side(TwoSides.ONE)
            .build();

        assertTrue(violationEquivalence.equivalent(violation1, violation2));

        violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100.09)
            .side(TwoSides.ONE)
            .build();
        assertTrue(violationEquivalence.equivalent(violation1, violation2));

        violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limit(1000.0)
            .reduction(0.95f)
            .value(1101)
            .side(TwoSides.ONE)
            .build();
        assertFalse(violationEquivalence.equivalent(violation1, violation2));

        violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100)
            .side(TwoSides.TWO)
            .build();
        assertFalse(violationEquivalence.equivalent(violation1, violation2));

        violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT)
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100)
            .side(TwoSides.ONE)
            .build();
        assertFalse(violationEquivalence.equivalent(violation1, violation2));

        violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_2")
            .type(LimitViolationType.CURRENT)
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100)
            .side(TwoSides.ONE)
            .build();
        assertFalse(violationEquivalence.equivalent(violation1, violation2));

        violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_2")
            .type(LimitViolationType.ACTIVE_POWER)
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100)
            .side(TwoSides.ONE)
            .build();
        assertFalse(violationEquivalence.equivalent(violation1, violation2));

        violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_2")
            .type(LimitViolationType.APPARENT_POWER)
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100)
            .side(TwoSides.TWO)
            .build();
        assertFalse(violationEquivalence.equivalent(violation1, violation2));

        violation1 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .operationalLimitsGroupId("group_1")
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100)
            .side(TwoSides.ONE)
            .build();

        violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .operationalLimitsGroupId("group_1")
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100)
            .side(TwoSides.ONE)
            .build();
        assertTrue(violationEquivalence.equivalent(violation1, violation2));

        violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .operationalLimitsGroupId("group_1")
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100.09)
            .side(TwoSides.ONE)
            .build();
        assertTrue(violationEquivalence.equivalent(violation1, violation2));

        violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .operationalLimitsGroupId("group_2")
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100)
            .side(TwoSides.ONE)
            .build();
        assertFalse(violationEquivalence.equivalent(violation1, violation2));

        violation1 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .operationalLimitsGroupId(null)
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100.09)
            .side(TwoSides.ONE)
            .build();

        violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .operationalLimitsGroupId("group_1")
            .limit(1000.0)
            .reduction(0.95f)
            .value(1100.09)
            .side(TwoSides.ONE)
            .build();
        assertFalse(violationEquivalence.equivalent(violation1, violation2));
    }

}
