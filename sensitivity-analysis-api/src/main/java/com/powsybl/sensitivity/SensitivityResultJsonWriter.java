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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SensitivityResultJsonWriter implements SensitivityResultWriter, AutoCloseable {

    private final JsonGenerator jsonGenerator;

    private final List<Contingency> contingencies;

    private final List<OperatorStrategy> operatorStrategies;

    private final List<SensitivityAnalysisResult.SensitivityStateStatus> contingencyStatusBuffer;

    public SensitivityResultJsonWriter(JsonGenerator jsonGenerator, List<Contingency> contingencies,
                                       List<OperatorStrategy> operatorStrategies) {
        this.jsonGenerator = Objects.requireNonNull(jsonGenerator);
        this.contingencies = Objects.requireNonNull(contingencies);
        this.operatorStrategies = Objects.requireNonNull(operatorStrategies);
        this.contingencyStatusBuffer = new ArrayList<>(Collections.nCopies(contingencies.size(), null));
        try {
            jsonGenerator.writeStartArray();
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
        contingencyStatusBuffer.set(contingencyIndex, new SensitivityAnalysisResult.SensitivityStateStatus(state, status));
    }

    @Override
    public void close() {
        try {
            jsonGenerator.writeEndArray();
            //Write buffered contingency status at the end
            jsonGenerator.writeStartArray();
            for (SensitivityAnalysisResult.SensitivityStateStatus status : contingencyStatusBuffer) {
                SensitivityAnalysisResult.SensitivityStateStatus.writeJson(jsonGenerator, status);
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
