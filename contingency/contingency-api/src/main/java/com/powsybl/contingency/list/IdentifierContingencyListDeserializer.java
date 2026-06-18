/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.list;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.identifiers.NetworkElementIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.powsybl.iidm.network.identifiers.json.IdentifierDeserializer.IDENTIFIER_LIST_VERSION;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class IdentifierContingencyListDeserializer extends StdDeserializer<IdentifierContingencyList> implements ContextualDeserializer {

    private static final String CONTEXT_NAME = "identifierContingencyList";

    private final JsonDeserializer<Object> identifiersDeserializer;

    public IdentifierContingencyListDeserializer() {
        this(null);
    }

    protected IdentifierContingencyListDeserializer(JsonDeserializer<?> identifiersDeserializer) {
        super(IdentifierContingencyList.class);
        this.identifiersDeserializer = (JsonDeserializer<Object>) identifiersDeserializer;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JavaType type = ctxt.getTypeFactory().constructCollectionType(ArrayList.class, NetworkElementIdentifier.class);
        JsonDeserializer<?> deser = ctxt.findContextualValueDeserializer(type, property);
        return new IdentifierContingencyListDeserializer(deser);
    }

    @Override
    public IdentifierContingencyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String name = null;
        String version = null;
        List<NetworkElementIdentifier> networkElementIdentifiers = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String test = parser.currentName();
            switch (test) {
                case "version" -> {
                    version = parser.nextTextValue();
                    JsonUtil.setSourceVersion(deserializationContext, version, IDENTIFIER_LIST_VERSION);
                }
                case "identifiableType" -> {
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "identifiableType",
                            version, "1.0");
                    parser.nextToken();
                }
                case "name" -> name = parser.nextTextValue();
                case "type" -> {
                    if (!parser.nextTextValue().equals(IdentifierContingencyList.TYPE)) {
                        throw new IllegalStateException("type should be: " + IdentifierContingencyList.TYPE);
                    }
                }
                case "identifiers" -> {
                    parser.nextToken();
                    networkElementIdentifiers = identifiersDeserializer != null ?
                        (List<NetworkElementIdentifier>) identifiersDeserializer.deserialize(parser, deserializationContext) :
                        JsonUtil.readList(deserializationContext, parser, NetworkElementIdentifier.class);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }
        return new IdentifierContingencyList(name, networkElementIdentifiers);
    }
}
