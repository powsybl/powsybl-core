/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.contingency.ContingencyContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class SensitivityFactorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetters() {
        ContingencyContext context = ContingencyContext.all();
        String functionId = "86";
        String variableId = "1664";
        SensitivityFactor factor = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER, functionId, SensitivityVariableType.TRANSFORMER_PHASE, variableId, true, context);
        assertSame(context, factor.getContingencyContext());
        assertEquals(functionId, factor.getFunctionId());
        assertEquals(SensitivityFunctionType.BRANCH_ACTIVE_POWER, factor.getFunctionType());
        assertEquals(functionId, factor.getFunctionId());
        assertEquals(SensitivityVariableType.TRANSFORMER_PHASE, factor.getVariableType());
        assertEquals(variableId, factor.getVariableId());
        assertTrue(factor.isVariableSet());
        assertEquals("SensitivityFactor(functionType=BRANCH_ACTIVE_POWER, functionId='86', variableType=TRANSFORMER_PHASE, variableId='1664', variableSet=true, contingencyContext=ContingencyContext(contingencyId='', contextType=ALL))", factor.toString());

        List<SensitivityFactor> factors = SensitivityFactor.createMatrix(SensitivityFunctionType.BRANCH_ACTIVE_POWER, List.of("l12", "l13", "l23"),
                SensitivityVariableType.HVDC_LINE_ACTIVE_POWER, List.of("hvdc34"), false, ContingencyContext.all());
        assertEquals(3, factors.size());
    }
}
