/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class ContingencyDeserializer extends StdDeserializer<Contingency> {

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "security-analysis"));

    public ContingencyDeserializer() {
        super(Contingency.class);
    }

    @Override
    public Contingency deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String id = null;
        List<ContingencyElement> elements = Collections.emptyList();

        List<Extension<Contingency>> extensions = Collections.emptyList();


        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "id":
                    id = parser.nextTextValue();
                    break;

                case "elements":
                    parser.nextToken();
                    elements = parser.readValueAs(new TypeReference<ArrayList<ContingencyElement>>() {
                    });
                    break;

                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.readExtensions(parser, deserializationContext, SUPPLIER.get());
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        Contingency contingency = new Contingency(id, elements);
        SUPPLIER.get().addExtensions(contingency, extensions);

        return contingency;
    }
}
