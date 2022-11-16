/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json.action;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.action.AbstractTapChangerRegulationAction;
import com.powsybl.security.action.PhaseTapChangerRegulationAction;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class TapChangerRegulationActionSerializer extends StdSerializer<AbstractTapChangerRegulationAction> {

    public TapChangerRegulationActionSerializer() {
        super(AbstractTapChangerRegulationAction.class);
    }

    @Override
    public void serialize(AbstractTapChangerRegulationAction action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", action.getType());
        jsonGenerator.writeStringField("id", action.getId());
        jsonGenerator.writeStringField("transformerId", action.getTransformerId());
        jsonGenerator.writeBooleanField("regulating", action.isRegulating());
        action.getSide().ifPresent(side -> {
            try {
                jsonGenerator.writeStringField("side", side.toString());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        if (action.getType().equals(PhaseTapChangerRegulationAction.NAME)) {
            jsonGenerator.writeStringField("regulationMode",
                    ((PhaseTapChangerRegulationAction) action).getRegulationMode().toString());
        }
        jsonGenerator.writeEndObject();
    }
}
