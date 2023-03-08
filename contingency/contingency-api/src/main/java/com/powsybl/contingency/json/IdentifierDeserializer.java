/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.contingency.list.identifier.*;
import com.powsybl.contingency.contingency.list.identifier.IdBasedNetworkElementIdentifier;
import com.powsybl.contingency.contingency.list.identifier.VoltageLevelAndOrderNetworkElementIdentifier;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class IdentifierDeserializer extends StdDeserializer<NetworkElementIdentifier> {

    public IdentifierDeserializer() {
        super(NetworkElementIdentifier.class);
    }

    @Override
    public NetworkElementIdentifier deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        NetworkElementIdentifier.IdentifierType type = null;
        String identifier = null;
        String voltageLevelId1 = null;
        String voltageLevelId2 = null;
        List<NetworkElementIdentifier> networkElementIdentifierList = Collections.emptyList();
        char order = 0;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "type":
                    type = NetworkElementIdentifier.IdentifierType.valueOf(parser.nextTextValue());
                    break;
                case "identifier":
                    identifier = parser.nextTextValue();
                    break;
                case "identifierList":
                    parser.nextToken();
                    networkElementIdentifierList = JsonUtil.readList(deserializationContext,
                            parser, NetworkElementIdentifier.class);
                    break;
                case "voltageLevelId1":
                    voltageLevelId1 = parser.nextTextValue();
                    break;
                case "voltageLevelId2":
                    voltageLevelId2 = parser.nextTextValue();
                    break;
                case "order":
                    String orderStr = parser.nextTextValue();
                    if (orderStr.length() != 1) {
                        throw new IllegalArgumentException("order is one character");
                    }
                    order = orderStr.charAt(0);
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        if (type == null) {
            throw new IllegalArgumentException("type of identifier can not be null");
        }
        switch (type) {
            case ID_BASED:
                return new IdBasedNetworkElementIdentifier(identifier);
            case LIST:
                return new NetworkElementIdentifierList(networkElementIdentifierList);
            case VOLTAGE_LEVELS_AND_ORDER:
                return new VoltageLevelAndOrderNetworkElementIdentifier(voltageLevelId1, voltageLevelId2, order);
            default:
                throw new IllegalArgumentException("type " + type + " does not exist");
        }
    }
}
