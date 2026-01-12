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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SensitivityResultModelWriter implements SensitivityResultWriter {

    private final List<Contingency> contingencies;

    private final List<OperatorStrategy> operatorStrategies;

    private final List<SensitivityValue> values = new ArrayList<>();

    private final List<SensitivityAnalysisResult.SensitivityStateStatus> stateStatuses;

    public SensitivityResultModelWriter(List<Contingency> contingencies, List<OperatorStrategy> operatorStrategies) {
        this.contingencies = Objects.requireNonNull(contingencies);
        this.operatorStrategies = Objects.requireNonNull(operatorStrategies);
        stateStatuses = new ArrayList<>(Collections.nCopies(contingencies.size(), null));
    }

    public List<SensitivityValue> getValues() {
        return values;
    }

    public List<SensitivityAnalysisResult.SensitivityStateStatus> getStateStatuses() {
        return stateStatuses;
    }

    @Override
    public void writeSensitivityValue(int factorIndex, int contingencyIndex, int operatorStrategyIndex, double value, double functionReference) {
        values.add(new SensitivityValue(factorIndex, contingencyIndex, operatorStrategyIndex, value, functionReference));
    }

    @Override
    public void writeStateStatus(int contingencyIndex, int operatorStrategyIndex, SensitivityAnalysisResult.Status status) {
        SensitivityState state = new SensitivityState(contingencyIndex != -1 ? contingencies.get(contingencyIndex).getId() : null,
                                                      operatorStrategyIndex != -1 ? operatorStrategies.get(operatorStrategyIndex).getId() : null);
        stateStatuses.set(contingencyIndex, new SensitivityAnalysisResult.SensitivityStateStatus(state, status));
    }
}
