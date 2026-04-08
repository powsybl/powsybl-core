/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.BranchFault;
import com.powsybl.shortcircuit.Fault;

import java.io.IOException;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class FaultSerializer extends StdSerializer<Fault> {

    public FaultSerializer() {
        super(Fault.class);
    }

    @Override
    public void serialize(Fault fault, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("type", fault.getType().name());
        jsonGenerator.writeStringField("id", fault.getId());
        jsonGenerator.writeStringField("elementId", fault.getElementId());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "r", fault.getRToGround());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "x", fault.getXToGround());
        jsonGenerator.writeStringField("connection", fault.getConnectionType().name());
        jsonGenerator.writeStringField("faultType", fault.getFaultType().name());
        if (fault.getType() == Fault.Type.BRANCH) {
            JsonUtil.writeOptionalDoubleField(jsonGenerator, "proportionalLocation", ((BranchFault) fault).getProportionalLocation());
        }

        jsonGenerator.writeEndObject();
    }
}
