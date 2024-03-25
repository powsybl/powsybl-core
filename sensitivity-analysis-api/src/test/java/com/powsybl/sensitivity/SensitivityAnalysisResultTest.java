/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.*;
import com.powsybl.sensitivity.json.SensitivityJsonModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SensitivityAnalysisResultTest extends AbstractSerDeTest {

    @Test
    void testSide1() {
        SensitivityFactor factor1 = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, "l",
                                                          SensitivityVariableType.INJECTION_ACTIVE_POWER, "g",
                                                          false, ContingencyContext.all());
        SensitivityFactor factor2 = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, "l2",
                                                          SensitivityVariableType.INJECTION_ACTIVE_POWER, "g2",
                                                          false, ContingencyContext.none());
        SensitivityFactor factor3 = new SensitivityFactor(SensitivityFunctionType.BRANCH_CURRENT_1, "l",
                SensitivityVariableType.TRANSFORMER_PHASE_1, "g",
                false, ContingencyContext.all());
        SensitivityFactor factor4 = new SensitivityFactor(SensitivityFunctionType.BRANCH_CURRENT_1, "l2",
                SensitivityVariableType.TRANSFORMER_PHASE_2, "g2",
                false, ContingencyContext.none());
        SensitivityFactor factor5 = new SensitivityFactor(SensitivityFunctionType.BRANCH_CURRENT_1, "l2",
                SensitivityVariableType.TRANSFORMER_PHASE_3, "g2",
                false, ContingencyContext.none());

        List<SensitivityFactor> factors = List.of(factor1, factor2, factor3, factor4, factor5);
        factors.forEach(f -> assertEquals(1, f.getFunctionType().getSide().orElse(0)));

        SensitivityValue value1 = new SensitivityValue(0, 0, 1d, 2d);
        SensitivityValue value2 = new SensitivityValue(1, -1, 3d, 4d);
        SensitivityValue value3 = new SensitivityValue(2, 0, 1d, 2d);
        SensitivityValue value4 = new SensitivityValue(3, -1, 3d, 4d);
        SensitivityValue value5 = new SensitivityValue(4, -1, 4d, 4d);
        List<SensitivityAnalysisResult.SensitivityContingencyStatus> contingencyStatus = new ArrayList<>();
        contingencyStatus.add(new SensitivityAnalysisResult.SensitivityContingencyStatus("NHV1_NHV2_2", SensitivityAnalysisResult.Status.SUCCESS));
        contingencyStatus.add(new SensitivityAnalysisResult.SensitivityContingencyStatus("NHV2_NHV3", SensitivityAnalysisResult.Status.NO_IMPACT));

        List<SensitivityValue> values = List.of(value1, value2, value3, value4, value5);
        SensitivityAnalysisResult result = new SensitivityAnalysisResult(factors, contingencyStatus, values);
        assertEquals(5, result.getValues().size());
        assertEquals(2, result.getValues("NHV1_NHV2_2").size());

        assertEquals(1d, result.getSensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(2d, result.getFunctionReferenceValue("NHV1_NHV2_2", "l", SensitivityFunctionType.BRANCH_ACTIVE_POWER_1), 0d);
        assertEquals(1d, result.getBranchFlow1SensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(2d, result.getBranchFlow1FunctionReferenceValue("NHV1_NHV2_2", "l"), 0d);

        assertEquals(1d, result.getSensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityFunctionType.BRANCH_CURRENT_1, SensitivityVariableType.TRANSFORMER_PHASE_1), 0d);
        assertEquals(2d, result.getFunctionReferenceValue("NHV1_NHV2_2", "l", SensitivityFunctionType.BRANCH_CURRENT_1), 0d);
        assertEquals(1d, result.getBranchCurrent1SensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityVariableType.TRANSFORMER_PHASE_1), 0d);
        assertEquals(2d, result.getBranchCurrent1FunctionReferenceValue("NHV1_NHV2_2", "l"), 0d);

        assertThrows(PowsyblException.class, () -> result.getFunctionReferenceValue("NHV1_NHV2_2", "llll", SensitivityFunctionType.BRANCH_ACTIVE_POWER_1));
        assertThrows(PowsyblException.class, () -> result.getSensitivityValue("NHV1_NHV2_2", "g", "l1", SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, SensitivityVariableType.INJECTION_ACTIVE_POWER));
        assertEquals(3d, result.getSensitivityValue(null, "g2", "l2", SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(3d, result.getBranchFlow1SensitivityValue("g2", "l2", SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(3d, result.getSensitivityValue("g2", "l2", SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(3d, result.getBranchCurrent1SensitivityValue("g2", "l2", SensitivityVariableType.TRANSFORMER_PHASE_2), 0d);
        assertEquals(4d, result.getBranchCurrent1SensitivityValue("g2", "l2", SensitivityVariableType.TRANSFORMER_PHASE_3), 0d);
        assertEquals(4d, result.getBranchFlow1FunctionReferenceValue("l2"), 0d);
        assertEquals(4d, result.getBranchCurrent1FunctionReferenceValue("l2"), 0d);
        assertEquals(3, result.getPreContingencyValues().size());

        assertEquals(SensitivityAnalysisResult.Status.SUCCESS, result.getContingencyStatus("NHV1_NHV2_2"));
        assertEquals(SensitivityAnalysisResult.Status.NO_IMPACT, result.getContingencyStatus("NHV2_NHV3"));
    }

    @Test
    void testSide2() {
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
        SensitivityFactor factor5 = new SensitivityFactor(SensitivityFunctionType.BRANCH_CURRENT_2, "l2",
                SensitivityVariableType.TRANSFORMER_PHASE_3, "g2",
                false, ContingencyContext.none());

        List<SensitivityFactor> factors = List.of(factor1, factor2, factor3, factor4, factor5);
        factors.forEach(f -> assertEquals(2, f.getFunctionType().getSide().orElse(0)));

        List<Contingency> contingencies = List.of(new Contingency("NHV1_NHV2_2", new BranchContingency("NHV1_NHV2_2")));
        SensitivityValue value1 = new SensitivityValue(0, 0, 1d, 2d);
        SensitivityValue value2 = new SensitivityValue(1, -1, 3d, 4d);
        SensitivityValue value3 = new SensitivityValue(2, 0, 1d, 2d);
        SensitivityValue value4 = new SensitivityValue(3, -1, 3d, 4d);
        SensitivityValue value5 = new SensitivityValue(4, -1, 6d, 4d);
        List<SensitivityValue> values = List.of(value1, value2, value3, value4, value5);
        List<SensitivityAnalysisResult.SensitivityContingencyStatus> contingencyStatus = new ArrayList<>();
        contingencies.forEach(c -> contingencyStatus.add(new SensitivityAnalysisResult.SensitivityContingencyStatus(c.getId(), SensitivityAnalysisResult.Status.SUCCESS)));
        SensitivityAnalysisResult result = new SensitivityAnalysisResult(factors, contingencyStatus, values);
        assertEquals(5, result.getValues().size());
        assertEquals(2, result.getValues("NHV1_NHV2_2").size());

        assertEquals(1d, result.getSensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityFunctionType.BRANCH_ACTIVE_POWER_2, SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(2d, result.getFunctionReferenceValue("NHV1_NHV2_2", "l", SensitivityFunctionType.BRANCH_ACTIVE_POWER_2), 0d);
        assertEquals(1d, result.getBranchFlow2SensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(2d, result.getBranchFlow2FunctionReferenceValue("NHV1_NHV2_2", "l"), 0d);

        assertEquals(1d, result.getSensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityFunctionType.BRANCH_CURRENT_2, SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(2d, result.getFunctionReferenceValue("NHV1_NHV2_2", "l", SensitivityFunctionType.BRANCH_CURRENT_2), 0d);
        assertEquals(1d, result.getBranchCurrent2SensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(2d, result.getBranchCurrent2FunctionReferenceValue("NHV1_NHV2_2", "l"), 0d);

        assertThrows(PowsyblException.class, () -> result.getFunctionReferenceValue("NHV1_NHV2_2", "llll", SensitivityFunctionType.BRANCH_ACTIVE_POWER_2));
        assertThrows(PowsyblException.class, () -> result.getSensitivityValue("NHV1_NHV2_2", "g", "l1", SensitivityFunctionType.BRANCH_ACTIVE_POWER_2, SensitivityVariableType.INJECTION_ACTIVE_POWER));
        assertEquals(3d, result.getSensitivityValue(null, "g2", "l2", SensitivityFunctionType.BRANCH_ACTIVE_POWER_2, SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(3d, result.getBranchFlow2SensitivityValue("g2", "l2", SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(3d, result.getBranchCurrent2SensitivityValue("g2", "l2", SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(6d, result.getBranchCurrent2SensitivityValue("g2", "l2", SensitivityVariableType.TRANSFORMER_PHASE_3), 0d);
        assertEquals(4d, result.getBranchFlow2FunctionReferenceValue("l2"), 0d);
        assertEquals(4d, result.getBranchCurrent2FunctionReferenceValue("l2"), 0d);
        assertEquals(3, result.getPreContingencyValues().size());
    }

    @Test
    void testSide3() {
        SensitivityFactor factor1 = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER_3, "l",
                SensitivityVariableType.INJECTION_ACTIVE_POWER, "g",
                false, ContingencyContext.all());
        SensitivityFactor factor2 = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER_3, "l2",
                SensitivityVariableType.INJECTION_ACTIVE_POWER, "g2",
                false, ContingencyContext.none());
        SensitivityFactor factor3 = new SensitivityFactor(SensitivityFunctionType.BRANCH_CURRENT_3, "l",
                SensitivityVariableType.INJECTION_ACTIVE_POWER, "g",
                false, ContingencyContext.all());
        SensitivityFactor factor4 = new SensitivityFactor(SensitivityFunctionType.BRANCH_CURRENT_3, "l2",
                SensitivityVariableType.INJECTION_ACTIVE_POWER, "g2",
                false, ContingencyContext.none());
        SensitivityFactor factor5 = new SensitivityFactor(SensitivityFunctionType.BRANCH_CURRENT_3, "l2",
                SensitivityVariableType.TRANSFORMER_PHASE_1, "g2",
                false, ContingencyContext.none());

        List<SensitivityFactor> factors = List.of(factor1, factor2, factor3, factor4, factor5);
        factors.forEach(f -> assertEquals(3, f.getFunctionType().getSide().orElse(0)));

        List<Contingency> contingencies = List.of(new Contingency("NHV1_NHV2_2", new BranchContingency("NHV1_NHV2_2")));
        SensitivityValue value1 = new SensitivityValue(0, 0, 2d, 2d);
        SensitivityValue value2 = new SensitivityValue(1, -1, 6d, 4d);
        SensitivityValue value3 = new SensitivityValue(2, 0, 2d, 2d);
        SensitivityValue value4 = new SensitivityValue(3, -1, 6d, 4d);
        SensitivityValue value5 = new SensitivityValue(4, -1, 12d, 4d);
        List<SensitivityValue> values = List.of(value1, value2, value3, value4, value5);
        List<SensitivityAnalysisResult.SensitivityContingencyStatus> contingencyStatus = new ArrayList<>();
        contingencies.forEach(c -> contingencyStatus.add(new SensitivityAnalysisResult.SensitivityContingencyStatus(c.getId(), SensitivityAnalysisResult.Status.SUCCESS)));
        SensitivityAnalysisResult result = new SensitivityAnalysisResult(factors, contingencyStatus, values);
        assertEquals(5, result.getValues().size());
        assertEquals(2, result.getValues("NHV1_NHV2_2").size());

        assertEquals(2d, result.getSensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityFunctionType.BRANCH_ACTIVE_POWER_3, SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(2d, result.getFunctionReferenceValue("NHV1_NHV2_2", "l", SensitivityFunctionType.BRANCH_ACTIVE_POWER_3), 0d);
        assertEquals(2d, result.getBranchFlow3SensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(2d, result.getBranchFlow3FunctionReferenceValue("NHV1_NHV2_2", "l"), 0d);

        assertEquals(2d, result.getSensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityFunctionType.BRANCH_CURRENT_3, SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(2d, result.getFunctionReferenceValue("NHV1_NHV2_2", "l", SensitivityFunctionType.BRANCH_CURRENT_3), 0d);
        assertEquals(2d, result.getBranchCurrent3SensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(2d, result.getBranchCurrent3FunctionReferenceValue("NHV1_NHV2_2", "l"), 0d);
        assertEquals(4d, result.getFunctionReferenceValue("l2", SensitivityFunctionType.BRANCH_CURRENT_3), 0d);

        assertThrows(PowsyblException.class, () -> result.getFunctionReferenceValue("NHV1_NHV2_2", "llll", SensitivityFunctionType.BRANCH_ACTIVE_POWER_3));
        assertThrows(PowsyblException.class, () -> result.getSensitivityValue("NHV1_NHV2_2", "g", "l1", SensitivityFunctionType.BRANCH_ACTIVE_POWER_3, SensitivityVariableType.INJECTION_ACTIVE_POWER));
        assertEquals(6d, result.getSensitivityValue(null, "g2", "l2", SensitivityFunctionType.BRANCH_ACTIVE_POWER_3, SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(6d, result.getBranchFlow3SensitivityValue("g2", "l2", SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(6d, result.getBranchCurrent3SensitivityValue("g2", "l2", SensitivityVariableType.INJECTION_ACTIVE_POWER), 0d);
        assertEquals(12d, result.getBranchCurrent3SensitivityValue("g2", "l2", SensitivityVariableType.TRANSFORMER_PHASE_1), 0d);
        assertEquals(4d, result.getBranchFlow3FunctionReferenceValue("l2"), 0d);
        assertEquals(4d, result.getBranchCurrent3FunctionReferenceValue("l2"), 0d);
        assertEquals(3, result.getPreContingencyValues().size());
    }

    @Test
    void testBusVoltage() {
        SensitivityFactor factor1 = new SensitivityFactor(SensitivityFunctionType.BUS_VOLTAGE, "l",
                SensitivityVariableType.BUS_TARGET_VOLTAGE, "g",
                false, ContingencyContext.all());
        SensitivityFactor factor2 = new SensitivityFactor(SensitivityFunctionType.BUS_VOLTAGE, "l2",
                SensitivityVariableType.BUS_TARGET_VOLTAGE, "g2",
                false, ContingencyContext.none());
        SensitivityFactor factor3 = new SensitivityFactor(SensitivityFunctionType.BUS_VOLTAGE, "l2",
                SensitivityVariableType.TRANSFORMER_PHASE_1, "g2",
                false, ContingencyContext.none());
        SensitivityFactor factor4 = new SensitivityFactor(SensitivityFunctionType.BUS_VOLTAGE, "l3",
                SensitivityVariableType.TRANSFORMER_PHASE_2, "g2",
                false, ContingencyContext.none());

        List<SensitivityFactor> factors = List.of(factor1, factor2, factor3, factor4);
        List<Contingency> contingencies = List.of(new Contingency("NHV1_NHV2_2", new BranchContingency("NHV1_NHV2_2")));
        SensitivityValue value1 = new SensitivityValue(0, 0, 1d, 2d);
        SensitivityValue value2 = new SensitivityValue(1, -1, 3d, 4d);
        SensitivityValue value3 = new SensitivityValue(2, -1, 6d, 4d);
        SensitivityValue value4 = new SensitivityValue(3, -1, 12d, 6d);

        List<SensitivityValue> values = List.of(value1, value2, value3, value4);
        List<SensitivityAnalysisResult.SensitivityContingencyStatus> contingencyStatus = new ArrayList<>();
        contingencies.forEach(c -> contingencyStatus.add(new SensitivityAnalysisResult.SensitivityContingencyStatus(c.getId(), SensitivityAnalysisResult.Status.SUCCESS)));
        SensitivityAnalysisResult result = new SensitivityAnalysisResult(factors, contingencyStatus, values);
        assertEquals(4, result.getValues().size());
        assertEquals(1, result.getValues("NHV1_NHV2_2").size());

        assertEquals(1d, result.getSensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityFunctionType.BUS_VOLTAGE, SensitivityVariableType.BUS_TARGET_VOLTAGE), 0d);
        assertEquals(2d, result.getFunctionReferenceValue("NHV1_NHV2_2", "l", SensitivityFunctionType.BUS_VOLTAGE), 0d);
        assertEquals(4d, result.getFunctionReferenceValue("l2", SensitivityFunctionType.BUS_VOLTAGE), 0d);
        assertEquals(4d, result.getBusVoltageFunctionReferenceValue("l2"), 0d);
        assertEquals(1d, result.getBusVoltageSensitivityValue("NHV1_NHV2_2", "g", "l", SensitivityVariableType.BUS_TARGET_VOLTAGE), 0d);
        assertEquals(3d, result.getBusVoltageSensitivityValue("g2", "l2", SensitivityVariableType.BUS_TARGET_VOLTAGE), 0d);
        assertEquals(6d, result.getBusVoltageSensitivityValue("g2", "l2", SensitivityVariableType.TRANSFORMER_PHASE_1), 0d);
        assertEquals(6d, result.getBusVoltageFunctionReferenceValue("l3"), 0d);
        assertEquals(12d, result.getBusVoltageSensitivityValue("g2", "l3", SensitivityVariableType.TRANSFORMER_PHASE_2), 0d);
        assertEquals(2d, result.getBusVoltageFunctionReferenceValue("NHV1_NHV2_2", "l"), 0d);
    }

    @Test
    void testCompareSensivitiyValueKeysEqualsNotEquals() {
        SensitivityValueKey key1 = new SensitivityValueKey(null, "l1", "g1", SensitivityFunctionType.BRANCH_CURRENT_1, SensitivityVariableType.BUS_TARGET_VOLTAGE);
        SensitivityValueKey key2 = new SensitivityValueKey("c1", "l1", "g1", SensitivityFunctionType.BRANCH_CURRENT_1, SensitivityVariableType.BUS_TARGET_VOLTAGE);
        SensitivityValueKey key3 = new SensitivityValueKey(null, "l1", "g1", SensitivityFunctionType.BRANCH_CURRENT_2, SensitivityVariableType.BUS_TARGET_VOLTAGE);
        SensitivityValueKey key4 = new SensitivityValueKey(null, "l2", "g1", SensitivityFunctionType.BRANCH_CURRENT_2, SensitivityVariableType.BUS_TARGET_VOLTAGE);
        SensitivityValueKey key5 = new SensitivityValueKey(null, "l2", "g3", SensitivityFunctionType.BRANCH_CURRENT_2, SensitivityVariableType.BUS_TARGET_VOLTAGE);
        SensitivityValueKey key6 = new SensitivityValueKey(null, "l2", "g3", SensitivityFunctionType.BRANCH_CURRENT_2, SensitivityVariableType.TRANSFORMER_PHASE);

        assertEquals(key1, key1);
        assertNotEquals(key1, key2);
        assertNotEquals(key2, key3);
        assertNotEquals(key3, key4);
        assertNotEquals(key4, key5);
        assertNotEquals(key5, key6);

        String dummy = "dummy";
        assertNotEquals(key1, dummy);
    }

    @Test
    void testSerializeDeserialize() throws IOException {
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
        contingencies.forEach(c -> contingencyStatus.add(new SensitivityAnalysisResult.SensitivityContingencyStatus(c.getId(), SensitivityAnalysisResult.Status.SUCCESS)));
        SensitivityAnalysisResult result = new SensitivityAnalysisResult(factors, contingencyStatus, values);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper().registerModule(new SensitivityJsonModule());
        roundTripTest(result, (result2, jsonFile) -> JsonUtil.writeJson(jsonFile, result, objectMapper),
            jsonFile -> JsonUtil.readJson(jsonFile, SensitivityAnalysisResult.class, objectMapper), "/SensitivityAnalysisResultRefV1.json");
    }
}
