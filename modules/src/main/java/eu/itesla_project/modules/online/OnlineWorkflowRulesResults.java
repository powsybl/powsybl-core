/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.online;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import eu.itesla_project.modules.securityindexes.SecurityIndexType;

/**
 * The results of the security rules application during a run of the online workflow
 *
 *
 * @author Quinary <itesla@quinary.com>
 */
public interface OnlineWorkflowRulesResults {

    /**
     * Get the id of the workflow where the rules have been applied
     * @return the id of the workflow
     */
    String getWorkflowId();

    /**
     * Get the time horizon used for the workflow where the rules have been applied
     * @return the time horizon of the workflow
     */
    TimeHorizon getTimeHorizon();

    /**
     * Get the contingencies analyzed by the workflow with security rules
     * @return the collection of ids of the contingencies analyzed with security rules
     */
    Collection<String> getContingenciesWithSecurityRulesResults();

    /**
     * Get the states analyzed by the workflow with security rules for a specific contingency
     * @param contingencyId  the id of the contingency
     * @return the list of ids of the states analyzed with security rules
     */
    List<Integer> getStatesWithSecurityRulesResults(String contingencyId);

    /**
     * Get the status (SAFE, SAFE_WITH_CORRECTIVE_ACTIONS, UNSAFE) of a state for a specific contingency 
     * @param contingencyId  the id of the contingency
     * @param stateId  the id of the state
     * @return the status (SAFE, SAFE_WITH_CORRECTIVE_ACTIONS, UNSAFE) of the state
     */
    StateStatus getStateStatus(String contingencyId, Integer stateId);

    /**
     * Get the results of the application of the security rules to a state for a contingency
     * @param contingencyId  the id of the contingency
     * @param stateId  the id of the state
     * @return the map of [index, security flag] pair, output of the application of the security rules  on a state for a contingency 
     */
    Map<String,Boolean> getStateResults(String contingencyId, Integer stateId);

    /**
     * Return if there are available rules for a state and a contingency
     * @param contingencyId  the id of the contingency
     * @param stateId  the id of the state
     * @return true if there are available rules for a state and a contingency, false otherwise
     */
    boolean areValidRulesAvailable(String contingencyId, Integer stateId);

    /**
     * Get the list of invalid rules types (phenomena), for a state and a contingency
     * @param contingencyId  the id of the contingency
     * @param stateId  the id of the state
     * @return the list of invalid rules types (phenomena), for a state and a contingency
     */
    List<SecurityIndexType> getInvalidRules(String contingencyId, Integer stateId);

}
