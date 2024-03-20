/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.action.PhaseTapChangerRegulationAction;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class PhaseTapChangerRegulationActionDeserializer extends AbstractTapChangerRegulationActionDeserializer<PhaseTapChangerRegulationAction> {

    public PhaseTapChangerRegulationActionDeserializer() {
        super(PhaseTapChangerRegulationAction.class);
    }

    private static class ParsingContext {
        String regulationMode;
        Double regulationValue = null;
    }

    @Override
    public PhaseTapChangerRegulationAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
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
                    if (!PhaseTapChangerRegulationAction.NAME.equals(type)) {
                        throw JsonMappingException.from(jsonParser, "Expected type :" + PhaseTapChangerRegulationAction.NAME + " got : " + type);
                    }
                    return true;
                case "regulationMode":
                    context.regulationMode = jsonParser.nextTextValue();
                    return true;
                case "regulationValue":
                    jsonParser.nextToken();
                    context.regulationValue = jsonParser.getValueAsDouble();
                    return true;
                default:
                    return false;
            }
        });
        PhaseTapChanger.RegulationMode mode = null;
        if (commonParsingContext.regulating) {
            mode = PhaseTapChanger.RegulationMode.valueOf(context.regulationMode);
        }
        return new PhaseTapChangerRegulationAction(commonParsingContext.id, commonParsingContext.transformerId,
                commonParsingContext.side, commonParsingContext.regulating, mode, context.regulationValue);
    }
}
