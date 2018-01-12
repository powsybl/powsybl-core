/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class BarExt extends AbstractExtension<Foo> {

    private final boolean value;

    public BarExt(boolean value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return "BarExt";
    }

    public boolean getValue() {
        return value;
    }
}
