/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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
import com.powsybl.security.ViolationLocation;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class ViolationLocationSerializer extends StdSerializer<ViolationLocation> {

    public ViolationLocationSerializer() {
        super(ViolationLocation.class);
    }

    @Override
    public void serialize(ViolationLocation violationLocation, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        JsonUtil.writeOptionalEnumField(jsonGenerator, "type", violationLocation.getType());
        jsonGenerator.writeStringField("voltageLevelId", violationLocation.getVoltageLevelId());
        Optional<String> busId = violationLocation.getBusId();
        if (busId.isPresent()) {
            jsonGenerator.writeStringField("busId", busId.get());
        }
        if (!violationLocation.getBusBarIds().isEmpty()) {
            serializerProvider.defaultSerializeField("busbarIds", violationLocation.getBusBarIds(), jsonGenerator);
        }
        jsonGenerator.writeEndObject();
    }
}
