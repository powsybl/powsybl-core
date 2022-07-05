package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.sensitivity.SensitivityAnalysisResult;

public class SensitivityContingencyStatusJsonSerializer extends StdSerializer<SensitivityAnalysisResult.SensitivityContingencyStatus> {

    public SensitivityContingencyStatusJsonSerializer() {
        super(SensitivityAnalysisResult.SensitivityContingencyStatus.class);
    }

    @Override
    public void serialize(SensitivityAnalysisResult.SensitivityContingencyStatus value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        SensitivityAnalysisResult.SensitivityContingencyStatus.writeJson(jsonGenerator, value);
    }
}
