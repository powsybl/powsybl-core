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
public class StatusSynthesis implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static int STATUS_UNDEFINED=-1;
	public static int STATUS_IDLE=0;
	public static int STATUS_RUNNING=1;
	public static int STATUS_STOPPED=2;
	public static int STATUS_TERMINATED=3;
	
	private String workflowId;
	private int status;
	
	public StatusSynthesis()
	{
		
	}
	
	public StatusSynthesis(String workflowId, int status)
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	
	

}
