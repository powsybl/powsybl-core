/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONSerializer;
import eu.itesla_project.modules.contingencies.ActionParameters;
import eu.itesla_project.modules.online.OnlineWorkflowResults;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class Utils {

	public static String actionsToJson(OnlineWorkflowResults wfResults, String contingencyId, Integer stateId) {
		Map<String, Object> actionInfo = new HashMap<String, Object>();
		actionInfo.put("actions_found", wfResults.getUnsafeStatesWithActions(contingencyId).get(stateId));
		actionInfo.put("status", wfResults.getStateStatus(contingencyId, stateId));
		List<Object> actions = new ArrayList<Object>();
		for (String actionId : wfResults.getActionsIds(contingencyId, stateId)) {
			if ( wfResults.getEquipmentsIds(contingencyId, stateId, actionId) != null 
					 && !wfResults.getEquipmentsIds(contingencyId, stateId, actionId).isEmpty() ) {
					Map<String, Object> action = new HashMap<String, Object>();
					List<Object> equipments = new ArrayList<Object>();
					for (String equipmentId : wfResults.getEquipmentsIds(contingencyId, stateId, actionId)) {
						ActionParameters actionParameters = wfResults.getParameters(contingencyId, stateId, actionId, equipmentId);
						if ( actionParameters != null && !actionParameters.getNames().isEmpty() ) {
							Map<String, Map<String, Object>> equipment = new HashMap<String, Map<String,Object>>();
							Map<String, Object> params = new HashMap<String, Object>();
							for(String param : actionParameters.getNames()) {
								params.put(param, actionParameters.getValue(param));
							}
							equipment.put(equipmentId, params);
							equipments.add(equipment);
						} else
							equipments.add(equipmentId);
					}
					action.put(actionId, equipments);
					actions.add(action);
				} else 
					actions.add(actionId);
		}
		if ( wfResults.getActionPlan(contingencyId, stateId) != null ) {
			actionInfo.put(wfResults.getActionPlan(contingencyId, stateId), actions);
		} else {
			if ( !actions.isEmpty() )
				actionInfo.put("actions", actions);
		}
		return JSONSerializer.toJSON(actionInfo).toString();
	}
	
	public static String actionsToJsonExtended(OnlineWorkflowResults wfResults, String contingencyId, Integer stateId) {
		Map<String, Object> actionInfo = new HashMap<String, Object>();
		actionInfo.put("actions_found", wfResults.getUnsafeStatesWithActions(contingencyId).get(stateId));
		actionInfo.put("status", wfResults.getStateStatus(contingencyId, stateId));
		String cause = wfResults.getCause(contingencyId, stateId);
		if ( cause != null )
			actionInfo.put("cause", cause);
		if ( wfResults.getActionsIds(contingencyId, stateId) != null && !wfResults.getActionsIds(contingencyId, stateId).isEmpty() ) {
			List<Object> actions = new ArrayList<Object>();
			for (String actionId : wfResults.getActionsIds(contingencyId, stateId)) {
				Map<String, Object> action = new HashMap<String, Object>();
				action.put("action_id", actionId);
				if ( wfResults.getEquipmentsIds(contingencyId, stateId, actionId) != null 
					 && !wfResults.getEquipmentsIds(contingencyId, stateId, actionId).isEmpty() ) {
					List<Object> equipments = new ArrayList<Object>();
					for (String equipmentId : wfResults.getEquipmentsIds(contingencyId, stateId, actionId)) {
						Map<String, Object> equipment = new HashMap<String, Object>();
						equipment.put("equipment_id", equipmentId);
						ActionParameters actionParameters = wfResults.getParameters(contingencyId, stateId, actionId, equipmentId);
						if ( actionParameters != null && !actionParameters.getNames().isEmpty() ) {
							Map<String, Object> params = new HashMap<String, Object>();
							for(String param : actionParameters.getNames()) {
								params.put(param, actionParameters.getValue(param));
							}
							equipment.put("parameters", params);
						}
						equipments.add(equipment);
					}
					action.put("equipments", equipments);
				} 
				actions.add(action);
			}
			actionInfo.put("actions", actions);
		}
		if ( wfResults.getActionPlan(contingencyId, stateId) != null ) {
			actionInfo.put("action_plan", wfResults.getActionPlan(contingencyId, stateId));
		}
		return JSONSerializer.toJSON(actionInfo).toString();
	}
}
