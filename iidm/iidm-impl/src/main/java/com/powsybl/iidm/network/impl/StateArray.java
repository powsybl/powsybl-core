/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.impl.util.Ref;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * To easily manage an array of state.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class StateArray<S extends State> {

    private final Ref<? extends MultiStateObject> multiStateObjRef;

    private final List<S> states;

    StateArray(Ref<? extends MultiStateObject> multiStateObjRef, StateFactory<S> stateFactory) {
        this.multiStateObjRef = multiStateObjRef;
        StateManagerImpl stateManager = multiStateObjRef.get().getStateManager();
        states = Collections.synchronizedList(new ArrayList<S>(stateManager.getStateArraySize()));
        for (int i = 0; i < stateManager.getStateArraySize(); i++) {
            states.add(null);
        }
        for (int i : stateManager.getStateIndexes()) {
            states.set(i, stateFactory.newState());
        }
    }

    S get() {
        return states.get(multiStateObjRef.get().getStateManager().getStateContext().getStateIndex());
    }

    void push(int number, StateFactory<S> stateFactory) {
        for (int i = 0; i < number; i++) {
            states.add(stateFactory.newState());
        }
    }

    void push(StateFactory<S> stateFactory) {
        states.add(stateFactory.newState());
    }

    void pop(int number) {
        for (int i = 0; i < number; i++) {
            states.remove(states.size() - 1);
        }
    }

    void delete(int index) {
        states.set(index, null);
    }

    void allocate(int[] indexes, StateFactory<S> stateFactory) {
        for (int index : indexes) {
            states.set(index, stateFactory.newState());
        }
    }

    S copy(int index) {
        return states.get(index).copy();
    }

}
