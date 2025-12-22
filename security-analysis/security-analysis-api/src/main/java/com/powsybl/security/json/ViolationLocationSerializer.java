/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.BusBreakerViolationLocation;
import com.powsybl.security.NodeBreakerViolationLocation;
import com.powsybl.security.ViolationLocation;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class ViolationLocationSerializer extends StdSerializer<ViolationLocation> {

    public ViolationLocationSerializer() {
        super(ViolationLocation.class);
    }

    @Override
    public void serialize(ViolationLocation violationLocation, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        JsonUtil.writeOptionalEnumProperty(jsonGenerator, "type", violationLocation.getType());
        if (ViolationLocation.Type.NODE_BREAKER == violationLocation.getType()) {
            NodeBreakerViolationLocation location = (NodeBreakerViolationLocation) violationLocation;
            jsonGenerator.writeStringProperty("voltageLevelId", location.getVoltageLevelId());
            serializationContext.defaultSerializeProperty("nodes", location.getNodes(), jsonGenerator);
        } else {
            BusBreakerViolationLocation location = (BusBreakerViolationLocation) violationLocation;
            serializationContext.defaultSerializeProperty("busIds", location.getBusIds(), jsonGenerator);
        }
        jsonGenerator.writeEndObject();
    }
}
