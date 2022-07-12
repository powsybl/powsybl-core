package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.sensitivity.SensitivityAnalysisResult;
import com.powsybl.sensitivity.SensitivityFactor;
import com.powsybl.sensitivity.SensitivityValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SensitivityAnalysisResultDeserializer extends StdDeserializer<SensitivityAnalysisResult> {

    protected SensitivityAnalysisResultDeserializer() {
        super(SensitivityAnalysisResult.class);
    }

    @Override
    public SensitivityAnalysisResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String version = null;
        List<SensitivityValue> sensitivityValues = null;
        List<SensitivityAnalysisResult.SensitivityContingencyStatus> contingencyStatus = null;
        List<SensitivityFactor> factors = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken(); // skip
                    version = parser.getValueAsString();
                    break;

                case "sensitivityFactors":
                    parser.nextToken();
                    factors = parser.readValueAs(new TypeReference<ArrayList<SensitivityFactor>>() { });
                    break;

                case "sensitivityValues":
                    parser.nextToken();
                    sensitivityValues = parser.readValueAs(new TypeReference<ArrayList<SensitivityValue>>() { });
                    break;

                case "contingencyStatus":
                    parser.nextToken();
                    contingencyStatus = parser.readValueAs(new TypeReference<ArrayList<SensitivityAnalysisResult.SensitivityContingencyStatus>>() {
                    });
                    break;
                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        SensitivityAnalysisResult result = new SensitivityAnalysisResult(factors, contingencyStatus, sensitivityValues);
        return result;
    }
}
