/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.List;

/**
 * This class provides methods to manage states of the network (create and
 * remove a state, set the working state, etc).
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface StateManager {

    /**
     * Get the state id list.
     *
     * @return the state id list
     */
    Collection<String> getStateIds();

    /**
     * Get the working state.
     *
     * @return the id of the working state
     */
    String getWorkingStateId();

    /**
     * Set the working state.
     *
     * @param stateId the id of the working state
     * @throws com.powsybl.commons.PowsyblException if the state is not found
     */
    void setWorkingState(String stateId);

    /**
     * Create a new state by cloning an existing one.
     *
     * @param sourceStateId the source state id
     * @param targetStateIds the target state id list (the ones that will be created)
     * @throws com.powsybl.commons.PowsyblException
     *                         if the source state is not found or if a state with
     *                         an id of targetStateIds already exists
     */
    void cloneState(String sourceStateId, List<String> targetStateIds);

    /**
     * Create a new state by cloning an existing one.
     *
     * @param sourceStateId the source state id
     * @param targetStateId the target state id (the one that will be created)
     * @throws com.powsybl.commons.PowsyblException
     *                         if the source state is not found or if a state with
     *                         the id targetStateId already exists
     */
    void cloneState(String sourceStateId, String targetStateId);

    /**
     * Remove a state.
     *
     * @param stateId the id of the state to remove
     */
    void removeState(String stateId);

    /**
     * Allows states to be accessed simulaneously by different threads. When
     * this options is activated, the working state can have a different value
     * for each thread.
     * @param allow
     */
    void allowStateMultiThreadAccess(boolean allow);

    boolean isStateMultiThreadAccessAllowed();

}
