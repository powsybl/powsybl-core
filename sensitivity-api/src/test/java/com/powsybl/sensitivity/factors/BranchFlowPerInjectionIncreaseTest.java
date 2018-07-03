/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors;

import com.powsybl.sensitivity.factors.functions.BranchFlow;
import com.powsybl.sensitivity.factors.variables.InjectionIncrease;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class BranchFlowPerInjectionIncreaseTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void checkFailsWhenNullFunction() {
        InjectionIncrease injectionIncrease = Mockito.mock(InjectionIncrease.class);
        exception.expect(NullPointerException.class);
        new BranchFlowPerInjectionIncrease(null, injectionIncrease);
    }

    @Test
    public void checkFailsWhenNullVariable() {
        BranchFlow branchFlow = Mockito.mock(BranchFlow.class);
        exception.expect(NullPointerException.class);
        new BranchFlowPerInjectionIncrease(branchFlow, null);
    }

    @Test
    public void testGetFunction() {
        BranchFlow branchFlow = Mockito.mock(BranchFlow.class);
        InjectionIncrease injectionIncrease = Mockito.mock(InjectionIncrease.class);
        BranchFlowPerInjectionIncrease factor = new BranchFlowPerInjectionIncrease(branchFlow, injectionIncrease);
        assertEquals(branchFlow, factor.getFunction());
    }

    @Test
    public void testGetVariable() {
        BranchFlow branchFlow = Mockito.mock(BranchFlow.class);
        InjectionIncrease injectionIncrease = Mockito.mock(InjectionIncrease.class);
        BranchFlowPerInjectionIncrease factor = new BranchFlowPerInjectionIncrease(branchFlow, injectionIncrease);
        assertEquals(injectionIncrease, factor.getVariable());
    }
}
