/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.optimizer;

import eu.itesla_project.modules.contingencies.ActionParameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class CorrectiveControlOptimizerResult {
	
	private final String contingencyId;
	private final boolean actionsFound;

	private String actionPlan = null;
	Map<String, Collection<String>> actions = new HashMap<String, Collection<String>>();
	Map<String,ActionParameters> equipments = new HashMap<>();
	private CCOFinalStatus finalStatus;
	private String cause;

	public CorrectiveControlOptimizerResult(String contingencyId, boolean actionsFound) {
		this.contingencyId = contingencyId;
		this.actionsFound = actionsFound;
	}

	public String getContingencyId() {
		return contingencyId;
	}

	public boolean areActionsFound() {
		return actionsFound;
	}
	
	public String getActionPlan() {
		return actionPlan;
	}

	public Collection<String> getActionsIds() {
		return actions.keySet();
	}
	
	public Collection<String> getEquipments(String actionId) {
		return actions.get(actionId);
	}
	
	public Map<String,ActionParameters> getEquipmentsWithParameters(String actionId) {
		Map<String,ActionParameters> actionEquipments = new HashMap<String, ActionParameters>();
		for (String equipmentId : actions.get(actionId)) {
			actionEquipments.put(equipmentId, equipments.get(actionEquipmentId(actionId, equipmentId)));
		}
		return actionEquipments;
	}
	
	public ActionParameters getParameters(String actionId, String equipmentId) {
		return equipments.get(actionEquipmentId(actionId, equipmentId));
	}

	public void setActionPlan(String actionPlan) {
		this.actionPlan = actionPlan;
	}
	
	public void addAction(String actionId, Map<String,ActionParameters> actionEquipments) {
		actions.put(actionId, actionEquipments.keySet());
		for (String equipmentId : actionEquipments.keySet()) {
			equipments.put(actionEquipmentId(actionId, equipmentId), actionEquipments.get(equipmentId));
		}
	}
	
	public void addEquipment(String actionId, String equipmentId, ActionParameters parameters) {
		Collection<String> actionEquiments = new ArrayList<String>();
		if ( equipments.containsKey(actionId) )
			actionEquiments = actions.get(actionId);
		actionEquiments.add(equipmentId);
		actions.put(actionId, actionEquiments);
		equipments.put(actionEquipmentId(actionId, equipmentId), parameters);
	}
	
	private String actionEquipmentId(String actionId, String equipmentId) {
		return actionId + "_" + equipmentId;
	}
	
	
	public CCOFinalStatus getFinalStatus() {
		return finalStatus;
	}

	public void setFinalStatus(CCOFinalStatus finalStatus) {
		this.finalStatus = finalStatus;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

}
