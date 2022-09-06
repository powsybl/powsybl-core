/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.*;
import com.powsybl.sensitivity.json.SensitivityJsonModule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SensitivityAnalysisResultTest extends AbstractConverterTest {

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
        List<SensitivityAnalysisResult.SensitivityContingencyStatus> contingencyStatus = new ArrayList<>();
        contingencies.forEach(c -> contingencyStatus.add(new SensitivityAnalysisResult.SensitivityContingencyStatus(c, SensitivityAnalysisResult.Status.CONVERGED)));

        List<SensitivityValue> values = List.of(value1, value2, value3, value4);
        SensitivityAnalysisResult result = new SensitivityAnalysisResult(factors, contingencies, contingencyStatus, values);
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
        assertEquals(3d, result.getSensitivityValue("g2", "l2", SensitivityFunctionType.BRANCH_ACTIVE_POWER_1), 0d);
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
        List<SensitivityAnalysisResult.SensitivityContingencyStatus> contingencyStatus = new ArrayList<>();
        contingencies.forEach(c -> contingencyStatus.add(new SensitivityAnalysisResult.SensitivityContingencyStatus(c, SensitivityAnalysisResult.Status.CONVERGED)));
        SensitivityAnalysisResult result = new SensitivityAnalysisResult(factors, contingencies, contingencyStatus, values);
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

    @Test
    public void testBusVoltage() {
        SensitivityFactor factor1 = new SensitivityFactor(SensitivityFunctionType.BUS_VOLTAGE, "l",
                SensitivityVariableType.BUS_TARGET_VOLTAGE, "g",
                false, ContingencyContext.all());
        SensitivityFactor factor2 = new SensitivityFactor(SensitivityFunctionType.BUS_VOLTAGE, "l2",
                SensitivityVariableType.BUS_TARGET_VOLTAGE, "g2",
                false, ContingencyContext.none());

        List<SensitivityFactor> factors = List.of(factor1, factor2);
        List<Contingency> contingencies = List.of(new Contingency("NHV1_NHV2_2", new BranchContingency("NHV1_NHV2_2")));
        SensitivityValue value1 = new SensitivityValue(0, 0, 1d, 2d);
        SensitivityValue value2 = new SensitivityValue(1, -1, 3d, 4d);
        List<SensitivityValue> values = List.of(value1, value2);
        List<SensitivityAnalysisResult.SensitivityContingencyStatus> contingencyStatus = new ArrayList<>();
        contingencies.forEach(c -> contingencyStatus.add(new SensitivityAnalysisResult.SensitivityContingencyStatus(c, SensitivityAnalysisResult.Status.CONVERGED)));
        SensitivityAnalysisResult result = new SensitivityAnalysisResult(factors, contingencies, contingencyStatus, values);
        assertEquals(2, result.getValues().size());
        assertEquals(1, result.getValues("NHV1_NHV2_2").size());

        assertEquals(1d, result.getSensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityFunctionType.BUS_VOLTAGE), 0d);
        assertEquals(2d, result.getFunctionReferenceValue("NHV1_NHV2_2", "l", SensitivityFunctionType.BUS_VOLTAGE), 0d);
        assertEquals(1d, result.getBusVoltageSensitivityValue("NHV1_NHV2_2", "g", "l"), 0d);
        assertEquals(2d, result.getBusVoltageFunctionReferenceValue("NHV1_NHV2_2", "l"), 0d);
    }

    @Test
    public void testCompareSensivitiyValueKeysEqualsNotEquals() {
        SensitivityValueKey key1 = new SensitivityValueKey(null, "l1", "g1", SensitivityFunctionType.BRANCH_CURRENT_1);
        SensitivityValueKey key2 = new SensitivityValueKey("c1", "l1", "g1", SensitivityFunctionType.BRANCH_CURRENT_1);
        SensitivityValueKey key3 = new SensitivityValueKey(null, "l1", "g1", SensitivityFunctionType.BRANCH_CURRENT_2);
        SensitivityValueKey key4 = new SensitivityValueKey(null, "l2", "g1", SensitivityFunctionType.BRANCH_CURRENT_2);
        SensitivityValueKey key5 = new SensitivityValueKey(null, "l2", "g3", SensitivityFunctionType.BRANCH_CURRENT_2);

        assertEquals(key1, key1);
        assertNotEquals(key1, key2);
        assertNotEquals(key2, key3);
        assertNotEquals(key3, key4);
        assertNotEquals(key4, key5);

        String dummy = "dummy";
        assertNotEquals(key1, dummy);
    }

    @Test
    public void testSerializeDeserialize() throws IOException {
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

        SensitivityValue value1 = new SensitivityValue(0, 0, 1d, 2d);
        SensitivityValue value2 = new SensitivityValue(1, -1, 3d, 4d);
        SensitivityValue value3 = new SensitivityValue(2, 0, 1d, 2d);
        SensitivityValue value4 = new SensitivityValue(3, -1, 3d, 4d);
        List<SensitivityValue> values = List.of(value1, value2, value3, value4);

        List<Contingency> contingencies = List.of(new Contingency("NHV1_NHV2_2", new BranchContingency("NHV1_NHV2_2")));
        List<SensitivityAnalysisResult.SensitivityContingencyStatus> contingencyStatus = new ArrayList<>();
        contingencies.forEach(c -> contingencyStatus.add(new SensitivityAnalysisResult.SensitivityContingencyStatus(c, SensitivityAnalysisResult.Status.CONVERGED)));
        SensitivityAnalysisResult result = new SensitivityAnalysisResult(factors, contingencies, contingencyStatus, values);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper().registerModule(new SensitivityJsonModule());
        roundTripTest(result, (result2, jsonFile) -> JsonUtil.writeJson(jsonFile, result, objectMapper),
            jsonFile -> JsonUtil.readJson(jsonFile, SensitivityAnalysisResult.class, objectMapper), "/SensitivityAnalysisResultRefV1.json");
    }
}
