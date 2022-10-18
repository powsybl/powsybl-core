/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json.action;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.security.action.PhaseTapChangerTapPositionAction;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class PhaseTapChangerTapPositionActionDeserializer extends StdDeserializer<PhaseTapChangerTapPositionAction> {

    public PhaseTapChangerTapPositionActionDeserializer() {
        super(PhaseTapChangerTapPositionAction.class);
    }

    private static class ParsingContext {
        String id;
        String transformerId;
        int value;
        Boolean relativeValue;
        ThreeWindingsTransformer.Side side;
    }

    @Override
    public PhaseTapChangerTapPositionAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!PhaseTapChangerTapPositionAction.NAME.equals(jsonParser.nextTextValue())) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + PhaseTapChangerTapPositionAction.NAME);
                    }
                    return true;
                case "id":
                    context.id = jsonParser.nextTextValue();
                    return true;
                case "transformerId":
                    context.transformerId = jsonParser.nextTextValue();
                    return true;
                case "value":
                    jsonParser.nextToken();
                    context.value = jsonParser.getValueAsInt();
                    return true;
                case "relativeValue":
                    jsonParser.nextToken();
                    context.relativeValue = jsonParser.getValueAsBoolean();
                    return true;
                case "side":
                    context.side = ThreeWindingsTransformer.Side.valueOf(jsonParser.nextTextValue());
                    return true;
                default:
                    return false;
            }
        });
        if (context.relativeValue == null) {
            throw JsonMappingException.from(jsonParser, "for phase tap changer tap position action relative value field can't be null");
        }
        if (context.value == 0) {
            throw JsonMappingException.from(jsonParser, "for phase tap changer tap position action value field can't equal zero");
        }
        if (context.side != null) {
            return new PhaseTapChangerTapPositionAction(context.id, context.transformerId, context.relativeValue, context.value, context.side);
        } else {
            return new PhaseTapChangerTapPositionAction(context.id, context.transformerId, context.relativeValue, context.value);
        }
    }
}
