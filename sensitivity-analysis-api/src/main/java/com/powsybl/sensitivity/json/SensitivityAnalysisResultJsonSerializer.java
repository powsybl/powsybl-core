/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sensitivity.SensitivityAnalysisResult;

import java.io.Reader;
import java.io.Writer;
import java.util.Objects;

/**
 * JSON Serialization utility class for sensitivity analysis results
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public final class SensitivityAnalysisResultJsonSerializer {

    /**
     * Read sensitivity analysis results in JSON format from reader
     * @param reader The reader to read from
     * @return The sensitivity analysis results created
     */
    public static SensitivityAnalysisResult read(Reader reader) {
        Objects.requireNonNull(reader);
        return JsonUtil.parseJson(reader, SensitivityAnalysisResult::parseJson);
    }

    /**
     * Write sensitivity analysis results in JSON format to writer
     * @param result The sensitivity analysis results to export
     * @param writer The writer to write to
     */
    public static void write(SensitivityAnalysisResult result, Writer writer) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(writer);
        SensitivityAnalysisResult.writeJson(writer, result);
    }

    private SensitivityAnalysisResultJsonSerializer() {
    }
}
