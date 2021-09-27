/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonGenerator;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SensitivityValueJsonWriter implements SensitivityValueWriter {

    private final JsonGenerator jsonGenerator;

    public SensitivityValueJsonWriter(JsonGenerator jsonGenerator) {
        this.jsonGenerator = Objects.requireNonNull(jsonGenerator);
    }

    @Override
    public void write(String contingencyId, String variableId, String functionId, int factorIndex, int contingencyIndex, double value, double functionReference) {
        SensitivityValue.writeJson(jsonGenerator, contingencyId, variableId, functionId, value, functionReference);
    }
}
