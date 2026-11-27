/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElement;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.it>}
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public class ContingencyDeserializer extends StdDeserializer<Contingency> {

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "security-analysis"));

    private final transient ValueDeserializer<Object> elementDeserializer;

    public ContingencyDeserializer() {
        this(null);
    }

    public ContingencyDeserializer(ValueDeserializer<Object> elementDeserializer) {
        super(Contingency.class);
        this.elementDeserializer = elementDeserializer;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext deserializationContext,
                                                BeanProperty property) throws DatabindException {
        return new ContingencyDeserializer(JsonUtil.buildListDeserializer(deserializationContext, property, ContingencyElement.class));
    }

    @Override
    public Contingency deserialize(JsonParser parser, DeserializationContext deserializationContext) throws JacksonException {
        String id = null;
        String name = null;
        List<ContingencyElement> elements = Collections.emptyList();

        List<Extension<Contingency>> extensions = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "id" -> id = parser.nextStringValue();
                case "name" -> name = parser.nextStringValue();
                case "elements" -> {
                    parser.nextToken();
                    elements = JsonUtil.readList(elementDeserializer, deserializationContext, parser, ContingencyElement.class);
                }
                case "extensions" -> {
                    parser.nextToken();
                    extensions = JsonUtil.readExtensions(parser, deserializationContext, SUPPLIER.get());
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }
        Contingency contingency = new Contingency(id, name, elements);
        SUPPLIER.get().addExtensions(contingency, extensions);

        return contingency;
    }
}
