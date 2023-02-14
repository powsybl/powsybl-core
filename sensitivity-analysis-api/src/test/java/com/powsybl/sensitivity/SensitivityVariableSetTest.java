/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sensitivity.json.JsonSensitivityAnalysisParameters;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SensitivityVariableSetTest extends AbstractConverterTest {

    private static final double EPSILON_COMPARISON = 1e-5;

    @Test
    void test() {
        WeightedSensitivityVariable variable = new WeightedSensitivityVariable("v1", 3.4);
        assertEquals("v1", variable.getId());
        assertEquals(3.4, variable.getWeight(), EPSILON_COMPARISON);
        assertEquals("WeightedSensitivityVariable(id='v1', weight=3.4)", variable.toString());
        SensitivityVariableSet variableSet = new SensitivityVariableSet("id", List.of(variable));
        assertEquals("id", variableSet.getId());
        assertEquals(1, variableSet.getVariables().size());
        assertNotNull(variableSet.getVariables().stream().findFirst());
        assertTrue(variableSet.getVariablesById().containsKey("v1"));
        assertEquals("v1", variableSet.getVariables().stream().findFirst().get().getId());
        assertEquals("v1", variableSet.getVariable("v1").getId());
        assertEquals(3.4, variableSet.getVariable("v1").getWeight(), EPSILON_COMPARISON);
        assertEquals("SensitivityVariableSet(id='id', variables={v1=WeightedSensitivityVariable(id='v1', weight=3.4)})", variableSet.toString());
    }

    @Test
    void testKeepInsertionOrder() {
        SensitivityVariableSet variableSet = new SensitivityVariableSet("id", List.of(new WeightedSensitivityVariable("firstV", 3.4),
                new WeightedSensitivityVariable("secondV", 2.1), new WeightedSensitivityVariable("bVariable", 4.1),
                new WeightedSensitivityVariable("aVariable", 6.1)));

        assertEquals(4, variableSet.getVariables().size());
        assertEquals("[firstV, secondV, bVariable, aVariable]", variableSet.getVariablesById().keySet().toString());
        List<WeightedSensitivityVariable> list = new ArrayList(variableSet.getVariables());
        assertEquals("firstV", list.get(0).getId());
        assertEquals("secondV", list.get(1).getId());
        assertEquals("bVariable", list.get(2).getId());
        assertEquals("aVariable", list.get(3).getId());
    }

    @Test
    void testJson() throws IOException {
        SensitivityVariableSet variableSet = new SensitivityVariableSet("id", List.of(new WeightedSensitivityVariable("v1", 3.4),
                                                                                      new WeightedSensitivityVariable("v2", 2.1)));
        ObjectMapper objectMapper = JsonSensitivityAnalysisParameters.createObjectMapper();
        roundTripTest(variableSet, (variableSet2, jsonFile) -> JsonUtil.writeJson(jsonFile, variableSet, objectMapper),
            jsonFile -> JsonUtil.readJson(jsonFile, SensitivityVariableSet.class, objectMapper), "/variableSetRef.json");
    }
}
