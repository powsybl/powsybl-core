/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ThreadLocalMultiVariantContext implements VariantContext {

    private final ThreadLocal<Integer> index = ThreadLocal.withInitial(() -> null);

    @Override
    public int getVariantIndex() {
        Integer i = index.get();
        if (i == null) {
            throw new PowsyblException("Variant index not set for current thread " + Thread.currentThread().getName());
        }
        return i;
    }

    @Override
    public void setVariantIndex(int index) {
        this.index.set(index);
    }

    public void reset() {
        index.remove();
    }

    @Override
    public void resetIfVariantIndexIs(int index) {
        Integer i = this.index.get();
        if (i != null && i == index) {
            this.index.remove();
        }
    }

    @Override
    public boolean isIndexSet() {
        return this.index.get() != null;
    }

}
