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

        if (!version.equals("1.0")) {
            //Only 1.0 version is supported for now
            throw new AssertionError("Version different than 1.0 not supported.");
        }
        return new SensitivityAnalysisResult(factors, contingencyStatus, sensitivityValues);
    }
}
