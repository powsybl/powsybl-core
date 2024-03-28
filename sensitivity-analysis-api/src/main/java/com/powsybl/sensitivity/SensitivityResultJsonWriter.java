/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.contingency.Contingency;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SensitivityResultJsonWriter implements SensitivityResultWriter, AutoCloseable {

    private final JsonGenerator jsonGenerator;

    private final List<Contingency> contingencies;

    private final List<SensitivityAnalysisResult.SensitivityContingencyStatus> contingencyStatusBuffer;

    public SensitivityResultJsonWriter(JsonGenerator jsonGenerator, List<Contingency> contingencies) {
        this.jsonGenerator = Objects.requireNonNull(jsonGenerator);
        this.contingencies = Objects.requireNonNull(contingencies);
        this.contingencyStatusBuffer = new ArrayList<>(Collections.nCopies(contingencies.size(), null));
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
    public void writeContingencyStatus(int contingencyIndex, SensitivityAnalysisResult.Status status) {
        contingencyStatusBuffer.set(contingencyIndex, new SensitivityAnalysisResult.SensitivityContingencyStatus(contingencies.get(contingencyIndex).getId(), status));
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
