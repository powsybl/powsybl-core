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
import com.powsybl.shortcircuit.ThreePhaseValue;

import java.io.IOException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ThreePhaseValueSerialiser extends StdSerializer<ThreePhaseValue> {
    public ThreePhaseValueSerialiser() {
        super(ThreePhaseValue.class);
    }

    @Override
    public void serialize(ThreePhaseValue value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        // Results on three phases.
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "magnitude1", value.getMagnitude1());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "magnitude2", value.getMagnitude2());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "magnitude3", value.getMagnitude3());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "phase1", value.getPhase1());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "phase2", value.getPhase2());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "phase3", value.getPhase3());
        // Fortescue results.
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "directMagnitude", value.getDirectMagnitude());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "zeroMagnitude", value.getZeroMagnitude());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "inverseMagnitude", value.getInverseMagnitude());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "directPhase", value.getDirectPhase());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "zeroPhase", value.getZeroPhase());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "inversePhase", value.getInversePhase());

        jsonGenerator.writeEndObject();
    }
}
