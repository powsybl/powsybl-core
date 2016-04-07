/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.server;


import java.util.HashMap;

import eu.itesla_project.online.ContingencyStatesActionsSynthesis;
import eu.itesla_project.online.ContingencyStatesIndexesSynthesis;
import eu.itesla_project.online.IndexSecurityRulesResultsSynthesis;
import eu.itesla_project.online.StatusSynthesis;
import eu.itesla_project.online.WcaContingenciesSynthesis;
import eu.itesla_project.online.WorkStatus;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class OnlineWorkflowInfo {
	
	
	private String worflowId;
	private int status=StatusSynthesis.STATUS_UNDEFINED;
	private boolean wcaRunning;
	private HashMap<Integer,WorkStatus> workStatus;
	private ContingencyStatesActionsSynthesis statesActions;
	private ContingencyStatesIndexesSynthesis unsafeContingencies;
	private IndexSecurityRulesResultsSynthesis  securityRulesIndexesApplication;
	
	//private UnstableContingenciesSynthesis unstableContingencies;
	//private String[] stableContingencies;
	//10/03/2015 Stable and ustable contingencies were merged into wcaContingencies
	private WcaContingenciesSynthesis wcaContingencies;
	
	
	public OnlineWorkflowInfo(String id)
	{
		worflowId=id;
		status=StatusSynthesis.STATUS_IDLE;
		workStatus=new HashMap<Integer,WorkStatus>();
		
	}

	public String getWorflowId() {
		return worflowId;
	}

	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isWcaRunning() {
		return wcaRunning;
	}

	public void setWcaRunning(boolean wcaRunning2) {
		this.wcaRunning = wcaRunning2;
	}

	public HashMap<Integer, WorkStatus> getWorkStatus() {
		return workStatus;
	}

	public void setWorkStatus(HashMap<Integer, WorkStatus> workStatus) {
		this.workStatus = workStatus;
	}

	public ContingencyStatesActionsSynthesis getStatesActions() {
		return statesActions;
	}

	public void setStatesActions(
			ContingencyStatesActionsSynthesis statesActions) {
		this.statesActions = statesActions;
	}

	public ContingencyStatesIndexesSynthesis getUnsafeContingencies() {
		return unsafeContingencies;
	}

	public void setUnsafeContingencies(
			ContingencyStatesIndexesSynthesis unsafeContingencies) {
		this.unsafeContingencies = unsafeContingencies;
	}

	/*public String[] getStableContingencies() {
		return stableContingencies;
	}

	public void setStableContingencies(String[] stableContingencies) {
		this.stableContingencies = stableContingencies;
	}
*/

	public IndexSecurityRulesResultsSynthesis getSecurityRulesIndexesApplication() {
		return securityRulesIndexesApplication;
	}


	public void setSecurityRulesIndexesApplication(IndexSecurityRulesResultsSynthesis  indexesSecurityRulesApplicatio) {
		this.securityRulesIndexesApplication = indexesSecurityRulesApplicatio;
	}
	
/*
	public UnstableContingenciesSynthesis getUnstableContingencies() {
		return unstableContingencies;
	}


	public void setUnstableContingencies(UnstableContingenciesSynthesis unstableContingencies) {
		this.unstableContingencies = unstableContingencies;
	}
	*/

	public WcaContingenciesSynthesis getWcaContingencies() {
		return wcaContingencies;
	}

	public void setWcaContingencies(WcaContingenciesSynthesis wcaContingencies) {
		this.wcaContingencies = wcaContingencies;
	}
}
