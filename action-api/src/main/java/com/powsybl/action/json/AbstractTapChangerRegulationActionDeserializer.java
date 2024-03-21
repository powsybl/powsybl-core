/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.action.AbstractTapChangerRegulationAction;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public abstract class AbstractTapChangerRegulationActionDeserializer<T extends AbstractTapChangerRegulationAction> extends StdDeserializer<T> {

    protected AbstractTapChangerRegulationActionDeserializer(Class<T> vc) {
        super(vc);
    }

    protected static class ParsingContext {
        String id;
        String transformerId;
        boolean regulating;
        ThreeSides side = null;
    }

    protected boolean deserializeCommonAttributes(JsonParser jsonParser, ParsingContext context, String name) throws IOException {
        switch (name) {
            case "id":
                context.id = jsonParser.nextTextValue();
                return true;
            case "transformerId":
                context.transformerId = jsonParser.nextTextValue();
                return true;
            case "side":
                context.side = ThreeSides.valueOf(jsonParser.nextTextValue());
                return true;
            case "regulating":
                jsonParser.nextToken();
                context.regulating = jsonParser.getValueAsBoolean();
                return true;
            default:
                return false;
        }
    }
}
