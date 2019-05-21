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
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import gnu.trove.list.array.TDoubleArrayList;
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

    private static final class LoadExtension extends AbstractExtension<Load> implements MultiVariantObject {

        private final TDoubleArrayList values;

        LoadExtension(Load load, double value) {
            super(load);

            int variantArraySize = getNetwork().getVariantManager().getVariantArraySize();
            this.values = new TDoubleArrayList(variantArraySize);
            for (int i = 0; i < variantArraySize; ++i) {
                this.values.add(value);
            }
        }

        @Override
        public String getName() {
            return "load-extension";
        }

        @Override
        public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
            values.ensureCapacity(values.size() + number);
            for (int i = 0; i < number; ++i) {
                values.add(values.get(sourceIndex));
            }
        }

        @Override
        public void reduceVariantArraySize(int number) {
            values.remove(values.size() - number, number);
        }

        @Override
        public void deleteVariantArrayElement(int index) {
            // Nothing to do
        }

        @Override
        public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
            for (int index : indexes) {
                values.set(index, values.get(sourceIndex));
            }
        }

        double getValue() {
            return this.values.get(getNetwork().getVariantIndex());
        }

        LoadExtension setValue(double value) {
            this.values.set(getNetwork().getVariantIndex(), value);
            return this;
        }

        private NetworkImpl getNetwork() {
            return (NetworkImpl) getExtendable().getTerminal().getVoltageLevel().getSubstation().getNetwork();
        }
    }

    public VariantManagerImplTest() {
    }

    @Test
    public void test() {
        NetworkIndex index = new NetworkIndex();
        IdentifiableMock identifiable1 = new IdentifiableMock("1");
        index.checkAndAdd(identifiable1);
        VariantManagerImpl variantManager = new VariantManagerImpl(index);
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
        // clone test with overwriting
        variantManager.cloneVariant("ClonedVariant2", VariantManagerConstants.INITIAL_VARIANT_ID, true);
        assertEquals(3, variantManager.getVariantArraySize());
        assertEquals(Sets.newHashSet(VariantManagerConstants.INITIAL_VARIANT_ID, "ClonedVariant1", "ClonedVariant2"), variantManager.getVariantIds());
        assertEquals(Sets.newHashSet(0, 1, 2), variantManager.getVariantIndexes());
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, variantManager.getWorkingVariantId());
        variantManager.setWorkingVariant("ClonedVariant1");
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

    @Test
    public void testMultipleNetworks() {
        NetworkIndex index = new NetworkIndex();

        VariantManager variantManager1 = new VariantManagerImpl(index);
        variantManager1.allowVariantMultiThreadAccess(true);
        assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, variantManager1.getWorkingVariantId());

        VariantManager variantManager2 = new VariantManagerImpl(index);
        variantManager2.allowVariantMultiThreadAccess(true);
        assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, variantManager1.getWorkingVariantId());

        variantManager1.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "testVariant");

        variantManager1.setWorkingVariant("testVariant");
        assertEquals("testVariant", variantManager1.getWorkingVariantId());

        //active variant for variant manager 2 should not have changed
        assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, variantManager2.getWorkingVariantId());
    }

    @Test
    public void testMultiStateExtensions() {
        String variante1 = "v1";
        String variante2 = "v2";

        Network network = EurostagTutorialExample1Factory.create();
        VariantManager manager = network.getVariantManager();

        Load load = network.getLoad("LOAD");
        LoadExtension loadExtension = new LoadExtension(load, 100);
        load.addExtension(LoadExtension.class, loadExtension);

        manager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variante1);
        manager.setWorkingVariant(variante1);

        assertEquals(100.0, loadExtension.getValue(), 0.0);
        loadExtension.setValue(200);
        assertEquals(200.0, loadExtension.getValue(), 0.0);

        manager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(100.0, loadExtension.getValue(), 0.0);

        manager.cloneVariant(variante1, variante2);
        manager.setWorkingVariant(variante2);
        assertEquals(200.0, loadExtension.getValue(), 0.0);

        loadExtension.setValue(300);
        manager.removeVariant(variante1);
        assertEquals(300.0, loadExtension.getValue(), 0.0);

        manager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(100.0, loadExtension.getValue(), 0.0);

        manager.removeVariant(variante2);
        assertEquals(100.0, loadExtension.getValue(), 0.0);
    }

    @Test
    public void overwriteVariant() {
        String variante1 = "v1";

        Network network = EurostagTutorialExample1Factory.create();
        VariantManager manager = network.getVariantManager();

        Generator generator = network.getGenerator("GEN");
        assertEquals(607.0, generator.getTargetP(), 0.0);

        manager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variante1);
        manager.setWorkingVariant(variante1);
        assertEquals(607.0, generator.getTargetP(), 0.0);
        generator.setTargetP(605);
        assertEquals(605.0, generator.getTargetP(), 0.0);
        manager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(607.0, generator.getTargetP(), 0.0);
        manager.cloneVariant(variante1, VariantManagerConstants.INITIAL_VARIANT_ID, true);
        manager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(605.0, generator.getTargetP(), 0.0);
    }
}
