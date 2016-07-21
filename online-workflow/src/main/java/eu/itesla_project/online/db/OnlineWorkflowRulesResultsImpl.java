/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.itesla_project.modules.online.OnlineWorkflowRulesResults;
import eu.itesla_project.modules.online.StateStatus;
import eu.itesla_project.modules.online.TimeHorizon;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
class OnlineWorkflowRulesResultsImpl implements OnlineWorkflowRulesResults {

    // the id of the workflow where the rules have been applied
    private final String workflowId;
    // the time horizon used for the workflow where the rules have been applied
    private final TimeHorizon timeHorizon;
    // <contingency, list of <stateId, security rule result>>
    private Map<String, Map<Integer,Map<String, Boolean>>> contingenciesWithRulesResults = new HashMap<String, Map<Integer,Map<String,Boolean>>>();
    // <contingency, list of <stateId, state status>
    private Map<String, Map<Integer,StateStatus>> contingenciesWithRulesStatus = new HashMap<String, Map<Integer,StateStatus>>();
    // contingencies, map of <stateId, available rules flag> 
    private Map<String, Map<Integer, Boolean>> contingenciesWithRules = new HashMap<String, Map<Integer,Boolean>>();
    // contingencies, map of <stateId, list of invalid rules>
    private Map<String, Map<Integer, List<SecurityIndexType>>> contingenciesWithInvalidRules = new HashMap<String, Map<Integer,List<SecurityIndexType>>>();


    OnlineWorkflowRulesResultsImpl(String workflowId, TimeHorizon timeHorizon) {
        this.workflowId = workflowId;
        this.timeHorizon = timeHorizon;
    }

    @Override
    public String getWorkflowId() {
        return workflowId;
    }

    @Override
    public TimeHorizon getTimeHorizon() {
        return timeHorizon;
    }

    @Override
    public Collection<String> getContingenciesWithSecurityRulesResults() {
        return contingenciesWithRulesResults.keySet();
    }

    @Override
    public List<Integer> getStatesWithSecurityRulesResults(String contingencyId) {
        List<Integer> statesWithRulesResults = new ArrayList<Integer>();
        statesWithRulesResults.addAll(contingenciesWithRulesResults.get(contingencyId).keySet());
        return statesWithRulesResults;
    }

    @Override
    public StateStatus getStateStatus(String contingencyId, Integer stateId) {
        return contingenciesWithRulesStatus.get(contingencyId).get(stateId);
    }

    @Override
    public Map<String, Boolean> getStateResults(String contingencyId, Integer stateId) {
        return contingenciesWithRulesResults.get(contingencyId).get(stateId);
    }

    void addContingencyWithSecurityRulesResults(String contingencyId, Integer stateId, StateStatus stateStatus, Map<String, Boolean> stateResults,
            boolean rulesAvailable, List<SecurityIndexType> invalidRules) {
        // save state results
        Map<Integer,Map<String, Boolean>> statesWithRulesResults = new HashMap<Integer, Map<String, Boolean>>();
        if ( contingenciesWithRulesResults.containsKey(contingencyId) ) {
            statesWithRulesResults = contingenciesWithRulesResults.get(contingencyId);
        }
        statesWithRulesResults.put(stateId, stateResults);
        contingenciesWithRulesResults.put(contingencyId, statesWithRulesResults);

        // save state status
        Map<Integer,StateStatus> statesWithRulesStatus = new HashMap<Integer, StateStatus>();
        if ( contingenciesWithRulesStatus.containsKey(contingencyId) ) {
            statesWithRulesStatus = contingenciesWithRulesStatus.get(contingencyId);
        }
        statesWithRulesStatus.put(stateId, stateStatus);
        contingenciesWithRulesStatus.put(contingencyId, statesWithRulesStatus);

        Map<Integer, Boolean> statesWithRules = new HashMap<Integer, Boolean>();
        if ( contingenciesWithRules.containsKey(contingencyId) ) {
            statesWithRules = contingenciesWithRules.get(contingencyId);
        }
        statesWithRules.put(stateId, rulesAvailable);
        contingenciesWithRules.put(contingencyId, statesWithRules);

        Map<Integer, List<SecurityIndexType>> statesWithInvalidRules = new HashMap<Integer, List<SecurityIndexType>>();
        if ( contingenciesWithInvalidRules.containsKey(contingencyId) ) {
            statesWithInvalidRules = contingenciesWithInvalidRules.get(contingencyId);
        }
        statesWithInvalidRules.put(stateId, invalidRules);
        contingenciesWithInvalidRules.put(contingencyId, statesWithInvalidRules);
    }

    @Override
    public boolean areValidRulesAvailable(String contingencyId, Integer stateId) {
        boolean rulesAvailable = false;
        if ( contingenciesWithRules.containsKey(contingencyId) && contingenciesWithRules.get(contingencyId).containsKey(stateId) )
            rulesAvailable = contingenciesWithRules.get(contingencyId).get(stateId);
        return rulesAvailable;
    }

    @Override
    public List<SecurityIndexType> getInvalidRules(String contingencyId, Integer stateId) {
        List<SecurityIndexType> invalidRules = new ArrayList<SecurityIndexType>();
        if ( contingenciesWithInvalidRules.containsKey(contingencyId) && contingenciesWithInvalidRules.get(contingencyId).containsKey(stateId) )
            invalidRules = contingenciesWithInvalidRules.get(contingencyId).get(stateId);
        return invalidRules;
    }

}
