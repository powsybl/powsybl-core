/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import eu.itesla_project.modules.online.OnlineWorkflowWcaResults;
import eu.itesla_project.modules.online.TimeHorizon;
import eu.itesla_project.modules.wca.WCACluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class WCAResults implements OnlineWorkflowWcaResults {

	// the id of the workflow where the WCA has been applied
	private final String workflowId;
	// the time horizon used for the workflow where the WCA has been applied
	private final TimeHorizon timeHorizon;
	// <contingency, cluster>
	private Map<String, WCACluster> contingenciesWithClusters = new HashMap<String, WCACluster>();
	
	public WCAResults(String workflowId, TimeHorizon timeHorizon) {
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
	
	public void addContingencyWithCluster(String contingencyId, WCACluster cluster) {
		contingenciesWithClusters.put(contingencyId, cluster);
	}
	
	@Override
	public Set<String> getContingencies() {
		return contingenciesWithClusters.keySet();
	}
	
	public Map<String, WCACluster>  getContingenciesWithClusters() {
		return contingenciesWithClusters;
	}
	
	public WCACluster getCluster(String contingencyId) {
		return contingenciesWithClusters.get(contingencyId);
	}
	
	@Override
	public int getClusterIndex(String contingencyId) {
		return contingenciesWithClusters.get(contingencyId).getNum().toIntValue();
	}
	
	@Override
	public List<String> getCauses(String contingencyId) {
		return contingenciesWithClusters.get(contingencyId).getCauses();
	}
	
	public  String toString() {
		String output = "time horizon: "+ timeHorizon.getName();
		output += "\n" + "contingencies with cluster: " + getContingencies();
		for(String contingencyId : getContingencies() )
			output += "\n[contingecy id = " + contingencyId + ", cluster = " + getClusterIndex(contingencyId) + "]"; 
		return output;
		
	}
	
}
