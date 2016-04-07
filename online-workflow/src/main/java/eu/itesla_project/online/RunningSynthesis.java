/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import java.io.Serializable;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class RunningSynthesis implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String workflowId;
	private boolean running;
	
	public RunningSynthesis()
	{
		
	}
	
	public RunningSynthesis(String workflowId, boolean running)
	{
		this.workflowId=workflowId;
		this.running=running;
	}
	
	public String getWorkflowId() {
		return workflowId;
	}
	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}
	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	

}
