/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity.json;

import com.powsybl.sensitivity.SensitivityAnalysisResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class SensitivityAnalysisResultSerializer extends StdSerializer<SensitivityAnalysisResult> {

    public SensitivityAnalysisResultSerializer() {
        super(SensitivityAnalysisResult.class);
    }

    @Override
    public void serialize(SensitivityAnalysisResult result, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("version", SensitivityAnalysisResult.VERSION);
        serializationContext.defaultSerializeProperty("sensitivityFactors", result.getFactors(), jsonGenerator);
        serializationContext.defaultSerializeProperty("sensitivityValues", result.getValues(), jsonGenerator);
        serializationContext.defaultSerializeProperty("stateStatus", result.getStateStatuses(), jsonGenerator);
        serializationContext.defaultSerializeProperty("contingencyIds", result.getContingencyIds(), jsonGenerator);
        serializationContext.defaultSerializeProperty("operatorStrategyIds", result.getOperatorStrategyIds(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
