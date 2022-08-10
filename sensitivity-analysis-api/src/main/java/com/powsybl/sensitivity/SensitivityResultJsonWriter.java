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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SensitivityResultJsonWriter implements SensitivityResultWriter, AutoCloseable {

    private final JsonGenerator jsonGenerator;

    private List<SensitivityAnalysisResult.SensitivityContingencyStatus> contingencyStatusBuffer;

    public SensitivityResultJsonWriter(JsonGenerator jsonGenerator) {
        this.jsonGenerator = Objects.requireNonNull(jsonGenerator);
        this.contingencyStatusBuffer = new ArrayList<>();
        try {
            jsonGenerator.writeStartArray();
            jsonGenerator.writeStartArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeSensitivityValue(int factorIndex, int contingencyIndex, double value, double functionReference) {
        SensitivityValue.writeJson(jsonGenerator, factorIndex, contingencyIndex, value, functionReference);
    }

    @Override
    public void writeContingencyStatus(SensitivityAnalysisResult.SensitivityContingencyStatus status) {
        contingencyStatusBuffer.add(status);
    }

    @Override
    public void close() {
        try {
            jsonGenerator.writeEndArray();
            //Write buffered contingency status at the end
            jsonGenerator.writeStartArray();
            for (SensitivityAnalysisResult.SensitivityContingencyStatus status : contingencyStatusBuffer) {
                SensitivityAnalysisResult.SensitivityContingencyStatus.writeJson(jsonGenerator, status);
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
