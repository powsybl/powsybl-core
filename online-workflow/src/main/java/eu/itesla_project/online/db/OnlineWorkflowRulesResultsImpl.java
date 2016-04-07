/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
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
	
	void addContingencyWithSecurityRulesResults(String contingencyId, Integer stateId, StateStatus stateStatus, 
													   Map<String, Boolean> stateResults) {
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
	}

}
