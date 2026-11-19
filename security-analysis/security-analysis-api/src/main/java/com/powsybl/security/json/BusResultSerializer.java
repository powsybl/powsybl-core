/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.results.BusResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class BusResultSerializer extends StdSerializer<BusResult> {

    public BusResultSerializer() {
        super(BusResult.class);
    }

    @Override
    public void serialize(BusResult busResult, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("voltageLevelId", busResult.getVoltageLevelId());
        jsonGenerator.writeStringProperty("busId", busResult.getBusId());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "v", busResult.getV());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "angle", busResult.getAngle());
        JsonUtil.writeExtensions(busResult, jsonGenerator, serializationContext);
        jsonGenerator.writeEndObject();
    }
}
