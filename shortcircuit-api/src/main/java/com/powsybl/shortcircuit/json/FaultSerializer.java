/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.BranchFault;
import com.powsybl.shortcircuit.Fault;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class FaultSerializer extends StdSerializer<Fault> {

    public FaultSerializer() {
        super(Fault.class);
    }

    @Override
    public void serialize(Fault fault, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringProperty("type", fault.getType().name());
        jsonGenerator.writeStringProperty("id", fault.getId());
        jsonGenerator.writeStringProperty("elementId", fault.getElementId());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "r", fault.getRToGround());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "x", fault.getXToGround());
        jsonGenerator.writeStringProperty("connection", fault.getConnectionType().name());
        jsonGenerator.writeStringProperty("faultType", fault.getFaultType().name());
        if (fault.getType() == Fault.Type.BRANCH) {
            JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "proportionalLocation", ((BranchFault) fault).getProportionalLocation());
        }

        jsonGenerator.writeEndObject();
    }
}
