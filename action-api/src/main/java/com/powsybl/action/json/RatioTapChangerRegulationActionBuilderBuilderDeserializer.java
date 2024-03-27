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
import com.powsybl.action.RatioTapChangerRegulationActionBuilder;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.action.*;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class RatioTapChangerRegulationActionBuilderBuilderDeserializer extends AbstractTapChangerRegulationActionBuilderDeserializer<RatioTapChangerRegulationActionBuilder> {

    public RatioTapChangerRegulationActionBuilderBuilderDeserializer() {
        super(RatioTapChangerRegulationActionBuilder.class);
    }

    @Override
    public RatioTapChangerRegulationActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        RatioTapChangerRegulationActionBuilder builder = new RatioTapChangerRegulationActionBuilder();
        AbstractTapChangerRegulationActionBuilderDeserializer.ParsingContext commonParsingContext = new AbstractTapChangerRegulationActionBuilderDeserializer.ParsingContext();
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
                    builder.withTargetV(jsonParser.getValueAsDouble());
                    return true;
                default:
                    return false;
            }
        });
        return builder.withId(commonParsingContext.id)
            .withTransformerId(commonParsingContext.transformerId)
            .withSide(commonParsingContext.side)
            .withRegulating(commonParsingContext.regulating);
    }
}
