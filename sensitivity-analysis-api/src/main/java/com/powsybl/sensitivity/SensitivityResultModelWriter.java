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

import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public class SensitivityResultModelWriter implements SensitivityResultWriter {

    private final List<Contingency> contingencies;

    private final List<OperatorStrategy> operatorStrategies;

    private final List<SensitivityValue> values = new ArrayList<>();

    private final Map<SensitivityState, SensitivityAnalysisResult.SensitivityStateStatus> stateStatuses = new LinkedHashMap<>();

    private boolean computationComplete;

    public SensitivityResultModelWriter(List<Contingency> contingencies, List<OperatorStrategy> operatorStrategies) {
        this.contingencies = Objects.requireNonNull(contingencies);
        this.operatorStrategies = Objects.requireNonNull(operatorStrategies);
    }

    public List<SensitivityValue> getValues() {
        return values;
    }

    public List<SensitivityAnalysisResult.SensitivityStateStatus> getStateStatuses() {
        return new ArrayList<>(stateStatuses.values());
    }

    public boolean isComputationComplete() {
        return computationComplete;
    }

    @Override
    public void writeSensitivityValue(int factorIndex, int contingencyIndex, int operatorStrategyIndex, double value, double functionReference) {
        values.add(new SensitivityValue(factorIndex, contingencyIndex, operatorStrategyIndex, value, functionReference));
    }

    @Override
    public void writeStateStatus(int contingencyIndex, int operatorStrategyIndex, SensitivityAnalysisResult.Status status,
                                 SensitivityAnalysisResult.LoadFlowStatus loadFlowStatus, int numCC, int numCS) {
        SensitivityState state = new SensitivityState(
                contingencyIndex != -1 ? contingencies.get(contingencyIndex).getId() : null,
                operatorStrategyIndex != -1 ? operatorStrategies.get(operatorStrategyIndex).getId() : null);
        SensitivityAnalysisResult.SensitivityStateStatus stateStatus = stateStatuses.computeIfAbsent(
                state, k -> new SensitivityAnalysisResult.SensitivityStateStatus(k, status));
        if (loadFlowStatus != null) {
            stateStatus.addComponentLoadFlowStatus(loadFlowStatus, numCC, numCS);
        }
    }

    @Override
    public void computationComplete() {
        computationComplete = true;
    }
}
