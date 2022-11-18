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
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.security.action.*;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class TapChangerRegulationActionDeserializer extends StdDeserializer<AbstractTapChangerRegulationAction> {

    public TapChangerRegulationActionDeserializer() {
        super(AbstractTapChangerRegulationAction.class);
    }

    private static class ParsingContext {
        String id;
        String type;
        String transformerId;
        boolean regulating;
        ThreeWindingsTransformer.Side side = null;
        String regulationMode;
    }

    @Override
    public AbstractTapChangerRegulationAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    String type = jsonParser.nextTextValue();
                    if (!PhaseTapChangerRegulationAction.NAME.equals(type) && !RatioTapChangerRegulationAction.NAME.equals(type)) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + PhaseTapChangerRegulationAction.NAME
                                + "or " + PhaseTapChangerRegulationAction.NAME);
                    }
                    context.type = type;
                    return true;
                case "id":
                    context.id = jsonParser.nextTextValue();
                    return true;
                case "transformerId":
                    context.transformerId = jsonParser.nextTextValue();
                    return true;
                case "side":
                    context.side = ThreeWindingsTransformer.Side.valueOf(jsonParser.nextTextValue());
                    return true;
                case "regulating":
                    jsonParser.nextToken();
                    context.regulating = jsonParser.getValueAsBoolean();
                    return true;
                case "regulationMode":
                    context.regulationMode = jsonParser.nextTextValue();
                    return true;
                default:
                    return false;
            }
        });
        if (context.type.equals(PhaseTapChangerRegulationAction.NAME)) {
            PhaseTapChanger.RegulationMode mode = null;
            if (context.regulating) {
                mode = PhaseTapChanger.RegulationMode.valueOf(context.regulationMode);
            }
            return new PhaseTapChangerRegulationAction(context.id, context.transformerId, context.side, context.regulating, mode);
        } else {
            return new RatioTapChangerRegulationAction(context.id, context.transformerId, context.side, context.regulating);
        }
    }
}
