/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.itesla_project.modules.online.OnlineWorkflowRulesResults;
import eu.itesla_project.modules.online.StateStatus;
import eu.itesla_project.modules.online.TimeHorizon;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class SecurityRulesApplicationResults implements OnlineWorkflowRulesResults {

    // the id of the workflow where the rules have been applied
    private final String workflowId;
    // the time horizon used for the workflow where the rules have been applied
    private final TimeHorizon timeHorizon;

    // <contingency, list of <stateId, security rules results>>
    private Map<String, StatesWithSecurityRulesResults> contingenciesWithSecurityRulesResults = new HashMap<String, StatesWithSecurityRulesResults>();

    public SecurityRulesApplicationResults(String workflowId, TimeHorizon timeHorizon) {
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


    public void addStateWithSecurityRulesResults(String contingencyId, Integer stateId, StateStatus stateStatus, Map<SecurityIndexType, StateStatus> securityRulesResults,
            boolean rulesAvailable, List<SecurityIndexType> invalidRules) {
        StatesWithSecurityRulesResults statesWithSecurityRulesResults = new StatesWithSecurityRulesResults();
        if ( contingenciesWithSecurityRulesResults.containsKey(contingencyId) ) {
            statesWithSecurityRulesResults = contingenciesWithSecurityRulesResults.get(contingencyId);
        }
        statesWithSecurityRulesResults.addState(stateId, stateStatus, securityRulesResults, rulesAvailable, invalidRules);
        contingenciesWithSecurityRulesResults.put(contingencyId, statesWithSecurityRulesResults);
    }

    @Override
    public Set<String> getContingenciesWithSecurityRulesResults() {
        return contingenciesWithSecurityRulesResults.keySet();
    }

    public List<Integer> getStatesWithSecurityRulesResults(String contingencyId) {
        List<Integer> statesWithSecurityRulesResults = new ArrayList<Integer>();
        for(Integer stateId : contingenciesWithSecurityRulesResults.get(contingencyId).getStates()) {
            statesWithSecurityRulesResults.add(stateId);
        }
        return statesWithSecurityRulesResults;
    }

    public Map<SecurityIndexType, StateStatus> getSecurityRulesResults(String contingencyId, Integer stateId) {
        return contingenciesWithSecurityRulesResults.get(contingencyId).getSecurityRulesResults(stateId);
    }

    @Override
    public Map<String, Boolean> getStateResults(String contingencyId, Integer stateId) {
        Map<SecurityIndexType, StateStatus> securityRulesResults = getSecurityRulesResults(contingencyId, stateId);
        Map<String, Boolean> stateResults = new HashMap<String,Boolean>();
        for(SecurityIndexType index : securityRulesResults.keySet()) {
            switch (securityRulesResults.get(index)) {
            case SAFE:
                stateResults.put(index.getLabel(), true);
                break;
            case UNSAFE:
                stateResults.put(index.getLabel(), false);
                break;
            default:
                break;
            }
        }
        return stateResults;
    }

    @Override
    public StateStatus getStateStatus(String contingencyId, Integer stateId) {
        return contingenciesWithSecurityRulesResults.get(contingencyId).getStateStatus(stateId);
    }

    private StatesWithSecurityRulesResults getStatesWithSecurityRulesResults_int(String contingencyId) {
        return contingenciesWithSecurityRulesResults.get(contingencyId);
    }

    @Override
    public boolean areValidRulesAvailable(String contingencyId, Integer stateId) {
        boolean rulesAvailable = false;
        if ( contingenciesWithSecurityRulesResults.containsKey(contingencyId) )
            rulesAvailable = contingenciesWithSecurityRulesResults.get(contingencyId).getRulesAvailability(stateId);
        return rulesAvailable;
    }

    @Override
    public List<SecurityIndexType> getInvalidRules(String contingencyId, Integer stateId) {
        List<SecurityIndexType> invalidRules = new ArrayList<SecurityIndexType>();
        if ( contingenciesWithSecurityRulesResults.containsKey(contingencyId) )
            invalidRules = contingenciesWithSecurityRulesResults.get(contingencyId).getInvalidRules(stateId);
        return invalidRules;
    }

    public String toString() {
        String output = "time horizon: "+ timeHorizon.getName();
        output += "\n" + "contingencies with security rules results: " + getContingenciesWithSecurityRulesResults();
        for(String contingencyId : getContingenciesWithSecurityRulesResults() )
            output += "\n[contingecy id = " + contingencyId + ", states = " + getStatesWithSecurityRulesResults_int(contingencyId) + "]"; 
        return output;
    }

    private class StatesWithSecurityRulesResults {

        // <stateId, security rules results>
        Map<Integer, Map<SecurityIndexType, StateStatus>> states = new HashMap<Integer, Map<SecurityIndexType, StateStatus>>();
        // <stateId, state status>
        Map<Integer, StateStatus> statesStatus = new HashMap<Integer, StateStatus>();
        // <stateId, available rules flag>
        Map<Integer, Boolean> statesWithRules = new HashMap<Integer, Boolean>();
        // <stateId, list of invalid rules>
        Map<Integer, List<SecurityIndexType>> statesWithInvalidRules = new HashMap<Integer, List<SecurityIndexType>>();

        boolean addState(Integer stateId, StateStatus stateStatus, Map<SecurityIndexType, StateStatus> securityRulesResults,
                Boolean rulesAvailable, List<SecurityIndexType> invalidRules) {
            boolean added = false;
            if ( !states.containsKey(stateId) && !statesStatus.containsKey(stateId) ) {
                states.put(stateId, securityRulesResults);
                statesStatus.put(stateId, stateStatus);
                statesWithRules.put(stateId, rulesAvailable);
                statesWithInvalidRules.put(stateId, invalidRules);
                added = true;
            }
            return added;
        }

        Set<Integer> getStates() {
            return states.keySet();
        }

        Map<SecurityIndexType, StateStatus> getSecurityRulesResults(Integer stateId) {
            return states.get(stateId);
        }

        StateStatus getStateStatus(Integer stateId) {
            return statesStatus.get(stateId);
        }

        boolean getRulesAvailability(Integer stateId) {
            return statesWithRules.get(stateId);
        }

        List<SecurityIndexType> getInvalidRules(Integer stateId) {
            return statesWithInvalidRules.get(stateId);
        }

        public String toString() {
            String output = "";
            for(Integer stateId : getStates()) {
                output += "[stateId " + stateId + ", " + getStateStatus(stateId) + ", rules = " + securityRulesResultsToString(getSecurityRulesResults(stateId)) + "]";
            }
            return output;
        }

        private String securityRulesResultsToString(Map<SecurityIndexType, StateStatus> securityRulesResults) {
            String results = "[";
            for (SecurityIndexType securityIndexType : securityRulesResults.keySet()) {
                results += "[" + securityIndexType.getLabel().replaceAll(" ", "_") + " " + securityRulesResults.get(securityIndexType) + "]";
            }
            results += " ]";
            return results;
        }
    }

}
