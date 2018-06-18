/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBiMap;
import com.google.common.primitives.Ints;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.StateManager;
import com.powsybl.iidm.network.StateManagerConstants;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class StateManagerImpl implements StateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateManagerImpl.class);

    private static final int INITIAL_STATE_INDEX = 0;

    private StateContext stateContext;

    private final ObjectStore objectStore;

    private final BiMap<String, Integer> id2index = HashBiMap.create();

    private int stateArraySize;

    private final Deque<Integer> unusedIndexes = new ArrayDeque<>();

    private final ReentrantLock stateLock = new ReentrantLock();

    StateManagerImpl(ObjectStore objectStore) {
        this.stateContext = new MultiStateContext(INITIAL_STATE_INDEX);
        this.objectStore = objectStore;
        // the network has always a zero index initial state
        id2index.put(StateManagerConstants.INITIAL_STATE_ID, INITIAL_STATE_INDEX);
        stateArraySize = INITIAL_STATE_INDEX + 1;
    }

    StateContext getStateContext() {
        return stateContext;
    }

    @Override
    public Collection<String> getStateIds() {
        stateLock.lock();
        try {
            return Collections.unmodifiableSet(id2index.keySet());
        } finally {
            stateLock.unlock();
        }
    }

    int getStateArraySize() {
        return stateArraySize;
    }

    int getStateCount() {
        return id2index.size();
    }

    Collection<Integer> getStateIndexes() {
        return id2index.values();
    }

    private int getStateIndex(String stateId) {
        Integer index = id2index.get(stateId);
        if (index == null) {
            throw new PowsyblException("State '" + stateId + "' not found");
        }
        return index;
    }

    @Override
    public String getWorkingStateId() {
        stateLock.lock();
        try {
            int index = stateContext.getStateIndex();
            return id2index.inverse().get(index);
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public void setWorkingState(String stateId) {
        stateLock.lock();
        try {
            int index = getStateIndex(stateId);
            stateContext.setStateIndex(index);
        } finally {
            stateLock.unlock();
        }
    }

    private Iterable<Stateful> getStafulObjects() {
        return FluentIterable.from(objectStore.getAll()).filter(Stateful.class);
    }

    @Override
    public void cloneState(String sourceStateId, String targetStateId) {
        cloneState(sourceStateId, Arrays.asList(targetStateId));
    }

    @Override
    public void cloneState(String sourceStateId, List<String> targetStateIds) {
        if (targetStateIds.isEmpty()) {
            throw new IllegalArgumentException("Empty target state id list");
        }
        LOGGER.debug("Creating states {}", targetStateIds);
        stateLock.lock();
        try {
            int sourceIndex = getStateIndex(sourceStateId);
            int initStateArraySize = stateArraySize;
            int extendedCount = 0;
            List<Integer> recycled = new ArrayList<>();
            for (String targetStateId : targetStateIds) {
                if (id2index.containsKey(targetStateId)) {
                    throw new PowsyblException("Target state '" + targetStateId + "' already exists");
                }
                if (unusedIndexes.isEmpty()) {
                    // extend state array size
                    id2index.put(targetStateId, stateArraySize);
                    stateArraySize++;
                    extendedCount++;
                } else {
                    // recycle an index
                    int index = unusedIndexes.pollLast();
                    id2index.put(targetStateId, index);
                    recycled.add(index);
                }
            }
            if (!recycled.isEmpty()) {
                int[] indexes = Ints.toArray(recycled);
                for (Stateful obj : getStafulObjects()) {
                    obj.allocateStateArrayElement(indexes, sourceIndex);
                }
                LOGGER.trace("Recycling state array indexes {}", Arrays.toString(indexes));
            }
            if (extendedCount > 0) {
                for (Stateful obj : getStafulObjects()) {
                    obj.extendStateArraySize(initStateArraySize, extendedCount, sourceIndex);
                }
                LOGGER.trace("Extending state array size to {} (+{})", stateArraySize, extendedCount);
            }
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public void removeState(String stateId) {
        stateLock.lock();
        try {
            if (StateManagerConstants.INITIAL_STATE_ID.equals(stateId)) {
                throw new PowsyblException("Removing initial state is forbidden");
            }
            int index = getStateIndex(stateId);
            id2index.remove(stateId);
            LOGGER.debug("Removing state '{}'", stateId);
            if (index == stateArraySize - 1) {
                // remove consecutive unsused index starting from the end
                int number = 0; // number of elements to remove
                for (int j = index; j >= 0; j--) {
                    if (id2index.containsValue(j)) {
                        break;
                    } else {
                        number++;
                        unusedIndexes.remove(j);
                    }
                }
                // reduce state array size
                for (Stateful obj : getStafulObjects()) {
                    obj.reduceStateArraySize(number);
                }
                stateArraySize -= number;
                LOGGER.trace("Reducing state array size to {}", stateArraySize);
            } else {
                unusedIndexes.add(index);
                // delete state array element at the unused index to avoid memory leak
                // (so that state data can be garbage collected)
                for (Stateful obj : getStafulObjects()) {
                    obj.deleteStateArrayElement(index);
                }
                LOGGER.trace("Deleting state array element at index {}", index);
            }
            // if the removed state is the working state, unset the working state
            stateContext.resetIfStateIndexIs(index);
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public void allowStateMultiThreadAccess(boolean allow) {
        stateLock.lock();
        try {
            if (allow) {
                int index = stateContext.getStateIndex();
                stateContext = ThreadLocalMultiStateContext.INSTANCE;
                stateContext.setStateIndex(index);
            } else {
                stateContext = new MultiStateContext(stateContext.getStateIndex());
            }
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public boolean isStateMultiThreadAccessAllowed() {
        stateLock.lock();
        try {
            return stateContext instanceof ThreadLocalMultiStateContext;
        } finally {
            stateLock.unlock();
        }
    }

    void forEachState(Runnable r) {
        stateLock.lock();
        int currentStateIndex = stateContext.getStateIndex();
        try {
            for (int index : id2index.values()) {
                stateContext.setStateIndex(index);
                r.run();
            }
        } finally {
            stateContext.setStateIndex(currentStateIndex);
            stateLock.unlock();
        }
    }

}
