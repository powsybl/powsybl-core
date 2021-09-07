/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors;

import com.powsybl.sensitivity.factors.functions.BranchFlow;
import com.powsybl.sensitivity.factors.variables.HvdcSetpointIncrease;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertThrows;

/**
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 */
public class BranchFlowPerHvdcSetpointIncreaseTest {

    @Test
    public void checkFailsWhenNullFunction() {
        HvdcSetpointIncrease hvdcSetpointIncrease = Mockito.mock(HvdcSetpointIncrease.class);
        assertThrows(NullPointerException.class, () ->
            new BranchFlowPerHvdcSetpointIncrease(null, hvdcSetpointIncrease));
    }

    @Test
    public void checkFailsWhenNullVariable() {
        BranchFlow branchFlow = Mockito.mock(BranchFlow.class);
        assertThrows(NullPointerException.class, () ->
            new BranchFlowPerHvdcSetpointIncrease(branchFlow, null));
    }

    @Test
    public void testGetFunction() {
        BranchFlow branchFlow = Mockito.mock(BranchFlow.class);
        HvdcSetpointIncrease hvdcSetpointIncrease = Mockito.mock(HvdcSetpointIncrease.class);
        BranchFlowPerHvdcSetpointIncrease factor = new BranchFlowPerHvdcSetpointIncrease(branchFlow, hvdcSetpointIncrease);
        assertEquals(branchFlow, factor.getFunction());
    }

    @Test
    public void testGetVariable() {
        BranchFlow branchFlow = Mockito.mock(BranchFlow.class);
        HvdcSetpointIncrease hvdcSetpointIncrease = Mockito.mock(HvdcSetpointIncrease.class);
        BranchFlowPerHvdcSetpointIncrease factor = new BranchFlowPerHvdcSetpointIncrease(branchFlow, hvdcSetpointIncrease);
        assertEquals(hvdcSetpointIncrease, factor.getVariable());
    }
}
