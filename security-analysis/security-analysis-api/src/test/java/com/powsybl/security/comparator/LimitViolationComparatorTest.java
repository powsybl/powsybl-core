/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.comparator;

import com.powsybl.iidm.network.Branch;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
        LimitViolation line1AcPViolation = new LimitViolation("NHV1_NHV2_1", LimitViolationType.ACTIVE_POWER, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, Branch.Side.ONE);
        LimitViolation line2AppViolation = new LimitViolation("NHV1_NHV2_2", LimitViolationType.APPARENT_POWER, null, Integer.MAX_VALUE, 900.0, 0.95f, 950.0, Branch.Side.ONE);

        List<LimitViolation> violations = Arrays.asList(line1Violation2, vl1Violation1, line2Violation, line1Violation1, vl1Violation2, line1AcPViolation, line2AppViolation);
        Collections.sort(violations, new LimitViolationComparator());

        assertEquals(line1AcPViolation, violations.get(0));
        assertEquals(line1Violation1, violations.get(1));
        assertEquals(line1Violation2, violations.get(2));
        assertEquals(line2AppViolation, violations.get(3));
        assertEquals(line2Violation, violations.get(4));
        assertEquals(vl1Violation1, violations.get(5));
        assertEquals(vl1Violation2, violations.get(6));
    }
}
