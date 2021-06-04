/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class FooExt extends AbstractExtension<Foo> {

    private final boolean value;
    private final String value2;

    public FooExt(boolean value, String value2) {
        this.value = value;
        this.value2 = value2;
    }

    public FooExt(boolean value) {
        this.value = value;
        this.value2 = "Hello";
    }

    @Override
    public String getName() {
        return "FooExt";
    }

    public boolean getValue() {
        return value;
    }

    public String getValue2() {
        return value2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FooExt)) {
            return false;
        }
        FooExt fooExt = (FooExt) o;
        return value == fooExt.value &&
                value2.equals(fooExt.value2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, value2);
    }
}
