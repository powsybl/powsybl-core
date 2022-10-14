/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json.action;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.action.*;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class ActionSerializer extends StdSerializer<Action> {

    public ActionSerializer() {
        super(Action.class);
    }

    @Override
    public void serialize(Action action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", action.getType());
        switch (action.getType()) {
            case SwitchAction.NAME:
                jsonGenerator.writeStringField("id", action.getId());
                jsonGenerator.writeStringField("switchId", ((SwitchAction) action).getSwitchId());
                jsonGenerator.writeBooleanField("open", ((SwitchAction) action).isOpen());
                break;
            case LineConnectionAction.NAME:
                jsonGenerator.writeStringField("id", action.getId());
                jsonGenerator.writeStringField("lineId", ((LineConnectionAction) action).getLineId());
                jsonGenerator.writeBooleanField("openSide1", ((LineConnectionAction) action).isOpenSide1());
                jsonGenerator.writeBooleanField("openSide2", ((LineConnectionAction) action).isOpenSide2());
                break;
            case PhaseTapChangerTapPositionAction.NAME:
                var tapPositionAction = (PhaseTapChangerTapPositionAction) action;
                jsonGenerator.writeStringField("id", action.getId());
                jsonGenerator.writeStringField("transformerId", tapPositionAction.getTransformerId());
                jsonGenerator.writeNumberField("value", tapPositionAction.getValue());
                jsonGenerator.writeBooleanField("relativeValue", tapPositionAction.isRelativeValue());
                tapPositionAction.getSide().ifPresent(side -> {
                    try {
                        jsonGenerator.writeStringField("side", side.toString());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
                break;
            case MultipleActionsAction.NAME:
                jsonGenerator.writeStringField("id", action.getId());
                jsonGenerator.writeObjectField("actions", ((MultipleActionsAction) action).getActions());
                break;
            default:
                throw JsonMappingException.from(jsonGenerator, "Unknown action type: " + action.getType());
        }
        jsonGenerator.writeEndObject();
    }
}
