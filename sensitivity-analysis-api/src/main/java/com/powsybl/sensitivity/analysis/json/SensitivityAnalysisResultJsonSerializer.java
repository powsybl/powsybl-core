/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.analysis.json;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sensitivity.analysis.SensitivityAnalysisResult;

import java.io.IOException;
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
     * @throws IOException
     */
    public static SensitivityAnalysisResult read(Reader reader) throws IOException {
        Objects.requireNonNull(reader);

        ObjectReader objectReader = JsonUtil.createObjectMapper().readerFor(SensitivityAnalysisResult.class);
        return objectReader.readValue(reader);
    }

    /**
     * Write sensitivity analysis results in JSON format to writer
     * @param result The sensitivity analysis results to export
     * @param writer The writer to write to
     * @throws IOException
     */
    public static void write(SensitivityAnalysisResult result, Writer writer) throws IOException {
        Objects.requireNonNull(result);
        Objects.requireNonNull(writer);

        ObjectWriter objectWriter = JsonUtil.createObjectMapper().writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(writer, result);
    }

    private SensitivityAnalysisResultJsonSerializer() {
    }
}
