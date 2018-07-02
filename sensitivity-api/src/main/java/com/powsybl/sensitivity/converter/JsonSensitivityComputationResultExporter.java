/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.converter;

import com.google.auto.service.AutoService;
import com.powsybl.sensitivity.SensitivityComputationResults;
import com.powsybl.sensitivity.json.SensitivityComputationResultJsonSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 * A SensitivityComputationResultExporter implementation which export the result in JSON
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@AutoService(SensitivityComputationResultExporter.class)
public class JsonSensitivityComputationResultExporter implements SensitivityComputationResultExporter {

    @Override
    public String getFormat() {
        return "JSON";
    }

    @Override
    public String getComment() {
        return "Export a security analysis result in JSON format";
    }

    @Override
    public void export(SensitivityComputationResults result, Writer writer) {
        try {
            SensitivityComputationResultJsonSerializer.write(result, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
