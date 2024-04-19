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

import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class LimitViolationEquivalenceTest {

    @Test
    void equivalent() {
        LimitViolationEquivalence violationEquivalence = new LimitViolationEquivalence(0.1);

        LimitViolation violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, TwoSides.ONE);

        LimitViolation violation2 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, TwoSides.ONE);
        assertTrue(violationEquivalence.equivalent(violation1, violation2));

        violation2 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.09, TwoSides.ONE);
        assertTrue(violationEquivalence.equivalent(violation1, violation2));

        violation2 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1101.0, TwoSides.ONE);
        assertFalse(violationEquivalence.equivalent(violation1, violation2));

        violation2 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, TwoSides.TWO);
        assertFalse(violationEquivalence.equivalent(violation1, violation2));

        violation2 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, TwoSides.ONE);
        assertFalse(violationEquivalence.equivalent(violation1, violation2));

        violation2 = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, TwoSides.ONE);
        assertFalse(violationEquivalence.equivalent(violation1, violation2));

        violation2 = new LimitViolation("NHV1_NHV2_2", LimitViolationType.ACTIVE_POWER, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, TwoSides.ONE);
        assertFalse(violationEquivalence.equivalent(violation1, violation2));

        violation2 = new LimitViolation("NHV1_NHV2_2", LimitViolationType.APPARENT_POWER, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, TwoSides.TWO);
        assertFalse(violationEquivalence.equivalent(violation1, violation2));
    }

}
