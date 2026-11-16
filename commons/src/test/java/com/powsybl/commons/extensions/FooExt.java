/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
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
}
