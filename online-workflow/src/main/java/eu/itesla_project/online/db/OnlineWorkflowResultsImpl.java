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

import eu.itesla_project.modules.contingencies.ActionParameters;
import eu.itesla_project.modules.online.OnlineWorkflowResults;
import eu.itesla_project.modules.online.TimeHorizon;
import eu.itesla_project.modules.optimizer.CCOFinalStatus;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
class OnlineWorkflowResultsImpl implements OnlineWorkflowResults {
	
	// the id of tne workflow that generated the results
	private final String workflowId;
	// the time horizon used for the workflow
	private final TimeHorizon timeHorizon;
	// <contingency, list of <stateId, action plans>
	private Map<String, Map<Integer,StateWithCCOInfo>> contingenciesWithActionsInfo = new HashMap<String, Map<Integer,StateWithCCOInfo>>();
	// <contingency, list of <stateId, <actions, <equipment, parameters>>>>
	private Map<String, Map<Integer, Map<String, Map<String, ActionParameters>>>> contingenciesWithActions = new HashMap<String, Map<Integer, Map<String, Map<String, ActionParameters>>>>();
	// <contingency, list of <stateId, indexes>>
	private Map<String, Map<Integer,Map<String, Boolean>>> unsafeContingencies = new HashMap<String,  Map<Integer,Map<String, Boolean>>>();

	OnlineWorkflowResultsImpl(String workflowId, TimeHorizon timeHorizon) {
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
	public Collection<String> getContingenciesWithActions() {
		return contingenciesWithActionsInfo.keySet();
	}

	@Override
	public Collection<String> getUnsafeContingencies() {
		return unsafeContingencies.keySet();
	}

	@Override
	public Map<Integer, Boolean> getUnsafeStatesWithActions(String contingencyId) {
		Map<Integer,Boolean> unsafeStatesWithActions = new HashMap<Integer, Boolean>();
		if( contingenciesWithActionsInfo.containsKey(contingencyId) ) {
			for (Integer stateId : contingenciesWithActionsInfo.get(contingencyId).keySet()) {
				unsafeStatesWithActions.put(stateId, contingenciesWithActionsInfo.get(contingencyId).get(stateId).isActionFound());
			}
		}
		return unsafeStatesWithActions;
	}

	@Override
	public List<Integer> getUnstableStates(String contingencyId) {
		List<Integer> unstableStates = new ArrayList<Integer>();
		if( unsafeContingencies.containsKey(contingencyId) )
			unstableStates.addAll(unsafeContingencies.get(contingencyId).keySet());
		return unstableStates;
	}
	
	@Override
	public CCOFinalStatus getStateStatus(String contingencyId, Integer stateId) {
		if( contingenciesWithActionsInfo.containsKey(contingencyId) )
			return contingenciesWithActionsInfo.get(contingencyId).get(stateId).getStatus();
		return null;
	}

	@Override
	public String getCause(String contingencyId, Integer stateId) {
		if( contingenciesWithActionsInfo.containsKey(contingencyId) )
			return contingenciesWithActionsInfo.get(contingencyId).get(stateId).getCause();
		return null;
	}
	
	@Override
	public String getActionPlan(String contingencyId, Integer stateId) {
		if( contingenciesWithActionsInfo.containsKey(contingencyId) )
			return contingenciesWithActionsInfo.get(contingencyId).get(stateId).getActionPlan();
		return null;
	}

	@Override
	public List<String> getActionsIds(String contingencyId, Integer stateId) {
		if( contingenciesWithActions.containsKey(contingencyId) 
			&& contingenciesWithActions.get(contingencyId).containsKey(stateId) )
			return new ArrayList<String>(contingenciesWithActions.get(contingencyId).get(stateId).keySet());
		return null;
	}

	@Override
	public Map<String, Boolean> getIndexesData(String contingencyId, Integer stateId) {
		if( unsafeContingencies.containsKey(contingencyId) )
			return unsafeContingencies.get(contingencyId).get(stateId);
		return null;
	}

	@Override
	public List<String> getEquipmentsIds(String contingencyId, Integer stateId, String actionId) {
		if( contingenciesWithActions.containsKey(contingencyId) 
			&& contingenciesWithActions.get(contingencyId).containsKey(stateId)
			&& contingenciesWithActions.get(contingencyId).get(stateId).containsKey(actionId) )
			return new ArrayList<String>(contingenciesWithActions.get(contingencyId).get(stateId).get(actionId).keySet());
		return null;
	}

	@Override
	public Map<String, ActionParameters> getEquipmentsWithParameters(String contingencyId, Integer stateId, String actionId) {
		if( contingenciesWithActions.containsKey(contingencyId) 
			&& contingenciesWithActions.get(contingencyId).containsKey(stateId) )
			return contingenciesWithActions.get(contingencyId).get(stateId).get(actionId);
		return null;
	}

	@Override
	public ActionParameters getParameters(String contingencyId, Integer stateId, String actionId, String equipmentId) {
		if( contingenciesWithActions.containsKey(contingencyId) 
			&& contingenciesWithActions.get(contingencyId).containsKey(stateId) )
			return contingenciesWithActions.get(contingencyId).get(stateId).get(actionId).get(equipmentId);
		return null;
	}

	void addContingenciesWithActions(String contingencyId, Integer stateId, boolean actionsFound, CCOFinalStatus status, String cause, 
									 String actionPlan, Map<String, Map<String, ActionParameters>> actions) {
		Map<Integer,StateWithCCOInfo> statesWithActionPlans = new HashMap<Integer, StateWithCCOInfo>();
		if ( contingenciesWithActionsInfo.containsKey(contingencyId) )
			statesWithActionPlans = contingenciesWithActionsInfo.get(contingencyId);
		statesWithActionPlans.put(stateId, new StateWithCCOInfo(actionsFound, status, cause, actionPlan));
		contingenciesWithActionsInfo.put(contingencyId, statesWithActionPlans);

		if ( actions != null ) {
			Map<Integer, Map<String, Map<String, ActionParameters>>> statesWithActions = new HashMap<Integer, Map<String, Map<String, ActionParameters>>>();
			if ( contingenciesWithActions.containsKey(contingencyId) ) {
				statesWithActions = contingenciesWithActions.get(contingencyId);
			}
			statesWithActions.put(stateId, actions);
			contingenciesWithActions.put(contingencyId, statesWithActions);
		}
	}
	
	void addUnsafeContingencies(String contingencyId, Integer stateId, Map<String, Boolean> indexesData) {
		Map<Integer,Map<String, Boolean>> unstableStates = new HashMap<Integer, Map<String, Boolean>>();
		if ( unsafeContingencies.containsKey(contingencyId) ) {
			unstableStates = unsafeContingencies.get(contingencyId);
		}
		unstableStates.put(stateId, indexesData);
		unsafeContingencies.put(contingencyId, unstableStates);
	}
	
	class StateWithCCOInfo {
		boolean actionFound;
		CCOFinalStatus status;
		String cause;
		String actionPlan;
		
		StateWithCCOInfo(boolean actionFound, CCOFinalStatus status, String cause, String actionPlan) {
			this.actionFound = actionFound;
			this.status = status;
			this.cause = cause;
			this.actionPlan = actionPlan;
		}
		
		public boolean isActionFound() {
			return actionFound;
		}

		CCOFinalStatus getStatus() {
			return status;
		}

		String getCause() {
			return cause;
		}
		
		String getActionPlan() {
			return actionPlan;
		}
	}

}
