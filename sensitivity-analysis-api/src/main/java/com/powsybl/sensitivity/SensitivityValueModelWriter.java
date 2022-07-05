/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SensitivityValueModelWriter implements SensitivityValueWriter {

    private final List<SensitivityValue> values = new ArrayList<>();

    private final List<SensitivityAnalysisResult.SensitivityContingencyStatus> contingencyStatuses = new ArrayList<>();

    public List<SensitivityValue> getValues() {
        return values;
    }

    public List<SensitivityAnalysisResult.SensitivityContingencyStatus> getContingencyStatuses() {
        return contingencyStatuses;
    }

    @Override
    public void write(int factorIndex, int contingencyIndex, double value, double functionReference) {
        values.add(new SensitivityValue(factorIndex, contingencyIndex, value, functionReference));
    }

    @Override
    public void writeContingencyStatus(SensitivityAnalysisResult.SensitivityContingencyStatus status) {
        contingencyStatuses.add(status);
    }
}
