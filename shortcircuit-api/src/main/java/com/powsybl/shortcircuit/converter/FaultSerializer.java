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
import com.powsybl.shortcircuit.BranchFault;
import com.powsybl.shortcircuit.BusFault;
import com.powsybl.shortcircuit.Fault;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FaultSerializer extends StdSerializer<Fault> {

    public FaultSerializer() {
        super(Fault.class);
    }

    @Override
    public void serialize(Fault fault, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("version", Fault.VERSION);
        jsonGenerator.writeStringField("dataType", getDataType(fault));
        jsonGenerator.writeStringField("id", fault.getId());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "r", fault.getR());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "x", fault.getX());
        jsonGenerator.writeStringField("connection", fault.getConnectionType().name());
        jsonGenerator.writeStringField("faultType", fault.getFaultType().name());
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "withLimitViolation", fault.withLimitViolations(), false);
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "withDetailedResults", fault.withVoltageMap(), false);
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "proportionalLocation", fault.getProportionalLocation());

        jsonGenerator.writeEndObject();
    }

    String getDataType(Fault fault) {
        Objects.requireNonNull(fault);
        String dataType;
        if (fault instanceof BusFault) {
            dataType = BusFault.class.getSimpleName();
        } else if (fault instanceof BranchFault) {
            dataType = BranchFault.class.getSimpleName();
        } else {
            throw new AssertionError("Unexpected datatype: " + fault.getClass().getSimpleName());
        }
        return dataType;
    }
}
