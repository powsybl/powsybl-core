package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.powsybl.loadflow.LoadFlowResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SensitivityContingencyStatusTest {

    private final JsonFactory factory = new JsonFactory();

    @Test
    void parseJsonSensitivityContingencyStatus1() throws Exception {
        // Arrange
        String json = "{\"contingencyId\": \"ID_001\"}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken();
        parser.nextToken();

        SensitivityAnalysisResult.SensitivityContingencyStatus.ParsingContext context = new SensitivityAnalysisResult.SensitivityContingencyStatus.ParsingContext();

        // Act
        SensitivityAnalysisResult.SensitivityContingencyStatus.parseJson(parser, context);

        // Assert
        assertEquals("ID_001", context.contingency.getId());
    }

    @Test
    void parseJsonSensitivityContingencyStatus2() throws Exception {
        // Arrange
        String json = """
            {
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
        JsonParser parser = factory.createParser(json);
        parser.nextToken();
        parser.nextToken();

        SensitivityAnalysisResult.SensitivityContingencyStatus.ParsingContext context = new SensitivityAnalysisResult.SensitivityContingencyStatus.ParsingContext();

        // Act
        SensitivityAnalysisResult.SensitivityContingencyStatus.parseJson(parser, context);

        // Assert
        assertEquals(1, context.componentLoadflowStatuses.size());
        var triple = context.componentLoadflowStatuses.getFirst();

        assertEquals(LoadFlowResult.ComponentResult.Status.CONVERGED, triple.getFirst().status());
        assertEquals("TestConvergence", triple.getFirst().statusText());
        assertEquals(5, triple.getSecond());
        assertEquals(2, triple.getThird());
    }

    @Test
    void parseJsonSensitivityPreContingencyStatus() throws Exception {
        // Arrange
        String json = """
            [{
              "loadFlowStatus": { "status": "CONVERGED", "statusText": "TestStatusText" },
              "numCC": 5,
              "numCS": 2
            }]
            """;
        JsonParser parser = factory.createParser(json);

        SensitivityAnalysisResult.SensitivityPreContingencyStatus.ParsingContext context = new SensitivityAnalysisResult.SensitivityPreContingencyStatus.ParsingContext();

        SensitivityAnalysisResult.SensitivityPreContingencyStatus.parseJson(parser, context);

        assertEquals(1, context.preContingencyloadflowStatuses.size());
        SensitivityAnalysisResult.SensitivityPreContingencyStatus sensitivityPreContingencyStatus = context.preContingencyloadflowStatuses.getFirst();

        assertEquals(LoadFlowResult.ComponentResult.Status.CONVERGED, sensitivityPreContingencyStatus.getLoadFlowStatus().status());
        assertEquals("TestStatusText", sensitivityPreContingencyStatus.getLoadFlowStatus().statusText());
        assertEquals(5, sensitivityPreContingencyStatus.getNumCC());
        assertEquals(2, sensitivityPreContingencyStatus.getNumCS());

    }
}
