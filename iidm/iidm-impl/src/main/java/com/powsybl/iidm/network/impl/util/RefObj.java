/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.util;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RefObj<T> implements Ref<T> {

    private T o;

    public RefObj(T o) {
        this.o = o;
    }

    @Override
    public T get() {
        return o;
    }

    public void set(T o) {
        this.o = o;
    }
}
