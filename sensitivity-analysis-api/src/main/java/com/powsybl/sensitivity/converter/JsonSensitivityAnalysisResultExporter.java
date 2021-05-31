/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.converter;

import com.google.auto.service.AutoService;
import com.powsybl.sensitivity.SensitivityAnalysisResult;
import com.powsybl.sensitivity.json.SensitivityAnalysisResultJsonSerializer;

import java.io.Writer;

/**
 * A {@link SensitivityAnalysisResultExporter} implementation which export the result in JSON
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@AutoService(SensitivityAnalysisResultExporter.class)
public class JsonSensitivityAnalysisResultExporter implements SensitivityAnalysisResultExporter {

    @Override
    public String getFormat() {
        return "JSON";
    }

    @Override
    public String getComment() {
        return "Export a security analysis result in JSON format";
    }

    @Override
    public void export(SensitivityAnalysisResult result, Writer writer) {
        SensitivityAnalysisResultJsonSerializer.write(result, writer);
    }
}
