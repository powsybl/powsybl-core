/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.powsybl.contingency.Contingency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SensitivityResultModelWriter implements SensitivityResultWriter {

    private final List<Contingency> contingencies;

    private final List<SensitivityValue> values = new ArrayList<>();

    private final List<SensitivityAnalysisResult.SensitivityContingencyStatus> contingencyStatuses;

    public SensitivityResultModelWriter(List<Contingency> contingencies) {
        this.contingencies = Objects.requireNonNull(contingencies);
        contingencyStatuses = new ArrayList<>(Collections.nCopies(contingencies.size(), null));
    }

    public List<SensitivityValue> getValues() {
        return values;
    }

    public List<SensitivityAnalysisResult.SensitivityContingencyStatus> getContingencyStatuses() {
        return contingencyStatuses;
    }

    @Override
    public void writeSensitivityValue(int factorIndex, int contingencyIndex, double value, double functionReference) {
        values.add(new SensitivityValue(factorIndex, contingencyIndex, value, functionReference));
    }

    @Override
    public void writeContingencyStatus(int contingencyIndex, SensitivityAnalysisResult.Status status) {
        contingencyStatuses.set(contingencyIndex, new SensitivityAnalysisResult.SensitivityContingencyStatus(contingencies.get(contingencyIndex).getId(), status));
    }
}
