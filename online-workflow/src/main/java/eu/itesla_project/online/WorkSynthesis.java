/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class WorkSynthesis implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String workflowId;
	private HashMap<Integer,WorkStatus> status;
	
	public WorkSynthesis(){};
	
	public WorkSynthesis(String workflowId, HashMap<Integer,WorkStatus> status)
	{
		this.workflowId=workflowId;
		this.status=status;
	}
	
	public String getWorkflowId() {
		return workflowId;
	}
	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}
	public HashMap<Integer, WorkStatus> getStatus() {
		return status;
	}
	public void setStatus(HashMap<Integer, WorkStatus> status) {
		this.status = status;
	}

}
