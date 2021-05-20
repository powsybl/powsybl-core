/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors;

import com.powsybl.sensitivity.ContingencyContext;
import com.powsybl.sensitivity.SensitivityFunctionType;
import com.powsybl.sensitivity.SensitivityVariableType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class BranchIntensityPerPSTAngleTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void checkFailsWhenNullFunction() {
        exception.expect(NullPointerException.class);
        new BranchIntensityPerPSTAngle(null, "12", ContingencyContext.createAllContingencyContext());
    }

    @Test
    public void checkFailsWhenNullVariable() {
        exception.expect(NullPointerException.class);
        new BranchIntensityPerPSTAngle("12", null, ContingencyContext.createAllContingencyContext());
    }

    @Test
    public void testGetters() {
        ContingencyContext context = ContingencyContext.createAllContingencyContext();
        String functionId = "86";
        String variableId = "1664";
        BranchIntensityPerPSTAngle factor = new BranchIntensityPerPSTAngle(functionId, variableId, context);
        Assert.assertSame(context, factor.getContingencyContext());
        Assert.assertEquals(functionId, factor.getFunctionId());
        Assert.assertEquals(SensitivityFunctionType.BRANCH_CURRENT, factor.getFunctionType());
        Assert.assertEquals(functionId, factor.getFunctionId());
        Assert.assertEquals(SensitivityVariableType.TRANSFORMER_PHASE, factor.getVariableType());
        Assert.assertEquals(variableId, factor.getVariableId());
        Assert.assertFalse(factor.isVariableSet());
    }
}
