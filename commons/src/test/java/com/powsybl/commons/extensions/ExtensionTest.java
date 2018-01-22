/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.PowsyblException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class ExtensionTest extends AbstractConverterTest {

    private static final Supplier<ExtensionSerializerProvider> SUPPLIER =
        Suppliers.memoize(() -> ExtensionSerializerProviders.createProvider(ExtensionJsonSerializer.class, "test"));

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

        ExtensionSerializer serializer = SUPPLIER.get().findSerializer("FooExt");
        assertNotNull(serializer);
        assertSame(fooExt, foo.getExtension(serializer.getExtensionClass()));
        assertSame(fooExt, foo.getExtensionByName(serializer.getExtensionName()));

        assertSame(barExt, foo.getExtension(BarExt.class));
        assertSame(barExt, foo.getExtensionByName("BarExt"));

        foo.removeExtension(FooExt.class);
        assertEquals(1, foo.getExtensions().size());

        foo.removeExtension(BarExt.class);
        assertEquals(0, foo.getExtensions().size());
    }

    @Test
    public void testExtensionSupplier() {
        assertNotNull(SUPPLIER.get().findSerializer("FooExt"));
        assertNotNull(SUPPLIER.get().findSerializerOrThrowException("FooExt"));

        assertNull(SUPPLIER.get().findSerializer("BarExt"));
        try {
            SUPPLIER.get().findSerializerOrThrowException("BarExt");
            fail();
        } catch (PowsyblException e) {
            // Nothing to do
        }
    }

    @Test
    public void testJson() throws IOException {
        Foo foo = FooDeserializer.read(getClass().getResourceAsStream("/extensions.json"));

        assertEquals(1, foo.getExtensions().size());
        assertNotNull(foo.getExtension(FooExt.class));
        assertNull(foo.getExtension(BarExt.class));
    }
}
