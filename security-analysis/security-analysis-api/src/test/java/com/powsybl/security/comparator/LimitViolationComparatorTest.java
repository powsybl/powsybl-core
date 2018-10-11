/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.comparator;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.powsybl.iidm.network.Branch;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class LimitViolationComparatorTest {

    @Test
    public void compare() {
        LimitViolation line1Violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, Branch.Side.ONE);
        LimitViolation line1Violation2 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, Branch.Side.TWO);
        LimitViolation line2Violation = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 900.0, 0.95f, 950.0, Branch.Side.ONE);
        LimitViolation vl1Violation1 = new LimitViolation("VL1", LimitViolationType.HIGH_VOLTAGE, 200.0, 1, 250.0);
        LimitViolation vl1Violation2 = new LimitViolation("VL1", LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT, 200.0, 1, 250.0);

        List<LimitViolation> violations = Arrays.asList(line1Violation2, vl1Violation1, line2Violation, line1Violation1, vl1Violation2);
        Collections.sort(violations, new LimitViolationComparator());

        assertTrue(violations.get(0).equals(line1Violation1));
        assertTrue(violations.get(1).equals(line1Violation2));
        assertTrue(violations.get(2).equals(line2Violation));
        assertTrue(violations.get(3).equals(vl1Violation1));
        assertTrue(violations.get(4).equals(vl1Violation2));
    }
}
