/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.identifiers.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.identifiers.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class IdentifierDeserializer extends StdDeserializer<NetworkElementIdentifier> {
    private static final String CONTEXT_NAME = "Identifier";
    private static final String CONTINGENCY_ID = "contingencyId";

    public static final String IDENTIFIER_LIST_VERSION = "identifierListVersion";

    public IdentifierDeserializer() {
        super(NetworkElementIdentifier.class);
    }

    @Override
    public NetworkElementIdentifier deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        NetworkElementIdentifier.IdentifierType type = null;
        String identifier = null;
        String voltageLevelId1 = null;
        String voltageLevelId2 = null;
        String contingencyId = null;
        String version = JsonUtil.getSourceVersion(deserializationContext, IDENTIFIER_LIST_VERSION);
        List<NetworkElementIdentifier> networkElementIdentifierList = Collections.emptyList();
        char order = 0;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "type" -> type = NetworkElementIdentifier.IdentifierType.valueOf(parser.nextTextValue());
                case "identifier" -> identifier = parser.nextTextValue();
                case CONTINGENCY_ID -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, CONTINGENCY_ID, version, "1.2");
                    contingencyId = parser.nextTextValue();
                }
                case "identifierList" -> {
                    parser.nextToken();
                    networkElementIdentifierList = JsonUtil.readList(deserializationContext,
                            parser, NetworkElementIdentifier.class);
                }
                case "voltageLevelId1" -> voltageLevelId1 = parser.nextTextValue();
                case "voltageLevelId2" -> voltageLevelId2 = parser.nextTextValue();
                case "order" -> {
                    String orderStr = parser.nextTextValue();
                    if (orderStr.length() != 1) {
                        throw new IllegalArgumentException("order is one character");
                    }
                    order = orderStr.charAt(0);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        if (type == null) {
            throw new IllegalArgumentException("type of identifier can not be null");
        }
        return switch (type) {
            case ID_BASED -> new IdBasedNetworkElementIdentifier(identifier, contingencyId);
            case LIST -> new NetworkElementIdentifierContingencyList(networkElementIdentifierList, contingencyId);
            case VOLTAGE_LEVELS_AND_ORDER ->
                    new VoltageLevelAndOrderNetworkElementIdentifier(voltageLevelId1, voltageLevelId2, order, contingencyId);
            case ELEMENT_WITH_UNKNOWN_CHARACTER ->
                new ElementWithUnknownCharacterIdentifier(identifier, contingencyId);
        };
    }
}
