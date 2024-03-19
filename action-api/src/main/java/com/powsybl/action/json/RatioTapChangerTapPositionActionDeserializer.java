/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.action.RatioTapChangerTapPositionAction;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class RatioTapChangerTapPositionActionDeserializer extends AbstractTapChangerTapPositionActionDeserializer<RatioTapChangerTapPositionAction> {

    public RatioTapChangerTapPositionActionDeserializer() {
        super(RatioTapChangerTapPositionAction.class);
    }

    @Override
    public RatioTapChangerTapPositionAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        AbstractTapChangerTapPositionActionDeserializer.ParsingContext commonParsingContext = new AbstractTapChangerTapPositionActionDeserializer.ParsingContext();
        String version = (String) deserializationContext.getAttribute(ActionListDeserializer.VERSION);
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            boolean found = deserializeCommonAttributes(jsonParser, commonParsingContext, name, version);
            if (found) {
                return true;
            }
            if (name.equals("type")) {
                String type = jsonParser.nextTextValue();
                if (!RatioTapChangerTapPositionAction.NAME.equals(type)) {
                    throw JsonMappingException.from(jsonParser, "Expected type :" + RatioTapChangerTapPositionAction.NAME + " got : " + type);
                }
                return true;
            }
            return false;
        });
        checkFields(commonParsingContext, jsonParser);
        return new RatioTapChangerTapPositionAction(commonParsingContext.id, commonParsingContext.transformerId,
                commonParsingContext.relativeValue, commonParsingContext.tapPosition, commonParsingContext.side);
    }
}
