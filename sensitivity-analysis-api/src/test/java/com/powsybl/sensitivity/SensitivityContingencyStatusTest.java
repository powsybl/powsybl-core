/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.powsybl.loadflow.LoadFlowResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
class SensitivityContingencyStatusTest {

    private final JsonFactory factory = new JsonFactory();

    @Test
    void parseStateStatusContingencyOnly() throws Exception {
        String json = """
            {
              "contingencyId": "ID_001",
              "componentsLoadFlowStatuses": [
                {
                  "loadFlowStatus": "CONVERGED",
                  "loadFlowStatusDescription": "TestConvergence",
                  "numCC": 0,
                  "numCS": 0
                }
              ]
            }
            """;
        try (JsonParser parser = factory.createParser(json)) {
            parser.nextToken();
            SensitivityAnalysisResult.SensitivityStateStatus stateStatus =
                    SensitivityAnalysisResult.SensitivityStateStatus.parseJson(parser, "1.2");
            assertEquals("ID_001", stateStatus.getState().contingencyId());
            assertNull(stateStatus.getState().operatorStrategyId());
            assertEquals(LoadFlowResult.ComponentResult.Status.CONVERGED, stateStatus.getComponentsLoadFlowStatusList().getFirst().status().status());
            assertEquals(1, stateStatus.getComponentsLoadFlowStatusList().size());
        }
    }

    @Test
    void parseStateStatusWithComponentsLoadFlowStatuses() throws Exception {
        String json = """
            {
              "contingencyId": "ID_001",
              "componentsLoadFlowStatuses": [
                {
                  "loadFlowStatus": "CONVERGED",
                  "loadFlowStatusDescription": "TestConvergence",
                  "numCC": 5,
                  "numCS": 2
                }
              ]
            }
            """;
        try (JsonParser parser = factory.createParser(json)) {
            parser.nextToken();
            SensitivityAnalysisResult.SensitivityStateStatus stateStatus =
                    SensitivityAnalysisResult.SensitivityStateStatus.parseJson(parser, "1.2");

            assertEquals("ID_001", stateStatus.getState().contingencyId());
            assertEquals(1, stateStatus.getComponentsLoadFlowStatusList().size());
            SensitivityAnalysisResult.SensitivityStateStatus.ComponentStatus triple =
                    stateStatus.getComponentsLoadFlowStatusList().getFirst();
            assertEquals(LoadFlowResult.ComponentResult.Status.CONVERGED, triple.status().status());
            assertEquals("TestConvergence", triple.status().statusText());
            assertEquals(5, triple.numCC());
            assertEquals(2, triple.numSC());
        }
    }

    @Test
    void parsePreContingencyStateStatus() throws Exception {
        String json = """
            {
              "componentsLoadFlowStatuses": [
                {
                  "loadFlowStatus": "CONVERGED",
                  "loadFlowStatusDescription": "TestStatusText",
                  "numCC": 5,
                  "numCS": 2
                }
              ]
            }
            """;
        try (JsonParser parser = factory.createParser(json)) {
            parser.nextToken();
            SensitivityAnalysisResult.SensitivityStateStatus stateStatus =
                    SensitivityAnalysisResult.SensitivityStateStatus.parseJson(parser, "1.2");

            assertEquals(SensitivityState.PRE_CONTINGENCY, stateStatus.getState());
            assertEquals(1, stateStatus.getComponentsLoadFlowStatusList().size());
            SensitivityAnalysisResult.SensitivityStateStatus.ComponentStatus triple =
                    stateStatus.getComponentsLoadFlowStatusList().getFirst();
            assertEquals(LoadFlowResult.ComponentResult.Status.CONVERGED, triple.status().status());
            assertEquals("TestStatusText", triple.status().statusText());
            assertEquals(5, triple.numCC());
            assertEquals(2, triple.numSC());
        }
    }
}
