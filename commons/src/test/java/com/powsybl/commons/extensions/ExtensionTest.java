/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ExtensionTest extends AbstractConverterTest {

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "test"));

    @Test
    public void testExtendable() {
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
    public void testExtensionSupplier() {
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
    public void testReadJson() throws IOException {
        Foo foo = FooDeserializer.read(getClass().getResourceAsStream("/extensions.json"));
        assertEquals(1, foo.getExtensions().size());
        assertNotNull(foo.getExtension(FooExt.class));
        assertNull(foo.getExtension(BarExt.class));
    }

    private void assertBadExtensionJsonThrows(ThrowingRunnable runnable) {
        PowsyblException exception = assertThrows(PowsyblException.class, runnable);
        assertTrue("Exception should be about bad field exceptions, but got: " + exception.getMessage(),
                exception.getMessage().contains("\"extensions\""));
    }

    @Test
    public void testBadReadJson() throws IOException {
        assertBadExtensionJsonThrows(() -> {
            Foo foo = FooDeserializer.read(getClass().getResourceAsStream("/BadExtensions.json"));
        });
    }

    @Test
    public void testWriteJson() throws IOException {
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
    public void testUpdateAndDeserialize() throws IOException {
        Foo foo = new Foo();
        FooExt fooExt = new FooExt(false, "Hello");
        foo.addExtension(FooExt.class, fooExt);
        FooDeserializer.update(getClass().getResourceAsStream("/extensionsUpdate.json"), foo);
        assertTrue(foo.getExtension(FooExt.class).getValue());
        assertEquals("Hello", foo.getExtension(FooExt.class).getValue2());
    }

    @Test
    public void testBadUpdateAndDeserialize() throws IOException {
        assertBadExtensionJsonThrows(() -> {
            Foo foo = new Foo();
            FooDeserializer.update(getClass().getResourceAsStream("/BadExtensions.json"), foo);
        });
    }

    @Test
    public void testUpdateWith2Extensions() throws IOException {
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
    public void testProviderConflict() {
        ExtensionXmlSerializer<?, ?> mock1 = Mockito.mock(ExtensionXmlSerializer.class);
        Mockito.when(mock1.getExtensionName()).thenReturn("mock");
        ExtensionXmlSerializer<?, ?> mock2 = Mockito.mock(ExtensionXmlSerializer.class);
        Mockito.when(mock2.getExtensionName()).thenReturn("mock");

        ExtensionXmlSerializer<?, ?>[] mocks = {mock1, mock2};

        Assert.assertThrows(IllegalStateException.class, () -> Arrays.stream(mocks).collect(Collectors.toMap(ExtensionXmlSerializer::getExtensionName, e -> e)));
    }
}
