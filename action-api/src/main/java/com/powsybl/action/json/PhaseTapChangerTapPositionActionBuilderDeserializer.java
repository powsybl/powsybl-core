/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.PhaseTapChangerTapPositionAction;
import com.powsybl.action.PhaseTapChangerTapPositionActionBuilder;
import com.powsybl.commons.json.JsonUtil;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class PhaseTapChangerTapPositionActionBuilderDeserializer
    extends AbstractTapChangerTapPositionActionBuilderDeserializer<PhaseTapChangerTapPositionActionBuilder> {

    public PhaseTapChangerTapPositionActionBuilderDeserializer() {
        super(PhaseTapChangerTapPositionActionBuilder.class);
    }

    @Override
    public PhaseTapChangerTapPositionActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
        PhaseTapChangerTapPositionActionBuilder builder = new PhaseTapChangerTapPositionActionBuilder();
        String version = (String) deserializationContext.getAttribute(ActionListDeserializer.VERSION);
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            boolean found = deserializeCommonAttributes(jsonParser, builder, name, version);
            if (found) {
                return true;
            }
            if (name.equals("type")) {
                String type = jsonParser.nextStringValue();
                if (!PhaseTapChangerTapPositionAction.NAME.equals(type)) {
                    throw DatabindException.from(jsonParser, "Expected type :" + PhaseTapChangerTapPositionAction.NAME + " got : " + type);
                }
                return true;
            }
            return false;
        });
        checkFields(builder, jsonParser);
        return builder;
    }
}
