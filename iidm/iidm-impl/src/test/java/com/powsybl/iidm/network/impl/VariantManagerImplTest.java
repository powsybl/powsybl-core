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
import com.powsybl.iidm.network.StateManagerConstants;
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
        VariantManagerImpl stateManager = new VariantManagerImpl(objectStore);
        // initial state test
        assertEquals(1, stateManager.getVariantArraySize());
        assertEquals(Collections.singleton(StateManagerConstants.INITIAL_STATE_ID), stateManager.getStateIds());
        assertEquals(Collections.singleton(0), stateManager.getVariantIndexes());
        try {
            stateManager.setWorkingState("UnknownState");
            fail();
        } catch (PowsyblException ignored) {
        }
        try {
            stateManager.removeState("UnknownState");
            fail();
        } catch (PowsyblException ignored) {
        }
        try {
            stateManager.removeState(StateManagerConstants.INITIAL_STATE_ID);
            fail();
        } catch (PowsyblException ignored) {
        }
        // cloning test
        stateManager.cloneState(StateManagerConstants.INITIAL_STATE_ID, "ClonedState1");
        assertEquals(2, stateManager.getVariantArraySize());
        assertEquals(Sets.newHashSet(StateManagerConstants.INITIAL_STATE_ID, "ClonedState1"), stateManager.getStateIds());
        assertEquals(Sets.newHashSet(0, 1), stateManager.getVariantIndexes());
        assertEquals(Collections.singleton(0), identifiable1.extended);
        // second cloning test
        stateManager.cloneState("ClonedState1", "ClonedState2");
        assertEquals(3, stateManager.getVariantArraySize());
        assertEquals(Sets.newHashSet(StateManagerConstants.INITIAL_STATE_ID, "ClonedState1", "ClonedState2"), stateManager.getStateIds());
        assertEquals(Sets.newHashSet(0, 1, 2), stateManager.getVariantIndexes());
        assertEquals(StateManagerConstants.INITIAL_STATE_ID, stateManager.getWorkingStateId());
        stateManager.setWorkingState("ClonedState1");
        assertEquals("ClonedState1", stateManager.getWorkingStateId());
        // "middle" state removing test
        stateManager.removeState("ClonedState1");
        try {
            assertEquals(StateManagerConstants.INITIAL_STATE_ID, stateManager.getWorkingStateId()); // because state is not set
            fail();
        } catch (Exception ignored) {
        }
        assertEquals(3, stateManager.getVariantArraySize());
        assertEquals(Sets.newHashSet(StateManagerConstants.INITIAL_STATE_ID, "ClonedState2"), stateManager.getStateIds());
        assertEquals(Sets.newHashSet(0, 2), stateManager.getVariantIndexes());
        assertEquals(Collections.singleton(1), identifiable1.deleted);
        // state array index recycling test
        stateManager.cloneState("ClonedState2", "ClonedState3");
        assertEquals(3, stateManager.getVariantArraySize());
        assertEquals(Sets.newHashSet(StateManagerConstants.INITIAL_STATE_ID, "ClonedState2", "ClonedState3"), stateManager.getStateIds());
        assertEquals(Sets.newHashSet(0, 1, 2), stateManager.getVariantIndexes());
        // state array reduction test
        stateManager.removeState("ClonedState3");
        assertEquals(3, stateManager.getVariantArraySize());
        stateManager.removeState("ClonedState2");
        assertEquals(1, stateManager.getVariantArraySize());
        assertEquals(Collections.singleton(StateManagerConstants.INITIAL_STATE_ID), stateManager.getStateIds());
        assertEquals(Collections.singleton(0), stateManager.getVariantIndexes());
        assertEquals(2, identifiable1.reducedCount);
    }
}
