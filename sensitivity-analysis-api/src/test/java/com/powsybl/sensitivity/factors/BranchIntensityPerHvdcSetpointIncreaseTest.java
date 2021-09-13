/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors;

import com.powsybl.sensitivity.factors.functions.BranchIntensity;
import com.powsybl.sensitivity.factors.variables.HvdcSetpointIncrease;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertSame;

/**
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 */
public class BranchIntensityPerHvdcSetpointIncreaseTest {

    private BranchIntensity branchIntensity;
    private HvdcSetpointIncrease hvdcSetpointIncrease;
    private BranchIntensityPerHvdcSetpointIncrease factor;

    @Before
    public void setUp() {
        branchIntensity = Mockito.mock(BranchIntensity.class);
        hvdcSetpointIncrease = Mockito.mock(HvdcSetpointIncrease.class);
        factor = new BranchIntensityPerHvdcSetpointIncrease(branchIntensity, hvdcSetpointIncrease);
    }

    @Test
    public void getFunction() {
        assertSame(branchIntensity, factor.getFunction());
    }

    @Test
    public void getVariable() {
        assertSame(hvdcSetpointIncrease, factor.getVariable());
    }
}
