/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;

import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ThreadLocalMultiStateContext implements StateContext {

    public static final ThreadLocalMultiStateContext INSTANCE = new ThreadLocalMultiStateContext();

    private final ThreadLocal<Integer> index = ThreadLocal.withInitial((Supplier<Integer>) () -> null);

    @Override
    public int getStateIndex() {
        Integer i = index.get();
        if (i == null) {
            throw new PowsyblException("State not set for current thread " + Thread.currentThread().getName());
        }
        return i;
    }

    @Override
    public void setStateIndex(int index) {
        this.index.set(index);
    }

    public void reset() {
        index.remove();
    }

    @Override
    public void resetIfStateIndexIs(int index) {
        Integer i = this.index.get();
        if (i != null && i == index) {
            this.index.remove();
        }
    }
}
