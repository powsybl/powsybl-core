/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.json.JsonLoadFlowParameters;
import com.powsybl.sensitivity.SensitivityAnalysisParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Set;

/**
 * Json serializer for sensitivity analysis parameters
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class SensitivityAnalysisParametersSerializer extends StdSerializer<SensitivityAnalysisParameters> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensitivityAnalysisParametersSerializer.class);

    SensitivityAnalysisParametersSerializer() {
        super(SensitivityAnalysisParameters.class);
    }

    @Override
    public void serialize(SensitivityAnalysisParameters parameters, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringProperty("version", SensitivityAnalysisParameters.VERSION);

        jsonGenerator.writeName("load-flow-parameters");
        JsonLoadFlowParameters.serialize(parameters.getLoadFlowParameters(), jsonGenerator, serializationContext);

        jsonGenerator.writeNumberProperty("flow-flow-sensitivity-value-threshold", parameters.getFlowFlowSensitivityValueThreshold());
        jsonGenerator.writeNumberProperty("voltage-voltage-sensitivity-value-threshold", parameters.getVoltageVoltageSensitivityValueThreshold());
        jsonGenerator.writeNumberProperty("flow-voltage-sensitivity-value-threshold", parameters.getFlowVoltageSensitivityValueThreshold());
        jsonGenerator.writeNumberProperty("angle-flow-sensitivity-value-threshold", parameters.getAngleFlowSensitivityValueThreshold());
        jsonGenerator.writeStringProperty("operator-strategies-calculation-mode", parameters.getOperatorStrategiesCalculationMode().name());
        JsonUtil.writeOptionalStringProperty(jsonGenerator, "debug-dir", parameters.getDebugDir());

        Set<String> notSerialized = JsonUtil.writeExtensions(parameters, jsonGenerator, serializationContext, JsonSensitivityAnalysisParameters.getExtensionSerializers()::get);
        if (!notSerialized.isEmpty()) {
            LOGGER.warn("No JSON serializer found for sensitivity analysis parameters extensions {}, they are not written", notSerialized);
        }

        jsonGenerator.writeEndObject();
    }
}
