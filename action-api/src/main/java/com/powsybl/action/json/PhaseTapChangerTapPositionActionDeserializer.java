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
import com.powsybl.action.PhaseTapChangerTapPositionAction;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class PhaseTapChangerTapPositionActionDeserializer extends AbstractTapChangerTapPositionActionDeserializer<PhaseTapChangerTapPositionAction> {

    public PhaseTapChangerTapPositionActionDeserializer() {
        super(PhaseTapChangerTapPositionAction.class);
    }

    @Override
    public PhaseTapChangerTapPositionAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext commonParsingContext = new ParsingContext();
        String version = (String) deserializationContext.getAttribute(ActionListDeserializer.VERSION);
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            boolean found = deserializeCommonAttributes(jsonParser, commonParsingContext, name, version);
            if (found) {
                return true;
            }
            if (name.equals("type")) {
                String type = jsonParser.nextTextValue();
                if (!PhaseTapChangerTapPositionAction.NAME.equals(type)) {
                    throw JsonMappingException.from(jsonParser, "Expected type :" + PhaseTapChangerTapPositionAction.NAME + " got : " + type);
                }
                return true;
            }
            return false;
        });
        checkFields(commonParsingContext, jsonParser);
        return new PhaseTapChangerTapPositionAction(commonParsingContext.id, commonParsingContext.transformerId,
                commonParsingContext.relativeValue, commonParsingContext.tapPosition, commonParsingContext.side);
    }
}
