/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.results.BusResult;

import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class BusResultSerializer extends StdSerializer<BusResult> {

    public BusResultSerializer() {
        super(BusResult.class);
    }

    @Override
    public void serialize(BusResult busResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("voltageLevelId", busResult.getVoltageLevelId());
        jsonGenerator.writeStringField("busId", busResult.getBusId());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "v", busResult.getV());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "angle", busResult.getAngle());
        JsonUtil.writeExtensions(busResult, jsonGenerator, serializerProvider);
        jsonGenerator.writeEndObject();
    }
}
