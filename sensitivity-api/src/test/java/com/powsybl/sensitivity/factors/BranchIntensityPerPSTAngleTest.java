/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors;

import com.powsybl.sensitivity.factors.functions.BranchIntensity;
import com.powsybl.sensitivity.factors.variables.PhaseTapChangerAngle;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertSame;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class BranchIntensityPerPSTAngleTest {

    private BranchIntensity branchIntensity;
    private PhaseTapChangerAngle pstAngle;
    private BranchIntensityPerPSTAngle factor;

    @Before
    public void setUp() {
        branchIntensity = Mockito.mock(BranchIntensity.class);
        pstAngle = Mockito.mock(PhaseTapChangerAngle.class);
        factor = new BranchIntensityPerPSTAngle(branchIntensity, pstAngle);
    }

    @Test
    public void getFunction() {
        assertSame(branchIntensity, factor.getFunction());
    }

    @Test
    public void getVariable() {
        assertSame(pstAngle, factor.getVariable());
    }
}
