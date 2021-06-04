/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
@AutoService(ExtensionSeriesSerializer.class)
public class FooExtSeriesSerializer implements ExtensionSeriesSerializer<Foo, FooExt> {

    private static final Predicate<Foo> EXISTS = foo -> foo.getExtensionByName("FooExt") != null;
    private static final Function<Foo, FooExt> GET = foo -> foo.getExtensionByName("FooExt");

    private static final Map<String, Integer> TYPE_MAP = ImmutableMap.of("fooExt_value", ExtensionSeriesSerializer.BOOLEAN_SERIES_TYPE,
                                                                "fooExt_value2", ExtensionSeriesSerializer.STRING_SERIES_TYPE);

    @Override
    public void serialize(ExtensionSeriesBuilder<?, Foo> builder) {
        builder.addBooleanSeries("fooExt_value", f -> {
            if (EXISTS.test(f)) {
                return GET.apply(f).getValue();
            } else {
                return false;
            }
        });
        builder.addStringSeries("fooExt_value2", f -> {
            if (EXISTS.test(f)) {
                return GET.apply(f).getValue2();
            } else {
                return "";
            }
        });
    }

    @Override
    public void deserialize(Foo element, String name, double value) {
    }

    @Override
    public void deserialize(Foo element, String name, int value) {
        if (EXISTS.test(element)) {
            FooExt oldExt = GET.apply(element);
            if (name.equals("fooExt_value")) {
                element.addExtension(FooExt.class, new FooExt(value == 1, oldExt.getValue2()));
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public void deserialize(Foo element, String name, String value) {
        if (EXISTS.test(element)) {
            FooExt oldExt = GET.apply(element);
            if (name.equals("fooExt_value2")) {
                element.addExtension(FooExt.class, new FooExt(oldExt.getValue(), value));
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public Map<String, Integer> getTypeMap() {
        return TYPE_MAP;
    }

    @Override
    public String getExtensionName() {
        return "FooExt";
    }

    @Override
    public String getCategoryName() {
        return "test";
    }

    @Override
    public Class<? super FooExt> getExtensionClass() {
        return FooExt.class;
    }
}
