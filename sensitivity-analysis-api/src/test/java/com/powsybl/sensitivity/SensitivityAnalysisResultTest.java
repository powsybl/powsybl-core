/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SensitivityAnalysisResultTest {

    @Test
    public void test() {
        SensitivityFactor factor1 = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, "l",
                                                          SensitivityVariableType.INJECTION_ACTIVE_POWER, "g",
                                                          false, ContingencyContext.all());
        SensitivityFactor factor2 = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, "l2",
                                                          SensitivityVariableType.INJECTION_ACTIVE_POWER, "g2",
                                                          false, ContingencyContext.none());
        SensitivityFactor factor3 = new SensitivityFactor(SensitivityFunctionType.BRANCH_CURRENT_1, "l",
                SensitivityVariableType.INJECTION_ACTIVE_POWER, "g",
                false, ContingencyContext.all());
        SensitivityFactor factor4 = new SensitivityFactor(SensitivityFunctionType.BRANCH_CURRENT_1, "l2",
                SensitivityVariableType.INJECTION_ACTIVE_POWER, "g2",
                false, ContingencyContext.none());

        List<SensitivityFactor> factors = List.of(factor1, factor2, factor3, factor4);
        List<Contingency> contingencies = List.of(new Contingency("NHV1_NHV2_2", new BranchContingency("NHV1_NHV2_2")));
        SensitivityValue value1 = new SensitivityValue(0, 0, 1d, 2d);
        SensitivityValue value2 = new SensitivityValue(1, -1, 3d, 4d);
        SensitivityValue value3 = new SensitivityValue(2, 0, 1d, 2d);
        SensitivityValue value4 = new SensitivityValue(3, -1, 3d, 4d);
        List<SensitivityValue> values = List.of(value1, value2, value3, value4);
        SensitivityAnalysisResult result = new SensitivityAnalysisResult(factors, contingencies, values);
        assertEquals(4, result.getValues().size());
        assertEquals(2, result.getValues("NHV1_NHV2_2").size());

        assertEquals(1d, result.getSensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityFunctionType.BRANCH_ACTIVE_POWER_1), 0d);
        assertEquals(2d, result.getFunctionReferenceValue("NHV1_NHV2_2", "l", SensitivityFunctionType.BRANCH_ACTIVE_POWER_1), 0d);
        assertEquals(1d, result.getBranchFlow1SensitivityValue("NHV1_NHV2_2", "g", "l"), 0d);
        assertEquals(2d, result.getBranchFlow1FunctionReferenceValue("NHV1_NHV2_2", "l"), 0d);

        assertEquals(1d, result.getSensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityFunctionType.BRANCH_CURRENT_1), 0d);
        assertEquals(2d, result.getFunctionReferenceValue("NHV1_NHV2_2", "l", SensitivityFunctionType.BRANCH_CURRENT_1), 0d);
        assertEquals(1d, result.getBranchCurrent1SensitivityValue("NHV1_NHV2_2", "g", "l"), 0d);
        assertEquals(2d, result.getBranchCurrent1FunctionReferenceValue("NHV1_NHV2_2", "l"), 0d);

        assertThrows(PowsyblException.class, () -> result.getFunctionReferenceValue("NHV1_NHV2_2", "llll", SensitivityFunctionType.BRANCH_ACTIVE_POWER_1));
        assertThrows(PowsyblException.class, () -> result.getSensitivityValue("NHV1_NHV2_2", "g", "l1", SensitivityFunctionType.BRANCH_ACTIVE_POWER_1));
        assertEquals(3d, result.getSensitivityValue(null, "g2", "l2", SensitivityFunctionType.BRANCH_ACTIVE_POWER_1), 0d);
        assertEquals(3d, result.getBranchCurrent1SensitivityValue("g2", "l2"), 0d);
        assertEquals(4d, result.getBranchFlow1FunctionReferenceValue("l2"), 0d);
        assertEquals(4d, result.getBranchCurrent1FunctionReferenceValue("l2"), 0d);
        assertEquals(2, result.getPreContingencyValues().size());
    }

    @Test
    public void testSide2() {
        SensitivityFactor factor1 = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER_2, "l",
                SensitivityVariableType.INJECTION_ACTIVE_POWER, "g",
                false, ContingencyContext.all());
        SensitivityFactor factor2 = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER_2, "l2",
                SensitivityVariableType.INJECTION_ACTIVE_POWER, "g2",
                false, ContingencyContext.none());
        SensitivityFactor factor3 = new SensitivityFactor(SensitivityFunctionType.BRANCH_CURRENT_2, "l",
                SensitivityVariableType.INJECTION_ACTIVE_POWER, "g",
                false, ContingencyContext.all());
        SensitivityFactor factor4 = new SensitivityFactor(SensitivityFunctionType.BRANCH_CURRENT_2, "l2",
                SensitivityVariableType.INJECTION_ACTIVE_POWER, "g2",
                false, ContingencyContext.none());

        List<SensitivityFactor> factors = List.of(factor1, factor2, factor3, factor4);
        List<Contingency> contingencies = List.of(new Contingency("NHV1_NHV2_2", new BranchContingency("NHV1_NHV2_2")));
        SensitivityValue value1 = new SensitivityValue(0, 0, 1d, 2d);
        SensitivityValue value2 = new SensitivityValue(1, -1, 3d, 4d);
        SensitivityValue value3 = new SensitivityValue(2, 0, 1d, 2d);
        SensitivityValue value4 = new SensitivityValue(3, -1, 3d, 4d);
        List<SensitivityValue> values = List.of(value1, value2, value3, value4);
        SensitivityAnalysisResult result = new SensitivityAnalysisResult(factors, contingencies, values);
        assertEquals(4, result.getValues().size());
        assertEquals(2, result.getValues("NHV1_NHV2_2").size());

        assertEquals(1d, result.getSensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityFunctionType.BRANCH_ACTIVE_POWER_2), 0d);
        assertEquals(2d, result.getFunctionReferenceValue("NHV1_NHV2_2", "l", SensitivityFunctionType.BRANCH_ACTIVE_POWER_2), 0d);
        assertEquals(1d, result.getBranchFlow2SensitivityValue("NHV1_NHV2_2", "g", "l"), 0d);
        assertEquals(2d, result.getBranchFlow2FunctionReferenceValue("NHV1_NHV2_2", "l"), 0d);

        assertEquals(1d, result.getSensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityFunctionType.BRANCH_CURRENT_2), 0d);
        assertEquals(2d, result.getFunctionReferenceValue("NHV1_NHV2_2", "l", SensitivityFunctionType.BRANCH_CURRENT_2), 0d);
        assertEquals(1d, result.getBranchCurrent2SensitivityValue("NHV1_NHV2_2", "g", "l"), 0d);
        assertEquals(2d, result.getBranchCurrent2FunctionReferenceValue("NHV1_NHV2_2", "l"), 0d);

        assertThrows(PowsyblException.class, () -> result.getFunctionReferenceValue("NHV1_NHV2_2", "llll", SensitivityFunctionType.BRANCH_ACTIVE_POWER_2));
        assertThrows(PowsyblException.class, () -> result.getSensitivityValue("NHV1_NHV2_2", "g", "l1", SensitivityFunctionType.BRANCH_ACTIVE_POWER_2));
        assertEquals(3d, result.getSensitivityValue(null, "g2", "l2", SensitivityFunctionType.BRANCH_ACTIVE_POWER_2), 0d);
        assertEquals(3d, result.getBranchCurrent2SensitivityValue("g2", "l2"), 0d);
        assertEquals(4d, result.getBranchFlow2FunctionReferenceValue("l2"), 0d);
        assertEquals(4d, result.getBranchCurrent2FunctionReferenceValue("l2"), 0d);
        assertEquals(2, result.getPreContingencyValues().size());
    }
}
