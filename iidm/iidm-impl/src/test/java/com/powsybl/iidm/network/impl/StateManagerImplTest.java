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
import com.powsybl.iidm.network.StateManager;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;

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
        assertTrue(stateManager.getStateArraySize() == 1);
        assertTrue(Sets.newHashSet(StateManager.INITIAL_STATE_ID).equals(stateManager.getStateIds()));
        assertTrue(Sets.newHashSet(0).equals(stateManager.getStateIndexes()));
        try {
            stateManager.setWorkingState("UnknownState");
            assertFalse(true);
        } catch (PowsyblException ignored) {
        }
        try {
            stateManager.removeState("UnknownState");
            assertFalse(true);
        } catch (PowsyblException ignored) {
        }
        try {
            stateManager.removeState(StateManager.INITIAL_STATE_ID);
            assertFalse(true);
        } catch (PowsyblException ignored) {
        }
        // cloning test
        stateManager.cloneState(StateManager.INITIAL_STATE_ID, "ClonedState1");
        assertTrue(stateManager.getStateArraySize() == 2);
        assertTrue(Sets.newHashSet(StateManager.INITIAL_STATE_ID, "ClonedState1").equals(stateManager.getStateIds()));
        assertTrue(Sets.newHashSet(0, 1).equals(stateManager.getStateIndexes()));
        assertTrue(Sets.newHashSet(0).equals(identifiable1.extended));
        // second cloning test
        stateManager.cloneState("ClonedState1", "ClonedState2");
        assertTrue(stateManager.getStateArraySize() == 3);
        assertTrue(Sets.newHashSet(StateManager.INITIAL_STATE_ID, "ClonedState1", "ClonedState2").equals(stateManager.getStateIds()));
        assertTrue(Sets.newHashSet(0, 1, 2).equals(stateManager.getStateIndexes()));
        assertTrue(stateManager.getWorkingStateId().equals(StateManager.INITIAL_STATE_ID));
        stateManager.setWorkingState("ClonedState1");
        assertTrue(stateManager.getWorkingStateId().equals("ClonedState1"));
        // "middle" state removing test
        stateManager.removeState("ClonedState1");
        try {
            assertTrue(stateManager.getWorkingStateId().equals(StateManager.INITIAL_STATE_ID)); // because state is not set
            fail();
        } catch (Exception ignored) {
        }
        assertTrue(stateManager.getStateArraySize() == 3);
        assertTrue(Sets.newHashSet(StateManager.INITIAL_STATE_ID, "ClonedState2").equals(stateManager.getStateIds()));
        assertTrue(Sets.newHashSet(0, 2).equals(stateManager.getStateIndexes()));
        assertTrue(Sets.newHashSet(1).equals(identifiable1.deleted));
        // state array index recycling test
        stateManager.cloneState("ClonedState2", "ClonedState3");
        assertTrue(stateManager.getStateArraySize() == 3);
        assertTrue(Sets.newHashSet(StateManager.INITIAL_STATE_ID, "ClonedState2", "ClonedState3").equals(stateManager.getStateIds()));
        assertTrue(Sets.newHashSet(0, 1, 2).equals(stateManager.getStateIndexes()));
        // state array reduction test
        stateManager.removeState("ClonedState3");
        assertTrue(stateManager.getStateArraySize() == 3);
        stateManager.removeState("ClonedState2");
        assertTrue(stateManager.getStateArraySize() == 1);
        assertTrue(Sets.newHashSet(StateManager.INITIAL_STATE_ID).equals(stateManager.getStateIds()));
        assertTrue(Sets.newHashSet(0).equals(stateManager.getStateIndexes()));
        assertTrue(identifiable1.reducedCount == 2);
    }
}
