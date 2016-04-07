/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ContingencyStatesActionsSynthesis implements Serializable {

	private static final long serialVersionUID = 1L;

	private final HashMap<String,Map> contingencyMap=new HashMap<String,Map>(); //contingencyid

	private String workflowId;

	public ContingencyStatesActionsSynthesis() {
		
	}
	
	public ContingencyStatesActionsSynthesis(String workflowId) {
		this.workflowId=workflowId;
	}
	
	
	public String getWorkflowId() {
		return workflowId;
	}
	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}
	
	public void addStateActions(String contingencyId, Integer stateId,List<ActionInfo> actions){
		Map<Integer,List<ActionInfo>> statesActions = contingencyMap.get(contingencyId);
		if(statesActions==null)
			statesActions = new HashMap<Integer,List<ActionInfo>>();
		
		
		
		statesActions.put(stateId, actions);
		System.out.println(" statesActions -->stateId: "+stateId);
		contingencyMap.put(contingencyId, statesActions);
	}

	public HashMap<String, Map> getContingencyMap() {
		return contingencyMap;
	}

	
	   

	}

