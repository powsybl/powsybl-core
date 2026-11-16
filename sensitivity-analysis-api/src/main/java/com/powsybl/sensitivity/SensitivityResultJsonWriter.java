/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.strategy.OperatorStrategy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SensitivityResultJsonWriter implements SensitivityResultWriter, AutoCloseable {

    private final JsonGenerator jsonGenerator;

    private final List<Contingency> contingencies;

    private final List<OperatorStrategy> operatorStrategies;

    private final Map<SensitivityState, SensitivityAnalysisResult.Status> stateStatusBuffer = new LinkedHashMap<>();

    public SensitivityResultJsonWriter(JsonGenerator jsonGenerator, List<Contingency> contingencies,
                                       List<OperatorStrategy> operatorStrategies) {
        this.jsonGenerator = Objects.requireNonNull(jsonGenerator);
        this.contingencies = Objects.requireNonNull(contingencies);
        this.operatorStrategies = Objects.requireNonNull(operatorStrategies);
        try {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("sensitivityValues");
            jsonGenerator.writeStartArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeSensitivityValue(int factorIndex, int contingencyIndex, int operatorStrategyIndex, double value, double functionReference) {
        SensitivityValue.writeJson(jsonGenerator, factorIndex, contingencyIndex, operatorStrategyIndex, value, functionReference);
    }

    @Override
    public void writeStateStatus(int contingencyIndex, int operatorStrategyIndex, SensitivityAnalysisResult.Status status) {
        SensitivityState state = new SensitivityState(contingencyIndex != -1 ? contingencies.get(contingencyIndex).getId() : null,
                                                      operatorStrategyIndex != -1 ? operatorStrategies.get(operatorStrategyIndex).getId() : null);
        stateStatusBuffer.put(state, status);
    }

    @Override
    public void close() {
        try {
            jsonGenerator.writeEndArray();

            //Write buffered contingency status at the end
            jsonGenerator.writeFieldName("stateStatus");
            jsonGenerator.writeStartArray();
            for (var e : stateStatusBuffer.entrySet()) {
                SensitivityAnalysisResult.SensitivityStateStatus.writeJson(jsonGenerator, e.getKey(), e.getValue());
            }
            jsonGenerator.writeEndArray();

            jsonGenerator.writeFieldName("contingencyIds");
            jsonGenerator.writeStartArray();
            for (Contingency contingency : contingencies) {
                jsonGenerator.writeString(contingency.getId());
            }
            jsonGenerator.writeEndArray();

            jsonGenerator.writeFieldName("operatorStrategyIds");
            jsonGenerator.writeStartArray();
            for (OperatorStrategy operatorStrategy : operatorStrategies) {
                jsonGenerator.writeString(operatorStrategy.getId());
            }
            jsonGenerator.writeEndArray();

            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
