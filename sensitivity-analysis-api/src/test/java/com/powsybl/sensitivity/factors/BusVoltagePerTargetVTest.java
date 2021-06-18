/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.sensitivity.SensitivityFunctionType;
import com.powsybl.sensitivity.SensitivityVariableType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class BusVoltagePerTargetVTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void checkFailsWhenNullFunction() {
        exception.expect(NullPointerException.class);
        new BusVoltagePerTargetV(null, "12", ContingencyContext.all());
    }

    @Test
    public void checkFailsWhenNullVariable() {
        exception.expect(NullPointerException.class);
        new BusVoltagePerTargetV("12", null, ContingencyContext.all());
    }

    @Test
    public void testGetters() {
        ContingencyContext context = ContingencyContext.all();
        String functionId = "86";
        String variableId = "1664";
        BusVoltagePerTargetV factor = new BusVoltagePerTargetV(functionId, variableId, context);
        Assert.assertSame(context, factor.getContingencyContext());
        Assert.assertEquals(functionId, factor.getFunctionId());
        Assert.assertEquals(SensitivityFunctionType.BUS_VOLTAGE, factor.getFunctionType());
        Assert.assertEquals(functionId, factor.getFunctionId());
        Assert.assertEquals(SensitivityVariableType.BUS_TARGET_VOLTAGE, factor.getVariableType());
        Assert.assertEquals(variableId, factor.getVariableId());
        Assert.assertFalse(factor.isVariableSet());
    }
}
