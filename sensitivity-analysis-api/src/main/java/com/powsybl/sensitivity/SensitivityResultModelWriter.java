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

import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public class SensitivityResultModelWriter implements SensitivityResultWriter {

    private final List<Contingency> contingencies;

    private final List<OperatorStrategy> operatorStrategies;

    private final List<SensitivityValue> values = new ArrayList<>();

    private final Map<SensitivityState, List<SensitivityStateStatus.ComponentStatus>> stateStatuses = new LinkedHashMap<>();

    public SensitivityResultModelWriter(List<Contingency> contingencies, List<OperatorStrategy> operatorStrategies) {
        this.contingencies = Objects.requireNonNull(contingencies);
        this.operatorStrategies = Objects.requireNonNull(operatorStrategies);
    }

    public List<SensitivityValue> getValues() {
        return values;
    }

    public List<SensitivityStateStatus> getStateStatuses() {
        return stateStatuses.entrySet().stream()
                .map(e -> new SensitivityStateStatus(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public void writeSensitivityValue(int factorIndex, int contingencyIndex, int operatorStrategyIndex, double value, double functionReference) {
        values.add(new SensitivityValue(factorIndex, contingencyIndex, operatorStrategyIndex, value, functionReference));
    }

    @Override
    public void writeStateStatus(int contingencyIndex, int operatorStrategyIndex,
                                 int connectedComponentNum, int synchronousComponentNum,
                                 LoadFlowResult.ComponentResult.Status status, String statusText) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(statusText);
        SensitivityState state = new SensitivityState(
                contingencyIndex != -1 ? contingencies.get(contingencyIndex).getId() : null,
                operatorStrategyIndex != -1 ? operatorStrategies.get(operatorStrategyIndex).getId() : null);
        stateStatuses.computeIfAbsent(state, s -> new ArrayList<>())
                .add(new SensitivityStateStatus.ComponentStatus(connectedComponentNum, synchronousComponentNum, status, statusText));
    }
}
