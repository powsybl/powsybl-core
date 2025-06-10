/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.sensitivity.json.JsonSensitivityAnalysisParameters;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SensitivityFactorTest extends AbstractSerDeTest {

    @Test
    void test() {
        SensitivityFactor factor = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, "l",
                                                         SensitivityVariableType.INJECTION_ACTIVE_POWER, "g",
                                                         false, ContingencyContext.all());
        assertEquals(ContingencyContext.all(), factor.getContingencyContext());
        assertEquals(SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, factor.getFunctionType());
        assertEquals(1, factor.getFunctionType().getSide().orElse(0));
        assertEquals("l", factor.getFunctionId());
        assertEquals(SensitivityVariableType.INJECTION_ACTIVE_POWER, factor.getVariableType());
        assertEquals(OptionalInt.empty(), factor.getVariableType().getSide());
        assertEquals("g", factor.getVariableId());
        assertFalse(factor.isVariableSet());
        assertEquals("SensitivityFactor(functionType=BRANCH_ACTIVE_POWER_1, functionId='l', variableType=INJECTION_ACTIVE_POWER, variableId='g', variableSet=false, contingencyContext=ContingencyContext(contingencyId='', contextType=ALL))", factor.toString());
    }

    @Test
    void test2() {
        SensitivityFactor factor1 = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, "l",
                SensitivityVariableType.TRANSFORMER_PHASE_1, "ptc1",
                false, ContingencyContext.all());
        assertEquals(ContingencyContext.all(), factor1.getContingencyContext());
        assertEquals("l", factor1.getFunctionId());
        assertEquals(SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, factor1.getFunctionType());
        assertEquals(1, factor1.getFunctionType().getSide().orElse(0));
        assertEquals(SensitivityVariableType.TRANSFORMER_PHASE_1, factor1.getVariableType());
        assertEquals(1, factor1.getVariableType().getSide().orElse(0));
        assertEquals("ptc1", factor1.getVariableId());
        assertFalse(factor1.isVariableSet());
        assertEquals("SensitivityFactor(functionType=BRANCH_ACTIVE_POWER_1, functionId='l', variableType=TRANSFORMER_PHASE_1, variableId='ptc1', variableSet=false, contingencyContext=ContingencyContext(contingencyId='', contextType=ALL))", factor1.toString());
    }

    @Test
    void testMatrix() {
        List<SensitivityFactor> factors = SensitivityFactor.createMatrix(SensitivityFunctionType.BRANCH_ACTIVE_POWER_2, List.of("l12", "l13", "l23"),
                                                                         SensitivityVariableType.HVDC_LINE_ACTIVE_POWER, List.of("hvdc34"),
                                                                         false, ContingencyContext.all());
        assertEquals(3, factors.size());
    }

    @Test
    void testJson() throws IOException {
        SensitivityFactor factor = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER_2, "l",
                                                         SensitivityVariableType.INJECTION_ACTIVE_POWER, "g",
                                                         false, ContingencyContext.all());
        ObjectMapper objectMapper = JsonSensitivityAnalysisParameters.createObjectMapper();
        roundTripTest(factor, (factor1, jsonFile) -> JsonUtil.writeJson(jsonFile, factor1, objectMapper),
            jsonFile -> JsonUtil.readJson(jsonFile, SensitivityFactor.class, objectMapper), "/factorRef.json");
    }

    @Test
    void testNullVariableSet() throws IOException {
        String json = """
            {
              "functionType": "BUS_VOLTAGE",
              "functionId": "branch1",
              "variableType": "BUS_TARGET_VOLTAGE",
              "variableId": "gen1",
              "contingencyContextType": "NONE"
            }
            """;

        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(new StringReader(json));
        parser.nextToken();
        assertThrows(NullPointerException.class, SensitivityFactor.parseJson(parser));
    }
}
