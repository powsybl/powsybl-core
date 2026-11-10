/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.sensitivity.SensitivityState;
import com.powsybl.sensitivity.SensitivityStateStatus;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static com.powsybl.sensitivity.json.SensitivityStateStatusJsonDeserializer.*;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class SensitivityStateStatusJsonSerializer extends StdSerializer<SensitivityStateStatus> {

    public SensitivityStateStatusJsonSerializer() {
        super(SensitivityStateStatus.class);
    }

    @Override
    public void serialize(SensitivityStateStatus value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        writeJson(jsonGenerator, value);
    }

    public static void writeJson(JsonGenerator jsonGenerator, SensitivityStateStatus stateStatus) {
        writeJson(jsonGenerator, stateStatus.getState(), stateStatus.getComponentsLoadFlowStatusList());
    }

    public static void writeJson(JsonGenerator jsonGenerator, SensitivityState state,
                                 List<SensitivityStateStatus.ComponentStatus> componentsLoadFlowStatusList) {
        try {
            jsonGenerator.writeStartObject();
            if (state.contingencyId() != null) {
                jsonGenerator.writeStringField("contingencyId", state.contingencyId());
            }
            if (state.operatorStrategyId() != null) {
                jsonGenerator.writeStringField("operatorStrategyId", state.operatorStrategyId());
            }
            if (componentsLoadFlowStatusList != null && !componentsLoadFlowStatusList.isEmpty()) {
                jsonGenerator.writeArrayFieldStart(COMPONENT_STATUSES);
                for (SensitivityStateStatus.ComponentStatus componentStatus : componentsLoadFlowStatusList) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField(LOAD_FLOW_STATUS, componentStatus.status().toString());
                    jsonGenerator.writeStringField(LOAD_FLOW_STATUS_TEXT, componentStatus.statusText());
                    jsonGenerator.writeNumberField(NUM_CC, componentStatus.connectedComponentNum());
                    jsonGenerator.writeNumberField(NUM_SC, componentStatus.synchronousComponentNum());
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
            }
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
