/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import gnu.trove.list.array.TIntArrayList;
import org.junit.jupiter.api.Test;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class MultiVariantExtensionTest {

    static class LoadExt extends AbstractMultiVariantIdentifiableExtension<Load> {

        private final TIntArrayList values;

        LoadExt(Load load, int value) {
            super(load);

            int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
            this.values = new TIntArrayList(variantArraySize);
            for (int i = 0; i < variantArraySize; i++) {
                this.values.add(value);
            }
        }

        @Override
        public String getName() {
            return "load-ext";
        }

        @Override
        public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
            values.ensureCapacity(values.size() + number);
            for (int i = 0; i < number; i++) {
                values.add(values.get(sourceIndex));
            }
        }

        @Override
        public void reduceVariantArraySize(int number) {
            values.remove(values.size() - number, number);
        }

        @Override
        public void deleteVariantArrayElement(int index) {
            values.set(index, -1);
        }

        @Override
        public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
            for (int index : indexes) {
                values.set(index, values.get(sourceIndex));
            }
        }

        int getValue() {
            return values.get(getVariantIndex());
        }

        void setValue(int value) {
            values.set(getVariantIndex(), value);
        }
    }

    @Test
    void test() {
        String variant1 = "variant1";
        String variant2 = "variant2";

        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");

        LoadExt ext = new LoadExt(load, 0);
        load.addExtension(LoadExt.class, ext);

        assertEquals(0, ext.getValue());

        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        assertEquals(2, ext.values.size());

        variantManager.setWorkingVariant(variant1);
        ext.setValue(4);
        assertEquals(4, ext.getValue());

        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertEquals(0, ext.getValue());

        variantManager.cloneVariant(variant1, variant2);
        assertEquals(3, ext.values.size());

        variantManager.setWorkingVariant(variant1);
        ext.setValue(2);

        variantManager.setWorkingVariant(variant2);
        assertEquals(4, ext.getValue());
        assertArrayEquals(new int[]{0, 2, 4}, ext.values.toArray());

        variantManager.removeVariant(variant1);
        assertEquals(3, ext.values.size());
        assertArrayEquals(new int[]{0, -1, 4}, ext.values.toArray());

        variantManager.removeVariant(variant2);
        assertEquals(1, ext.values.size());
        assertArrayEquals(new int[]{0}, ext.values.toArray());
    }
}
