/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.itesla_project.modules.contingencies.ActionParameters;
import eu.itesla_project.modules.online.OnlineWorkflowResults;
import eu.itesla_project.modules.online.TimeHorizon;
import eu.itesla_project.modules.optimizer.CCOFinalStatus;
import eu.itesla_project.simulation.securityindexes.SecurityIndex;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ForecastAnalysisResults implements OnlineWorkflowResults {
	
	// the id of the workflow that generated the results
	private final String workflowId;
	// the time horizon used for the workflow
	private final TimeHorizon timeHorizon;
	// <contingency, list of <stateId, actions>>
	private Map<String, StatesWithActions> contingenciesWithActions = new HashMap<String, StatesWithActions>();
	// <contingency, list of <stateId, action plan>
	private Map<String, Map<Integer, StateWithCCOInfo>> contingenciesWithActionsInfo = new HashMap<String, Map<Integer, StateWithCCOInfo>>();
	// <contingency, list of <stateId, indexes>>
	private Map<String, StatesWithIndexes> unsafeContingencies = new HashMap<String, StatesWithIndexes>();
	
	public ForecastAnalysisResults(String workflowId, TimeHorizon timeHorizon) {
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
	
	public void addStateWithActions(String contingencyId, Integer stateId, boolean actionsFound, CCOFinalStatus status, String cause, String actionPlan, Map<String, Map<String,ActionParameters>> actions) {
		Map<Integer, StateWithCCOInfo> statesWithActionInfo = new HashMap<Integer, StateWithCCOInfo>();
		if ( contingenciesWithActionsInfo.containsKey(contingencyId) )
			statesWithActionInfo = contingenciesWithActionsInfo.get(contingencyId);
		statesWithActionInfo.put(stateId, new StateWithCCOInfo(actionsFound, status, cause, actionPlan));
		contingenciesWithActionsInfo.put(contingencyId, statesWithActionInfo);
		
		if ( actions != null ) {
			StatesWithActions statesWithActions = new StatesWithActions();
			if ( contingenciesWithActions.containsKey(contingencyId) ) {
				statesWithActions = contingenciesWithActions.get(contingencyId);
			}
			statesWithActions.addState(stateId, actions);
			contingenciesWithActions.put(contingencyId, statesWithActions);
		}
	}
	
	public void addUnsafeStateWithIndexes(String contingencyId, Integer stateId, List<SecurityIndex> indexes) {
		StatesWithIndexes unsafeStatesWithIndexes = new StatesWithIndexes();
		if ( unsafeContingencies.containsKey(contingencyId) ) {
			unsafeStatesWithIndexes = unsafeContingencies.get(contingencyId);
		}
		unsafeStatesWithIndexes.addState(stateId, indexes);
		unsafeContingencies.put(contingencyId, unsafeStatesWithIndexes);
	}
	
	@Override
	public Collection<String> getContingenciesWithActions() {
		return contingenciesWithActionsInfo.keySet();
	}
	
	@Override
	public Collection<String> getUnsafeContingencies() {
		return unsafeContingencies.keySet();
	}
	
	private StatesWithActions getStatesWithActions(String contingencyId) {
		return contingenciesWithActions.get(contingencyId);
	}

	private StatesWithIndexes getStatesWithIndexes(String contingencyId) {
		return unsafeContingencies.get(contingencyId);
	}

	@Override
	public Map<Integer,Boolean> getUnsafeStatesWithActions(String contingencyId) {
		if( contingenciesWithActionsInfo.containsKey(contingencyId) ) {
			Map<Integer,Boolean> unsafeStatesWithActions = new HashMap<Integer, Boolean>();
			for (Integer stateId : contingenciesWithActionsInfo.get(contingencyId).keySet()) {
				unsafeStatesWithActions.put(stateId, contingenciesWithActionsInfo.get(contingencyId).get(stateId).isActionFound());
			}
			return unsafeStatesWithActions;
		}
		return null;
	}
	
	@Override
	public List<Integer> getUnstableStates(String contingencyId) {
		if ( unsafeContingencies.containsKey(contingencyId) )
			return new ArrayList<Integer>(unsafeContingencies.get(contingencyId).getStates());
		return null;
	}
	
	@Override
	public CCOFinalStatus getStateStatus(String contingencyId, Integer stateId) {
		if ( contingenciesWithActionsInfo.containsKey(contingencyId) )
			return contingenciesWithActionsInfo.get(contingencyId).get(stateId).getStatus();
		return null;
	}

	@Override
	public String getCause(String contingencyId, Integer stateId) {
		if ( contingenciesWithActionsInfo.containsKey(contingencyId) )
			return contingenciesWithActionsInfo.get(contingencyId).get(stateId).getCause();
		return null;
	}
		
	@Override
	public String getActionPlan(String contingencyId, Integer stateId) {
		if ( contingenciesWithActionsInfo.containsKey(contingencyId) )
			return contingenciesWithActionsInfo.get(contingencyId).get(stateId).getActionPlan();
		return null;
	}

	@Override
	public List<String> getActionsIds(String contingencyId, Integer stateId) {
		if( contingenciesWithActions.containsKey(contingencyId) )
			return contingenciesWithActions.get(contingencyId).getActionsIds(stateId);
		return null;
	}
	
	public List<SecurityIndex> getIndexes(String contingencyId, Integer stateId) {
		if( unsafeContingencies.containsKey(contingencyId) )
			return unsafeContingencies.get(contingencyId).getIndexes(stateId);
		return null;
	}
	
	@Override
	public Map<String, Boolean> getIndexesData(String contingencyId, Integer stateId) {
		List<SecurityIndex> indexes = getIndexes(contingencyId, stateId);
		Map<String, Boolean> indexesData = new HashMap<String,Boolean>();
		for (SecurityIndex index : indexes)
			indexesData.put(index.getId().getSecurityIndexType().getLabel(), index.isOk());
		return indexesData;
	}

	@Override
	public List<String> getEquipmentsIds(String contingencyId, Integer stateId, String actionId) {
		if( contingenciesWithActions.containsKey(contingencyId) )
			return new ArrayList<String>(contingenciesWithActions.get(contingencyId).getEquipments(stateId, actionId).keySet());
		return null;
	}

	@Override
	public Map<String, ActionParameters> getEquipmentsWithParameters(String contingencyId, Integer stateId, String actionId) {
		if( contingenciesWithActions.containsKey(contingencyId) )
			return contingenciesWithActions.get(contingencyId).getEquipments(stateId, actionId);
		return null;
	}

	@Override
	public ActionParameters getParameters(String contingencyId, Integer stateId, String actionId, String equipmentId) {
		if( contingenciesWithActions.containsKey(contingencyId) && contingenciesWithActions.get(contingencyId).getEquipments(stateId, actionId)!=null )
			return contingenciesWithActions.get(contingencyId).getEquipments(stateId, actionId).get(equipmentId);
		return null;
	}
	
	public String toString() {
		String output = "time horizon: "+ timeHorizon.getName();
		output += "\n" + "contingencies with actions: " + getContingenciesWithActions();
		for(String contingencyId : getContingenciesWithActions() )
			output += "\n[contingency id = " + contingencyId + ", states = " + getStatesWithActions(contingencyId) + "]"; 
		output += "\n" + "unsafe contingencies: " + getUnsafeContingencies();
		for(String contingencyId : getUnsafeContingencies() )
			output += "\n[contingency id = " + contingencyId + ", states = " + getStatesWithIndexes(contingencyId) + "]";
		return output;
	}
	

	class StatesWithActions {
		
		// <stateId, <actions, <equipments, parameters>>>
		Map<Integer,Map<String, Map<String,ActionParameters>>> states = new HashMap<Integer,Map<String, Map<String,ActionParameters>>>();
		
		void addState(Integer stateId, Map<String, Map<String,ActionParameters>> actions) {
			states.put(stateId, actions);
		}
		
		Set<Integer> getStates() {
			return states.keySet();
		}
		
		Map<String, ActionParameters> getEquipments(Integer stateId, String actionId) {
			if( states.containsKey(stateId) )
				return states.get(stateId).get(actionId);
			return null;
		}
		
		List<String> getActionsIds(Integer stateId) {
			if( states.containsKey(stateId) )
				return new ArrayList<String>(states.get(stateId).keySet());
			return null;
		}
		
		@Override
		public String toString() {
			String output = "";
			for(Integer stateId : getStates()) {
				output += "[stateId " + stateId + ", actions = " + states.get(stateId).toString() + "]";
			}
			return output;
		}
		
	}
	
	
	class StatesWithIndexes {
		
		// <stateId, indexes>
		Map<Integer,List<SecurityIndex>> states = new HashMap<Integer,List<SecurityIndex>>();
		
		void addState(Integer stateId, List<SecurityIndex> indexes) {
			states.put(stateId, indexes);
		}
		
		Set<Integer> getStates() {
			return states.keySet();
		}
		
		List<SecurityIndex> getIndexes(Integer stateId) {
			return states.get(stateId);
		}
		
		@Override
		public String toString() {
			String output = "";
			for(Integer stateId : getStates()) {
				output += "[stateId " + stateId + ", indexes = " + securityIndexesToString(getIndexes(stateId)) + "]";
			}
			return output;
		}
		
		String securityIndexesToString(List<SecurityIndex> indexes) {
			String results = "[";
			for (SecurityIndex index : indexes) {
				results += "[" + index.getId().getSecurityIndexType().getLabel().replaceAll(" ", "_") + " safe=" + index.isOk() + "]"; 
			}
			results += "]";
			return results;
		}
		
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
