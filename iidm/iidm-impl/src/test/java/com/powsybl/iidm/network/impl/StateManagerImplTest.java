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
public class StateManagerImplTest {

    private static final class IdentifiableMock extends AbstractExtendable<IdentifiableMock> implements Identifiable<IdentifiableMock>, Stateful {

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
        public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
            for (int i = 0; i < number; i++) {
                extended.add(sourceIndex);
            }
        }

        @Override
        public void reduceStateArraySize(int number) {
            reducedCount += number;
        }

        @Override
        public void deleteStateArrayElement(int index) {
            deleted.add(index);
        }

        @Override
        public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        }
    }

    public StateManagerImplTest() {
    }

    @Test
    public void test() {
        ObjectStore objectStore = new ObjectStore();
        IdentifiableMock identifiable1 = new IdentifiableMock("1");
        objectStore.checkAndAdd(identifiable1);
        StateManagerImpl stateManager = new StateManagerImpl(objectStore);
        // initial state test
        assertEquals(1, stateManager.getStateArraySize());
        assertEquals(Collections.singleton(StateManagerConstants.INITIAL_STATE_ID), stateManager.getStateIds());
        assertEquals(Collections.singleton(0), stateManager.getStateIndexes());
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
        assertEquals(2, stateManager.getStateArraySize());
        assertEquals(Sets.newHashSet(StateManagerConstants.INITIAL_STATE_ID, "ClonedState1"), stateManager.getStateIds());
        assertEquals(Sets.newHashSet(0, 1), stateManager.getStateIndexes());
        assertEquals(Collections.singleton(0), identifiable1.extended);
        // second cloning test
        stateManager.cloneState("ClonedState1", "ClonedState2");
        assertEquals(3, stateManager.getStateArraySize());
        assertEquals(Sets.newHashSet(StateManagerConstants.INITIAL_STATE_ID, "ClonedState1", "ClonedState2"), stateManager.getStateIds());
        assertEquals(Sets.newHashSet(0, 1, 2), stateManager.getStateIndexes());
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
        assertEquals(3, stateManager.getStateArraySize());
        assertEquals(Sets.newHashSet(StateManagerConstants.INITIAL_STATE_ID, "ClonedState2"), stateManager.getStateIds());
        assertEquals(Sets.newHashSet(0, 2), stateManager.getStateIndexes());
        assertEquals(Collections.singleton(1), identifiable1.deleted);
        // state array index recycling test
        stateManager.cloneState("ClonedState2", "ClonedState3");
        assertEquals(3, stateManager.getStateArraySize());
        assertEquals(Sets.newHashSet(StateManagerConstants.INITIAL_STATE_ID, "ClonedState2", "ClonedState3"), stateManager.getStateIds());
        assertEquals(Sets.newHashSet(0, 1, 2), stateManager.getStateIndexes());
        // state array reduction test
        stateManager.removeState("ClonedState3");
        assertEquals(3, stateManager.getStateArraySize());
        stateManager.removeState("ClonedState2");
        assertEquals(1, stateManager.getStateArraySize());
        assertEquals(Collections.singleton(StateManagerConstants.INITIAL_STATE_ID), stateManager.getStateIds());
        assertEquals(Collections.singleton(0), stateManager.getStateIndexes());
        assertEquals(2, identifiable1.reducedCount);
    }
}
