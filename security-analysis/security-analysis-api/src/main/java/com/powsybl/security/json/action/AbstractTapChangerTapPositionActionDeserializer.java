/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import com.powsybl.contingency.contingency.list.identifier.IdBasedNetworkElementIdentifier;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.security.action.AbstractTapChangerTapPositionAction;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public abstract class AbstractTapChangerTapPositionActionDeserializer<T extends AbstractTapChangerTapPositionAction> extends StdDeserializer<T> {

    protected AbstractTapChangerTapPositionActionDeserializer(Class<T> vc) {
        super(vc);
    }

    protected static class ParsingContext {
        String id;
        List<NetworkElementIdentifier> networkElementIdentifiers;
        int tapPosition;
        Boolean relativeValue;
        ThreeSides side = null;
    }

    protected boolean deserializeCommonAttributes(JsonParser jsonParser, ParsingContext context, String name, String version, DeserializationContext deserializationContext) throws IOException {
        switch (name) {
            case "id":
                context.id = jsonParser.nextTextValue();
                return true;
            case "transformerId":
                context.networkElementIdentifiers = Collections.singletonList(new IdBasedNetworkElementIdentifier(jsonParser.nextTextValue()));
                return true;
            case "identifiers":
                jsonParser.nextToken();
                context.networkElementIdentifiers = JsonUtil.readList(deserializationContext, jsonParser, NetworkElementIdentifier.class);
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
                context.side = ThreeSides.valueOf(jsonParser.nextTextValue());
                return true;
            default:
                return false;
        }
    }

    protected void checkFields(ParsingContext context, JsonParser jsonParser) throws JsonMappingException {
        if (context.relativeValue == null) {
            throw JsonMappingException.from(jsonParser, "for phase tap changer tap position action relative value field can't be null");
        }
    }
}
