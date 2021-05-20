/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class SensitivityFactorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetters() {
        ContingencyContext context = ContingencyContext.createAllContingencyContext();
        String functionId = "86";
        String variableId = "1664";
        SensitivityFactor factor = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER, functionId, SensitivityVariableType.TRANSFORMER_PHASE, variableId, true, context);
        Assert.assertSame(context, factor.getContingencyContext());
        Assert.assertEquals(functionId, factor.getFunctionId());
        Assert.assertEquals(SensitivityFunctionType.BRANCH_ACTIVE_POWER, factor.getFunctionType());
        Assert.assertEquals(functionId, factor.getFunctionId());
        Assert.assertEquals(SensitivityVariableType.TRANSFORMER_PHASE, factor.getVariableType());
        Assert.assertEquals(variableId, factor.getVariableId());
        Assert.assertTrue(factor.isVariableSet());
    }
}
