/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.VariantManagerConstants;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VariantManagerImplTest {

    private static final class IdentifiableMock extends AbstractExtendable<IdentifiableMock> implements Identifiable<IdentifiableMock>, MultiVariantObject {

        private final String id;

        private final Set<Integer> extended = new HashSet<>();

        private final Set<Integer> deleted = new HashSet<>();

        private int reducedCount = 0;

        private IdentifiableMock(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return id;
        }

        @Override
        public boolean hasProperty() {
            return false;
        }

        @Override
        public Properties getProperties() {
            return null;
        }

        @Override
        public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
            for (int i = 0; i < number; i++) {
                extended.add(sourceIndex);
            }
        }

        @Override
        public void reduceVariantArraySize(int number) {
            reducedCount += number;
        }

        @Override
        public void deleteVariantArrayElement(int index) {
            deleted.add(index);
        }

        @Override
        public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        }
    }

    public VariantManagerImplTest() {
    }

    @Test
    public void test() {
        ObjectStore objectStore = new ObjectStore();
        IdentifiableMock identifiable1 = new IdentifiableMock("1");
        objectStore.checkAndAdd(identifiable1);
        VariantManagerImpl variantManager = new VariantManagerImpl(objectStore);
        // initial variant test
        assertEquals(1, variantManager.getVariantArraySize());
        assertEquals(Collections.singleton(VariantManagerConstants.INITIAL_VARIANT_ID), variantManager.getVariantIds());
        assertEquals(Collections.singleton(0), variantManager.getVariantIndexes());
        try {
            variantManager.setWorkingVariant("UnknownVariant");
            fail();
        } catch (PowsyblException ignored) {
        }
        try {
            variantManager.removeVariant("UnknownVariant");
            fail();
        } catch (PowsyblException ignored) {
        }
        try {
            variantManager.removeVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
            fail();
        } catch (PowsyblException ignored) {
        }
        // cloning test
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "ClonedVariant1");
        assertEquals(2, variantManager.getVariantArraySize());
        assertEquals(Sets.newHashSet(VariantManagerConstants.INITIAL_VARIANT_ID, "ClonedVariant1"), variantManager.getVariantIds());
        assertEquals(Sets.newHashSet(0, 1), variantManager.getVariantIndexes());
        assertEquals(Collections.singleton(0), identifiable1.extended);
        // second cloning test
        variantManager.cloneVariant("ClonedVariant1", "ClonedVariant2");
        assertEquals(3, variantManager.getVariantArraySize());
        assertEquals(Sets.newHashSet(VariantManagerConstants.INITIAL_VARIANT_ID, "ClonedVariant1", "ClonedVariant2"), variantManager.getVariantIds());
        assertEquals(Sets.newHashSet(0, 1, 2), variantManager.getVariantIndexes());
        assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, variantManager.getWorkingVariantId());
        variantManager.setWorkingVariant("ClonedVariant1");
        assertEquals("ClonedVariant1", variantManager.getWorkingVariantId());
        // "middle" variant removing test
        variantManager.removeVariant("ClonedVariant1");
        try {
            assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, variantManager.getWorkingVariantId()); // because variant is not set
            fail();
        } catch (Exception ignored) {
        }
        assertEquals(3, variantManager.getVariantArraySize());
        assertEquals(Sets.newHashSet(VariantManagerConstants.INITIAL_VARIANT_ID, "ClonedVariant2"), variantManager.getVariantIds());
        assertEquals(Sets.newHashSet(0, 2), variantManager.getVariantIndexes());
        assertEquals(Collections.singleton(1), identifiable1.deleted);
        // variant array index recycling test
        variantManager.cloneVariant("ClonedVariant2", "ClonedVariant3");
        assertEquals(3, variantManager.getVariantArraySize());
        assertEquals(Sets.newHashSet(VariantManagerConstants.INITIAL_VARIANT_ID, "ClonedVariant2", "ClonedVariant3"), variantManager.getVariantIds());
        assertEquals(Sets.newHashSet(0, 1, 2), variantManager.getVariantIndexes());
        // variant array reduction test
        variantManager.removeVariant("ClonedVariant3");
        assertEquals(3, variantManager.getVariantArraySize());
        variantManager.removeVariant("ClonedVariant2");
        assertEquals(1, variantManager.getVariantArraySize());
        assertEquals(Collections.singleton(VariantManagerConstants.INITIAL_VARIANT_ID), variantManager.getVariantIds());
        assertEquals(Collections.singleton(0), variantManager.getVariantIndexes());
        assertEquals(2, identifiable1.reducedCount);
    }
}
