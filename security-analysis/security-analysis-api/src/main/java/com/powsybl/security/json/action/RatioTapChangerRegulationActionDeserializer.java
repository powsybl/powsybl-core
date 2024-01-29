/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.action;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.action.*;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class RatioTapChangerRegulationActionDeserializer extends AbstractTapChangerRegulationActionDeserializer<RatioTapChangerRegulationAction> {

    public RatioTapChangerRegulationActionDeserializer() {
        super(RatioTapChangerRegulationAction.class);
    }

    private static class ParsingContext {
        Double targetV = null;
    }

    @Override
    public RatioTapChangerRegulationAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        AbstractTapChangerRegulationActionDeserializer.ParsingContext commonParsingContext = new AbstractTapChangerRegulationActionDeserializer.ParsingContext();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            boolean found = deserializeCommonAttributes(jsonParser, commonParsingContext, name);
            if (found) {
                return true;
            }
            switch (name) {
                case "type":
                    String type = jsonParser.nextTextValue();
                    if (!RatioTapChangerRegulationAction.NAME.equals(type)) {
                        throw JsonMappingException.from(jsonParser, "Expected type :" + RatioTapChangerRegulationAction.NAME + " got : " + type);
                    }
                    return true;
                case "targetV":
                    jsonParser.nextToken();
                    context.targetV = jsonParser.getValueAsDouble();
                    return true;
                default:
                    return false;
            }
        });
        return new RatioTapChangerRegulationAction(commonParsingContext.id, commonParsingContext.transformerId,
                commonParsingContext.side, commonParsingContext.regulating, context.targetV);
    }
}
