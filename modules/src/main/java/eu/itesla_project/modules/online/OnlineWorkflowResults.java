/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.online;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import eu.itesla_project.modules.contingencies.ActionParameters;
import eu.itesla_project.modules.optimizer.CCOFinalStatus;

/**
 * The results of a run of the online workflow
 *
 * @author Quinary <itesla@quinary.com>
 */
public interface OnlineWorkflowResults {
	
	/**
	 * Get the id of the workflow that generated the results
	 * @return the id of the workflow
	 */
	String getWorkflowId();
	
	/**
	 * Get the time horizon used for the workflow
	 * @return the time horizon of the workflow
	 */
	TimeHorizon getTimeHorizon();

	
	/**
	 * Get the contingencies, analyzed by the workflow, that requires corrective actions
	 * @return the collection of ids of the contingencies requiring corrective actions
	 */
	Collection<String> getContingenciesWithActions();
	
	/**
	 * Get the unsafe contingencies analysed by the workflow
	 * @return the collection of ids of the unsafe contingencies
	 */
	Collection<String> getUnsafeContingencies();
	
	
	/**
	 * Get the states that requires corrective actions, given a contingency
	 * @param contingencyId the id of the contingency
	 * @return a map of [id, action found flag] for the states requiring corrective actions
	 */
	Map<Integer,Boolean> getUnsafeStatesWithActions(String contingencyId);
	
	/**
	 * Get the unstable states, given a contingency
	 * @param contingencyId the id of the contingency
	 * @return the list of ids of the unstable states
	 */
	List<Integer> getUnstableStates(String contingencyId);
	
	
	/**
	 * Get the status, related to corrective actions, of a state for a contingency
	 * @param contingencyId the id of the contingency
	 * @param stateId the id of the state
	 * @return the status, related to corrective actions, of a state for a contingency
	 */
	CCOFinalStatus getStateStatus(String contingencyId, Integer stateId);
	
	
	/**
	 * Get the cause, related to corrective actions found or not, about a state and a contingency
	 * @param contingencyId the id of the contingency
	 * @param stateId the id of the state
	 * @return the cause, related to corrective actions found or not, about a state and a contingency
	 */
	String getCause(String contingencyId, Integer stateId);
	
	/**
	 * Get the action plan required to make a state stable, for a contingency
	 * @param contingencyId the id of the contingency
	 * @param stateId the id of the state
	 * @return the id of the action plan required to make a state stable, for a contingency
	 */
	String getActionPlan(String contingencyId, Integer stateId);
	
	/**
	 * Get the actions required to make a state stable, for a contingency
	 * @param contingencyId the id of the contingency
	 * @param stateId the id of the state
	 * @return the list of ids of actions required to make a state stable, for a contingency
	 */
	List<String> getActionsIds(String contingencyId, Integer stateId);
	
	/**
	 * Get the security indexes, output of a T-D simulation on a state for a contingency
	 * @param contingencyId the id of the contingency
	 * @param stateId the id of the state
	 * @return the map of [index, security flag] pair, output of a T-D simulation on a state for a contingency
	 */
	Map<String,Boolean> getIndexesData(String contingencyId, Integer stateId);

	
	/**
	 * Get the equipments of an action, given a contingency and a state 
	 * @param contingencyId the contingency id
	 * @param stateId the state id
	 * @param actionId the action id
	 * @return the list of equipments of an action
	 */
	List<String> getEquipmentsIds(String contingencyId, Integer stateId, String actionId);
	
	/**
	 * Get the equipments of an action, with the related parameters, given a contingency and a state
	 * @param contingencyId the contingency id
	 * @param stateId the state id
	 * @param actionId the action id
	 * @return the map of [equipment, parameters] pairs, for an action, given a contingency and a state
	 */
	Map<String, ActionParameters> getEquipmentsWithParameters(String contingencyId, Integer stateId, String actionId);
	
	/**
	 * Get the parameters for an equipment of an action, given a contingency and a state
	 * @param contingencyId the contingency id
	 * @param stateId the state id
	 * @param actionId the action id
	 * @param equipmentId
	 * @return the parameters of the action
	 */
	ActionParameters getParameters(String contingencyId, Integer stateId, String actionId, String equipmentId);
}
