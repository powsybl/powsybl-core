/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * To easily manage an array of variant.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class VariantArray<S extends Variant> {

    private final Ref<? extends VariantManagerHolder> variantManagerHolder;

    private final List<S> variants;

    VariantArray(Ref<? extends VariantManagerHolder> variantManagerHolder, VariantFactory<S> variantFactory) {
        this.variantManagerHolder = variantManagerHolder;
        VariantManagerImpl variantManager = variantManagerHolder.get().getVariantManager();
        variants = Collections.synchronizedList(new ArrayList<S>(variantManager.getVariantArraySize()));
        for (int i = 0; i < variantManager.getVariantArraySize(); i++) {
            variants.add(null);
        }
        for (int i : variantManager.getVariantIndexes()) {
            variants.set(i, variantFactory.newVariant());
        }
    }

    S get() {
        return variants.get(variantManagerHolder.get().getVariantManager().getVariantContext().getVariantIndex());
    }

    void push(int number, VariantFactory<S> variantFactory) {
        for (int i = 0; i < number; i++) {
            variants.add(variantFactory.newVariant());
        }
    }

    void push(VariantFactory<S> variantFactory) {
        variants.add(variantFactory.newVariant());
    }

    void pop(int number) {
        for (int i = 0; i < number; i++) {
            variants.remove(variants.size() - 1);
        }
    }

    void delete(int index) {
        variants.set(index, null);
    }

    void allocate(int[] indexes, VariantFactory<S> variantFactory) {
        for (int index : indexes) {
            variants.set(index, variantFactory.newVariant());
        }
    }

    S copy(int index) {
        return variants.get(index).copy();
    }

}
