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
import com.powsybl.security.action.*;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class ActionDeserializer extends StdDeserializer<Action> {

    public ActionDeserializer() {
        super(Action.class);
    }

    @Override
    public Action deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        parser.nextToken();
        while (parser.getCurrentName() != null) {
            if ("type".equals(parser.getCurrentName())) {
                switch (parser.nextTextValue()) {
                    case LineConnectionAction.NAME:
                        LineConnectionActionDeserializer lineConnectionActionDeserializer = new LineConnectionActionDeserializer();
                        return lineConnectionActionDeserializer.deserialize(parser, deserializationContext);
                    case MultipleActionsAction.NAME:
                        MultipleActionsActionDeserializer multipleActionsActionDeserializer = new MultipleActionsActionDeserializer();
                        return multipleActionsActionDeserializer.deserialize(parser, deserializationContext);
                    case PhaseTapChangerTapPositionAction.NAME:
                        PhaseTapChangerTapPositionActionDeserializer phaseTapChangerTapPositionActionDeserializer = new PhaseTapChangerTapPositionActionDeserializer();
                        return phaseTapChangerTapPositionActionDeserializer.deserialize(parser, deserializationContext);
                    case SwitchAction.NAME:
                        SwitchActionDeserializer switchActionDeserializer = new SwitchActionDeserializer();
                        return switchActionDeserializer.deserialize(parser, deserializationContext);
                    default:
                        throw JsonMappingException.from(parser, "Unknown action type: " + parser.getCurrentName());
                }
            }
            parser.nextToken();
        }
        throw new IllegalArgumentException("contingency List needs a type");
    }
}
