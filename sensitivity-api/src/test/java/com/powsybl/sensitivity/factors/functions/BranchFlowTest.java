/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors.functions;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class BranchFlowTest {

    private static final String FUNCTION_ID = "Function ID";
    private static final String FUNCTION_NAME = "Function name";
    private static final String BRANCH_ID = "Branch ID";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testNullBranchId() {
        exception.expect(NullPointerException.class);
        new BranchFlow(FUNCTION_ID, FUNCTION_NAME, null);
    }

    @Test
    public void getName() {
        BranchFlow branchFlow = new BranchFlow(FUNCTION_ID, FUNCTION_NAME, BRANCH_ID);
        assertEquals(FUNCTION_NAME, branchFlow.getName());
    }

    @Test
    public void getId() {
        BranchFlow branchFlow = new BranchFlow(FUNCTION_ID, FUNCTION_NAME, BRANCH_ID);
        assertEquals(FUNCTION_ID, branchFlow.getId());
    }

    @Test
    public void getBranchId() {
        BranchFlow branchFlow = new BranchFlow(FUNCTION_ID, FUNCTION_NAME, BRANCH_ID);
        assertEquals(BRANCH_ID, branchFlow.getBranchId());
    }

}
