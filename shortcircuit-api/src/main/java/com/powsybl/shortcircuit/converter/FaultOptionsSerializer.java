/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.option.FaultOptions;

import java.io.IOException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FaultOptionsSerializer extends StdSerializer<FaultOptions> {

    public FaultOptionsSerializer() {
        super(FaultOptions.class);
    }

    @Override
    public void serialize(FaultOptions options, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("id", options.getId());
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "withLimitViolations", options.isWithLimitViolations(), false);
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "withVoltageMap", options.isWithVoltageMap(), false);
        jsonGenerator.writeEndObject();
    }
}
