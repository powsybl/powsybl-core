/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.impl.results;

import com.google.common.testing.EqualsTester;
import com.powsybl.security.results.BranchResult;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class BranchResultTest {
    @Test
    public void test() {
        BranchResult branchResult = new BranchResult("id1", 500, 200, 300, -500, 300, -300);
        assertEquals("id1", branchResult.getBranchId());
        assertEquals(300.0, branchResult.getI1());
        assertEquals(500.0, branchResult.getP1());
        assertEquals(200.0, branchResult.getQ1());
        assertEquals(-300.0, branchResult.getI2());
        assertEquals(-500.0, branchResult.getP2());
        assertEquals(300.0, branchResult.getQ2());

        new EqualsTester()
            .addEqualityGroup(new BranchResult("id2", 400, 200, 300, -500, 300, -300),
                new BranchResult("id2", 400, 200, 300, -500, 300, -300))
            .addEqualityGroup(branchResult, branchResult)
            .testEquals();
    }
}
