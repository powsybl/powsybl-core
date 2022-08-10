/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.sensitivity.SensitivityAnalysisResult;

import java.io.IOException;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class SensitivityAnalysisResultSerializer extends StdSerializer<SensitivityAnalysisResult> {

    private static final String VERSION = "1.0";

    public SensitivityAnalysisResultSerializer() {
        super(SensitivityAnalysisResult.class);
    }

    @Override
    public void serialize(SensitivityAnalysisResult result, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", VERSION);
        jsonGenerator.writeObjectField("sensitivityFactors", result.getFactors());
        jsonGenerator.writeObjectField("sensitivityValues", result.getValues());
        jsonGenerator.writeObjectField("contingencyStatus", result.getContingencyStatuses());
        jsonGenerator.writeEndObject();
    }
}
