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
import com.powsybl.security.action.AbstractTapChangerTapPositionAction;
import com.powsybl.security.action.PhaseTapChangerTapPositionAction;
import com.powsybl.security.action.RatioTapChangerTapPositionAction;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class TapChangerTapPositionActionDeserializer extends StdDeserializer<AbstractTapChangerTapPositionAction> {

    public TapChangerTapPositionActionDeserializer() {
        super(PhaseTapChangerTapPositionAction.class);
    }

    private static class ParsingContext {
        String id;
        String type;
        String transformerId;
        int tapPosition;
        Boolean relativeValue;
        ThreeWindingsTransformer.Side side = null;
    }

    @Override
    public AbstractTapChangerTapPositionAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        String version = (String) deserializationContext.getAttribute("version");
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    String type = jsonParser.nextTextValue();
                    if (!PhaseTapChangerTapPositionAction.NAME.equals(type) && !RatioTapChangerTapPositionAction.NAME.equals(type)) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + PhaseTapChangerTapPositionAction.NAME
                                + "or " + RatioTapChangerTapPositionAction.NAME);
                    }
                    context.type = type;
                    return true;
                case "id":
                    context.id = jsonParser.nextTextValue();
                    return true;
                case "transformerId":
                    context.transformerId = jsonParser.nextTextValue();
                    return true;
                case "value":
                    JsonUtil.assertLessThanOrEqualToReferenceVersion("actions", "Tag: value", version, "1.0");
                    jsonParser.nextToken();
                    context.tapPosition = jsonParser.getValueAsInt();
                    return true;
                case "tapPosition":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion("actions", "Tag: tapPosition", version, "1.1");
                    jsonParser.nextToken();
                    context.tapPosition = jsonParser.getValueAsInt();
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
        if (context.tapPosition == 0) {
            throw JsonMappingException.from(jsonParser, "for phase tap changer tap position action tapPosition field can't equal zero");
        }
        if (context.type.equals(PhaseTapChangerTapPositionAction.NAME)) {
            return new PhaseTapChangerTapPositionAction(context.id, context.transformerId, context.relativeValue, context.tapPosition, context.side);
        } else {
            return new RatioTapChangerTapPositionAction(context.id, context.transformerId, context.relativeValue, context.tapPosition, context.side);
        }
    }
}
