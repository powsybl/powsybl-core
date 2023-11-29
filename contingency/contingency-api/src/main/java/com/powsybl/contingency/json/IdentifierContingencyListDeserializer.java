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
import com.powsybl.contingency.contingency.list.IdentifierContingencyList;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class IdentifierContingencyListDeserializer extends StdDeserializer<IdentifierContingencyList> {

    private static final String CONTEXT_NAME = "identifierContingencyList";

    public IdentifierContingencyListDeserializer() {
        super(IdentifierContingencyList.class);
    }

    @Override
    public IdentifierContingencyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String name = null;
        String version = null;
        List<NetworkElementIdentifier> networkElementIdentifiers = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version" -> {
                    version = parser.nextTextValue();
                    deserializationContext.setAttribute("version", version);
                }
                case "identifiableType" -> {
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "identifiableType", version, "1.0");
                    parser.nextToken();
                }
                case "name" -> name = parser.nextTextValue();
                case "type" -> parser.nextToken();
                case "identifiers" -> {
                    parser.nextToken();
                    networkElementIdentifiers = JsonUtil.readList(deserializationContext, parser, NetworkElementIdentifier.class);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new IdentifierContingencyList(name, networkElementIdentifiers);
    }
}
