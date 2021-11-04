/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

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
        SensitivityFactor factor1 = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER, "l",
                                                          SensitivityVariableType.INJECTION_ACTIVE_POWER, "g",
                                                          false, ContingencyContext.all());
        SensitivityFactor factor2 = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER, "l2",
                                                          SensitivityVariableType.INJECTION_ACTIVE_POWER, "g2",
                                                          false, ContingencyContext.none());
        List<SensitivityFactor> factors = List.of(factor1, factor2);
        List<Contingency> contingencies = List.of(new Contingency("NHV1_NHV2_2", new BranchContingency("NHV1_NHV2_2")));
        SensitivityValue value1 = new SensitivityValue(0, 0, 1d, 2d);
        SensitivityValue value2 = new SensitivityValue(1, -1, 3d, 4d);
        List<SensitivityValue> values = List.of(value1, value2);
        SensitivityAnalysisResult result = new SensitivityAnalysisResult(factors, contingencies, values);
        assertSame(factors, result.getFactors());
        assertSame(contingencies, result.getContingencies());
        assertEquals(2, result.getValues().size());
        assertEquals(1, result.getValues("NHV1_NHV2_2").size());
        assertSame(value1, result.getValue("NHV1_NHV2_2", "l", "g"));
        assertEquals(2d, result.getFunctionReferenceValue("NHV1_NHV2_2", "l"), 0d);
        assertTrue(Double.isNaN(result.getFunctionReferenceValue("NHV1_NHV2_2", "llll")));
        assertNull(result.getValue("NHV1_NHV2_2", "l1", "g"));
        assertSame(value2, result.getValue(null, "l2", "g2"));
        assertEquals(1, result.getPreContingencyValues().size());
    }
}
