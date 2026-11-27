/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.strategy.OperatorStrategy;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.sensitivity.json.SensitivityStateStatusJsonSerializer;
import tools.jackson.core.JsonGenerator;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public class SensitivityResultJsonWriter implements SensitivityResultWriter, AutoCloseable {

    private final JsonGenerator jsonGenerator;

    private final List<Contingency> contingencies;

    private final List<OperatorStrategy> operatorStrategies;

    private final Map<SensitivityState, List<SensitivityStateStatus.ComponentStatus>> stateStatusBuffer = new LinkedHashMap<>();

    private boolean computationComplete = false;

    public SensitivityResultJsonWriter(JsonGenerator jsonGenerator, List<Contingency> contingencies,
                                       List<OperatorStrategy> operatorStrategies) {
        this.jsonGenerator = Objects.requireNonNull(jsonGenerator);
        this.contingencies = Objects.requireNonNull(contingencies);
        this.operatorStrategies = Objects.requireNonNull(operatorStrategies);
        jsonGenerator.writeStartObject();
        jsonGenerator.writeName("sensitivityValues");
        jsonGenerator.writeStartArray();
    }

    @Override
    public void writeSensitivityValue(int factorIndex, int contingencyIndex, int operatorStrategyIndex, double value, double functionReference) {
        SensitivityValue.writeJson(jsonGenerator, factorIndex, contingencyIndex, operatorStrategyIndex, value, functionReference);
    }

    @Override
    public void writeStateStatus(int contingencyIndex, int operatorStrategyIndex, int connectedComponentNum, int synchronousComponentNum,
                                 LoadFlowResult.ComponentResult.Status status, String statusText) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(statusText);
        SensitivityState state = new SensitivityState(
                contingencyIndex != -1 ? contingencies.get(contingencyIndex).getId() : null,
                operatorStrategyIndex != -1 ? operatorStrategies.get(operatorStrategyIndex).getId() : null);
        stateStatusBuffer.computeIfAbsent(state, s -> new ArrayList<>())
                .add(new SensitivityStateStatus.ComponentStatus(connectedComponentNum, synchronousComponentNum, status, statusText));
    }

    @Override
    public void computationComplete() {
        computationComplete = true;
    }

    @Override
    public boolean isComputationComplete() {
        return computationComplete;
    }

    @Override
    public void close() {
        jsonGenerator.writeEndArray();

        jsonGenerator.writeName("stateStatus");
        jsonGenerator.writeStartArray();
        for (var stateStatus : stateStatusBuffer.entrySet()) {
            SensitivityStateStatusJsonSerializer.writeJson(jsonGenerator, new SensitivityStateStatus(stateStatus.getKey(), stateStatus.getValue()));
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeName("contingencyIds");
        jsonGenerator.writeStartArray();
        for (Contingency contingency : contingencies) {
            jsonGenerator.writeString(contingency.getId());
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeName("operatorStrategyIds");
        jsonGenerator.writeStartArray();
        for (OperatorStrategy operatorStrategy : operatorStrategies) {
            jsonGenerator.writeString(operatorStrategy.getId());
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeBooleanProperty("computationComplete", computationComplete);

        jsonGenerator.writeEndObject();
    }
}
