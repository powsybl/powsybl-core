/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SensitivityValueJsonWriter implements SensitivityValueWriter, AutoCloseable {

    private final JsonGenerator jsonGenerator;
    private final JsonGenerator jsonStatusGenerator;

    public SensitivityValueJsonWriter(JsonGenerator jsonGenerator, JsonGenerator jsonStatusGenerator) {
        this.jsonGenerator = Objects.requireNonNull(jsonGenerator);
        this.jsonStatusGenerator = Objects.requireNonNull(jsonStatusGenerator);
        try {
            jsonGenerator.writeStartArray();
            jsonStatusGenerator.writeStartArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(int factorIndex, int contingencyIndex, double value, double functionReference) {
        SensitivityValue.writeJson(jsonGenerator, factorIndex, contingencyIndex, value, functionReference);
    }

    @Override
    public void writeContingencyStatus(SensitivityAnalysisResult.SensitivityContingencyStatus status) {
        SensitivityAnalysisResult.SensitivityContingencyStatus.writeJson(jsonStatusGenerator, status);
    }

    @Override
    public void close() {
        try {
            jsonGenerator.writeEndArray();
            jsonStatusGenerator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
