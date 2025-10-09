/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.auto.service.AutoService;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;
import com.powsybl.shortcircuit.json.ShortCircuitAnalysisJsonModule;

import java.io.IOException;
import java.io.Writer;

/**
 * Exports a short circuit analysis result in JSON format.
 *
 * @author Teofil-Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
@AutoService(ShortCircuitAnalysisResultExporter.class)
public class JsonShortCircuitAnalysisResultExporter implements ShortCircuitAnalysisResultExporter {

    @Override
    public String getFormat() {
        return "JSON";
    }

    @Override
    public String getComment() {
        return "Export a result in JSON format";
    }

    @Override
    public void export(ShortCircuitAnalysisResult result, Writer writer, Network network) throws IOException {
        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new ShortCircuitAnalysisJsonModule());

        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(writer, result);

    }
}
