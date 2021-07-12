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

    public List<SensitivityValue> getValues() {
        return values;
    }

    @Override
    public void write(SensitivityFactor factorContext, String contingencyId, int contingencyIndex, double value, double functionReference) {
        values.add(new SensitivityValue(factorContext, contingencyId, value, functionReference));
    }
}
