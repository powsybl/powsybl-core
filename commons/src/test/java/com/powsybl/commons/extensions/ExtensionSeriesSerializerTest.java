/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ExtensionSeriesSerializerTest {

    private static final Supplier<ExtensionProviders<ExtensionSeriesSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionSeriesSerializer.class, "test"));

    @Test
    public void test() {
        ExtensionProvider provider = SUPPLIER.get().findProvider("FooExt");
        assertNotNull(provider);
        Collection<ExtensionSeriesSerializer> providers = SUPPLIER.get().getProviders();
        List<ExtensionSeriesSerializer> list = new ArrayList<>(providers);
        ExtensionSeriesSerializer extensionSeriesSerializer = list.get(0);
        var f1 = new Foo();
        f1.addExtension(FooExt.class, new FooExt(false, "foo"));
        var f2 = new Foo();
        var mockSeriesBuilder = new MockSeriesBuilder(List.of(f1, f2));
        extensionSeriesSerializer.serialize(mockSeriesBuilder);
        mockSeriesBuilder.check();

        // add extension
        extensionSeriesSerializer.deserialize(f2, "fooExt_value", 1);
        assertEquals(new FooExt(true, ""), f2.getExtension(FooExt.class));
        extensionSeriesSerializer.deserialize(f1, "fooExt_value", 1);
        extensionSeriesSerializer.deserialize(f1, "fooExt_value2", "bar");
        assertEquals(new FooExt(true, "bar"), f1.getExtension(FooExt.class));
    }

    class MockSeriesBuilder implements ExtensionSeriesBuilder<MockSeriesBuilder, Foo> {

        private final List<Foo> elements;
        private final List<Boolean> values;
        private final List<String> values2;

        public MockSeriesBuilder(List<Foo> elements) {
            this.elements = elements;
            values = new ArrayList<>();
            values2 = new ArrayList<>();
        }

        @Override
        public MockSeriesBuilder addBooleanSeries(String seriesName, Predicate<Foo> booleanGetter) {
            if (seriesName.equals("fooExt_value")) {
                for (int i = 0; i < elements.size(); i++) {
                    values.add(booleanGetter.test(elements.get(i)));
                }
            }
            return this;
        }

        @Override
        public MockSeriesBuilder addDoubleSeries(String seriesName, ToDoubleFunction<Foo> doubleGetter) {
            return this;
        }

        @Override
        public MockSeriesBuilder addStringSeries(String seriesName, Function<Foo, String> stringGetter) {
            if (seriesName.equals("fooExt_value2")) {
                for (int i = 0; i < elements.size(); i++) {
                    values2.add(stringGetter.apply(elements.get(i)));
                }
            }
            return this;
        }

        @Override
        public <U> MockSeriesBuilder addIntSeries(String seriesName, Function<Foo, U> objectGetter, ToIntFunction<U> intGetter, int undefinedValue) {
            return this;
        }

        void check() {
            assertEquals(Arrays.asList(false, false), values);
            assertEquals(List.of("foo", ""), values2);
        }
    }
}
