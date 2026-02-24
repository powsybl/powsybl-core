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
import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SensitivityResultJsonWriter implements SensitivityResultWriter, AutoCloseable {

    private final JsonGenerator jsonGenerator;

    private final List<Contingency> contingencies;

    private final List<SensitivityAnalysisResult.SensitivityContingencyStatus> contingencyStatusBuffer;

    private final List<SensitivityAnalysisResult.SensitivityPreContingencyStatus> precontingencyStatusBuffer;

    private boolean computationComplete = false;

    public SensitivityResultJsonWriter(JsonGenerator jsonGenerator, List<Contingency> contingencies) {
        this.jsonGenerator = Objects.requireNonNull(jsonGenerator);
        this.contingencies = Objects.requireNonNull(contingencies);
        this.contingencyStatusBuffer = new ArrayList<>(Collections.nCopies(contingencies.size(), null));
        this.precontingencyStatusBuffer = new ArrayList<>();
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
    public void writeContingencyStatus(int contingencyIndex, SensitivityAnalysisResult.Status status, SensitivityAnalysisResult.LoadFlowStatus loadFlowStatus, int numCC, int numCs) {
        contingencyStatusBuffer.set(contingencyIndex, new SensitivityAnalysisResult.SensitivityContingencyStatus(contingencies.get(contingencyIndex).getId(), status));
        contingencyStatusBuffer.get(contingencyIndex).addComponentLoadFlowStatus(loadFlowStatus, numCC, numCs);
    }

    @Override
    public void writeSynchronousComponentStatus(int numCC, int numCS, SensitivityAnalysisResult.LoadFlowStatus loadFlowStatus) {
        SensitivityAnalysisResult.SensitivityPreContingencyStatus e = new SensitivityAnalysisResult.SensitivityPreContingencyStatus(loadFlowStatus, numCC, numCS);
        precontingencyStatusBuffer.add(e);

    }

    @Override
    public void computationComplete() {
        computationComplete = true;
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
            jsonGenerator.writeStartArray();
            for (SensitivityAnalysisResult.SensitivityPreContingencyStatus status : precontingencyStatusBuffer) {
                SensitivityAnalysisResult.SensitivityPreContingencyStatus.writeJson(jsonGenerator, status);
            }
            jsonGenerator.writeEndArray();
            // Write computation completion information
            jsonGenerator.writeStartArray();
            jsonGenerator.writeStartObject();
            jsonGenerator.writeBooleanField("completion", computationComplete);
            jsonGenerator.writeEndObject();
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
