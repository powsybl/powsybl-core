/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.report.ReportNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class FooDeserializer extends StdDeserializer<Foo> {

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "test"));

    private ReportNode reportNode;

    public FooDeserializer() {
        super(Foo.class);
    }

    public FooDeserializer(final ReportNode reportNode) {
        super(Foo.class);
        this.reportNode = reportNode;
    }

    @Override
    public Foo deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        List<Extension<Foo>> extensions = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentName().equals("extensions")) {
                parser.nextToken();
                if (reportNode != null) {
                    extensions = JsonUtil.readExtensions(parser, context, SUPPLIER.get(), null, reportNode);
                } else {
                    extensions = JsonUtil.readExtensions(parser, context);
                }
            }
        }
        Foo foo = new Foo();
        SUPPLIER.get().addExtensions(foo, extensions);
        return foo;
    }

    static Foo read(InputStream stream) throws IOException {
        ObjectMapper mapper = JsonUtil.createObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Foo.class, new FooDeserializer());
        mapper.registerModule(module);
        return mapper.readValue(stream, Foo.class);
    }

    static Foo read(InputStream stream, ReportNode reportNode) throws IOException {
        ObjectMapper mapper = JsonUtil.createObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Foo.class, new FooDeserializer(reportNode));
        mapper.registerModule(module);
        return mapper.readValue(stream, Foo.class);
    }

    @Override
    public Foo deserialize(JsonParser parser, DeserializationContext context, Foo initFoo) throws IOException {
        List<Extension<Foo>> extensions = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentName().equals("extensions")) {
                parser.nextToken();
                if (reportNode != null) {
                    extensions = JsonUtil.updateExtensions(parser, context, SUPPLIER.get(), initFoo, reportNode);
                } else {
                    extensions = JsonUtil.updateExtensions(parser, context, initFoo);
                }
            }
        }
        SUPPLIER.get().addExtensions(initFoo, extensions);
        return initFoo;
    }

    static Foo update(InputStream stream, Foo foo) throws IOException {
        ObjectMapper mapper = JsonUtil.createObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Foo.class, new FooDeserializer());
        mapper.registerModule(module);
        return mapper.readerForUpdating(foo).readValue(stream);
    }

    static Foo update(InputStream stream, Foo foo, ReportNode reportNode) throws IOException {
        ObjectMapper mapper = JsonUtil.createObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Foo.class, new FooDeserializer(reportNode));
        mapper.registerModule(module);
        return mapper.readerForUpdating(foo).readValue(stream);
    }
}
