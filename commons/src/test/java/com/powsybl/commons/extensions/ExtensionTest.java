/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ExtensionTest extends AbstractSerDeTest {

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "test"));

    @Test
    void testExtendable() {
        Foo foo = new Foo();
        FooExt fooExt = new FooExt(true);
        BarExt barExt = new BarExt(false);

        foo.addExtension(FooExt.class, fooExt);
        foo.addExtension(BarExt.class, barExt);
        assertEquals(2, foo.getExtensions().size());

        assertSame(fooExt, foo.getExtension(FooExt.class));
        assertSame(fooExt, foo.getExtensionByName("FooExt"));

        ExtensionProvider provider = SUPPLIER.get().findProvider("FooExt");
        assertNotNull(provider);
        assertSame(fooExt, foo.getExtension(provider.getExtensionClass()));
        assertSame(fooExt, foo.getExtensionByName(provider.getExtensionName()));

        assertSame(barExt, foo.getExtension(BarExt.class));
        assertSame(barExt, foo.getExtensionByName("BarExt"));

        assertTrue(foo.removeExtension(FooExt.class));
        assertNull(foo.getExtension(FooExt.class));
        assertEquals(1, foo.getExtensions().size());

        assertTrue(foo.removeExtension(BarExt.class));
        assertNull(foo.getExtension(BarExt.class));
        assertEquals(0, foo.getExtensions().size());
    }

    @Test
    void testExtensionSupplier() {
        assertNotNull(SUPPLIER.get().findProvider("FooExt"));
        assertNotNull(SUPPLIER.get().findProviderOrThrowException("FooExt"));
        assertNotNull(SUPPLIER.get().findProvider("BarExt"));

        ExtensionProviders<? extends ExtensionJsonSerializer> supplier = ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "test", Collections.singleton("FooExt"));
        assertNotNull(supplier);
        assertNotNull(supplier.findProviderOrThrowException("FooExt"));
        assertNull(supplier.findProvider("BarExt"));
        try {
            supplier.findProviderOrThrowException("BarExt");
            fail();
        } catch (PowsyblException e) {
            // Nothing to do
        }
    }

    @Test
    void testReadJson() throws IOException {
        Foo foo = FooDeserializer.read(getClass().getResourceAsStream("/extensions.json"));
        assertEquals(1, foo.getExtensions().size());
        assertNotNull(foo.getExtension(FooExt.class));
        assertNull(foo.getExtension(BarExt.class));
    }

    private void assertBadExtensionJsonThrows(Executable runnable) {
        PowsyblException exception = assertThrows(PowsyblException.class, runnable);
        assertTrue(exception.getMessage().contains("\"extensions\""),
                "Exception should be about bad field exceptions, but got: " + exception.getMessage());
    }

    @Test
    void testBadReadJson() throws IOException {
        assertBadExtensionJsonThrows(() -> {
            Foo foo = FooDeserializer.read(getClass().getResourceAsStream("/BadExtensions.json"));
        });
    }

    @Test
    void testWriteJson() throws IOException {
        Files.createFile(tmpDir.resolve("extensions.json"));
        ExtensionProviders<? extends ExtensionJsonSerializer> supplier = ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "test", Collections.singleton("FooExt"));
        Foo foo = new Foo();
        FooExt fooExt = new FooExt(true);
        BarExt barExt = new BarExt(false);
        foo.addExtension(FooExt.class, fooExt);
        foo.addExtension(BarExt.class, barExt);

        try (JsonGenerator jsonGen = new JsonFactory().createGenerator(Files.newOutputStream(tmpDir.resolve("extensions.json")), JsonEncoding.UTF8)) {
            jsonGen.writeStartObject();
            Set<String> notFound = JsonUtil.writeExtensions(foo, jsonGen, new DefaultSerializerProvider.Impl(), supplier);
            jsonGen.writeEndObject();
            assertNotNull(notFound);
            assertFalse(notFound.isEmpty());
            assertEquals(1, notFound.size());
            assertTrue(notFound.contains("BarExt"));
        }
    }

    @Test
    void testUpdateAndDeserialize() throws IOException {
        Foo foo = new Foo();
        FooExt fooExt = new FooExt(false, "Hello");
        foo.addExtension(FooExt.class, fooExt);
        FooDeserializer.update(getClass().getResourceAsStream("/extensionsUpdate.json"), foo);
        assertTrue(foo.getExtension(FooExt.class).getValue());
        assertEquals("Hello", foo.getExtension(FooExt.class).getValue2());
    }

    @Test
    void testBadUpdateAndDeserialize() throws IOException {
        assertBadExtensionJsonThrows(() -> {
            Foo foo = new Foo();
            FooDeserializer.update(getClass().getResourceAsStream("/BadExtensions.json"), foo);
        });
    }

    @Test
    void testUpdateWith2Extensions() throws IOException {
        Foo foo = new Foo();
        FooExt fooExt = new FooExt(false, "Hello");
        BarExt barExt = new BarExt(true);
        foo.addExtension(FooExt.class, fooExt);
        foo.addExtension(BarExt.class, barExt);
        FooDeserializer.update(getClass().getResourceAsStream("/extensions.json"), foo);
        assertTrue(foo.getExtension(FooExt.class).getValue());
        assertEquals("Hello", foo.getExtension(FooExt.class).getValue2());
        assertFalse(foo.getExtension(BarExt.class).getValue());
    }

    @Test
    void testProviderConflict() {
        ExtensionSerDe<?, ?> mock1 = Mockito.mock(ExtensionSerDe.class);
        Mockito.when(mock1.getExtensionName()).thenReturn("mock");
        ExtensionSerDe<?, ?> mock2 = Mockito.mock(ExtensionSerDe.class);
        Mockito.when(mock2.getExtensionName()).thenReturn("mock");

        ExtensionSerDe<?, ?>[] mocks = {mock1, mock2};

        assertThrows(IllegalStateException.class, () -> Arrays.stream(mocks).collect(Collectors.toMap(ExtensionSerDe::getExtensionName, e -> e)));
    }
}
