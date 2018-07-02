/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sensitivity.SensitivityComputationResults;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;

public final class SensitivityComputationResultJsonSerializer {

    public static SensitivityComputationResults read(Reader reader) throws IOException {
        Objects.requireNonNull(reader);

        ObjectReader objectReader = JsonUtil.createObjectMapper().readerFor(SensitivityComputationResults.class);
        return objectReader.readValue(reader);
    }

    public static void write(SensitivityComputationResults result, Writer writer) throws IOException {
        Objects.requireNonNull(result);
        Objects.requireNonNull(writer);

        ObjectWriter objectWriter = JsonUtil.createObjectMapper().writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(writer, result);
    }

    private SensitivityComputationResultJsonSerializer() {
    }
}
