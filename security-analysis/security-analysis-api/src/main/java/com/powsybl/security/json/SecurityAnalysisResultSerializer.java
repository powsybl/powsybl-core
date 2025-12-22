/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.action.json.ActionJsonModule;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.SecurityAnalysisResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class SecurityAnalysisResultSerializer extends StdSerializer<SecurityAnalysisResult> {

    public static final String VERSION = "1.8";

    public SecurityAnalysisResultSerializer() {
        super(SecurityAnalysisResult.class);
    }

    @Override
    public void serialize(SecurityAnalysisResult result, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("version", VERSION);
        if (result.getNetworkMetadata() != null) {
            serializationContext.defaultSerializeProperty("network", result.getNetworkMetadata(), jsonGenerator);
        }
        serializationContext.defaultSerializeProperty("preContingencyResult", result.getPreContingencyResult(), jsonGenerator);
        serializationContext.defaultSerializeProperty("postContingencyResults", result.getPostContingencyResults(), jsonGenerator);
        serializationContext.defaultSerializeProperty("operatorStrategyResults", result.getOperatorStrategyResults(), jsonGenerator);
        JsonUtil.writeExtensions(result, jsonGenerator, serializationContext);

        jsonGenerator.writeEndObject();
    }

    public static void write(SecurityAnalysisResult result, Writer writer) throws IOException {
        Objects.requireNonNull(result);
        Objects.requireNonNull(writer);

        JsonMapper jsonMapper = JsonUtil.createJsonMapperBuilder()
            .addModule(new SecurityAnalysisJsonModule())
            .addModule(new ActionJsonModule())
            .build();

        ObjectWriter objectWriter = jsonMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(writer, result);
    }
}
