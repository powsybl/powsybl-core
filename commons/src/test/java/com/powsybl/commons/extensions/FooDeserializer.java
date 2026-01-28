/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.json.JsonUtil;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class FooDeserializer extends StdDeserializer<Foo> {

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "test"));

    public FooDeserializer() {
        super(Foo.class);
    }

    @Override
    public Foo deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        List<Extension<Foo>> extensions = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentName().equals("extensions")) {
                parser.nextToken();
                extensions = JsonUtil.readExtensions(parser, context);
            }
        }
        Foo foo = new Foo();
        SUPPLIER.get().addExtensions(foo, extensions);
        return foo;
    }

    static Foo read(InputStream stream) throws JacksonException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Foo.class, new FooDeserializer());
        JsonMapper mapper = JsonUtil.createJsonMapperBuilder()
            .addModule(module)
            .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();
        return mapper.readValue(stream, Foo.class);
    }

    @Override
    public Foo deserialize(JsonParser parser, DeserializationContext context, Foo initFoo) throws JacksonException {
        List<Extension<Foo>> extensions = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentName().equals("extensions")) {
                parser.nextToken();
                extensions = JsonUtil.updateExtensions(parser, context, initFoo);
            }
        }
        SUPPLIER.get().addExtensions(initFoo, extensions);
        return initFoo;
    }

    static Foo update(InputStream stream, Foo foo) throws JacksonException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Foo.class, new FooDeserializer());
        JsonMapper mapper = JsonUtil.createJsonMapperBuilder()
            .addModule(module)
            .build();
        return mapper.readerForUpdating(foo).readValue(stream);
    }
}
