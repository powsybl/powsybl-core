/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.contingency.ContingencyContext;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SensitivityFactorTest extends AbstractConverterTest {

    @Test
    public void test() {
        SensitivityFactor factor = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER, "l",
                                                         SensitivityVariableType.INJECTION_ACTIVE_POWER, "g",
                                                         false, ContingencyContext.all());
        assertEquals(ContingencyContext.all(), factor.getContingencyContext());
        assertEquals("l", factor.getFunctionId());
        assertEquals(SensitivityFunctionType.BRANCH_ACTIVE_POWER, factor.getFunctionType());
        assertEquals("l", factor.getFunctionId());
        assertEquals(SensitivityVariableType.INJECTION_ACTIVE_POWER, factor.getVariableType());
        assertEquals("g", factor.getVariableId());
        assertFalse(factor.isVariableSet());
        assertEquals("SensitivityFactor(functionType=BRANCH_ACTIVE_POWER, functionId='l', variableType=INJECTION_ACTIVE_POWER, variableId='g', variableSet=false, contingencyContext=ContingencyContext(contingencyId='', contextType=ALL))", factor.toString());
    }

    @Test
    public void testMatrix() {
        List<SensitivityFactor> factors = SensitivityFactor.createMatrix(SensitivityFunctionType.BRANCH_ACTIVE_POWER, List.of("l12", "l13", "l23"),
                                                                         SensitivityVariableType.HVDC_LINE_ACTIVE_POWER, List.of("hvdc34"),
                                                                         false, ContingencyContext.all());
        assertEquals(3, factors.size());
    }

    @Test
    public void testJson() throws IOException {
        SensitivityFactor factor = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER, "l",
                                                         SensitivityVariableType.INJECTION_ACTIVE_POWER, "g",
                                                         false, ContingencyContext.all());
        List<SensitivityFactor> factors = List.of(factor);
        roundTripTest(factors, (factors2, jsonFile) -> SensitivityFactor.writeJson(jsonFile, factors2), SensitivityFactor::readJson, "/factorsRef.json");
    }
}
