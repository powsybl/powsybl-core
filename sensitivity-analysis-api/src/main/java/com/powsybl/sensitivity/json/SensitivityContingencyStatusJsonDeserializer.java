package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.sensitivity.SensitivityAnalysisResult;

public class SensitivityContingencyStatusJsonDeserializer extends StdDeserializer<SensitivityAnalysisResult.SensitivityContingencyStatus> {

    public SensitivityContingencyStatusJsonDeserializer() {
        super(SensitivityAnalysisResult.SensitivityContingencyStatus.class);
    }

    @Override
    public SensitivityAnalysisResult.SensitivityContingencyStatus deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        return SensitivityAnalysisResult.SensitivityContingencyStatus.parseJson(jsonParser);
    }
}
