/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sensitivity.SensitivityFactor;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Objects;

/**
 * JSON Serialization utility class for sensitivity factors input
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public final class SensitivityFactorsJsonSerializer {

    /**
     * Read sensitivity factors input in JSON format from reader
     * @param reader The reader to read from
     * @return The sensitivity factors input created
     */
    public static List<SensitivityFactor> read(Reader reader) {
        Objects.requireNonNull(reader);
        return JsonUtil.parseJson(reader, SensitivityFactor::parseMultipleJson);
    }

    /**
     * Write sensitivity factors input in JSON format to writer
     * @param sensitivityFactors The sensitivity factors input to export
     * @param writer The writer to write to
     */
    public static void write(List<SensitivityFactor> sensitivityFactors, Writer writer) {
        Objects.requireNonNull(sensitivityFactors);
        Objects.requireNonNull(writer);
        SensitivityFactor.writeJson(writer, sensitivityFactors);
    }

    private SensitivityFactorsJsonSerializer() {
    }
}
