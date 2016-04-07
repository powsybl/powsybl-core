/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.db;

import eu.itesla_project.modules.online.OnlineWorkflowWcaResults;
import eu.itesla_project.modules.online.TimeHorizon;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
class OnlineWorkflowWcaResultsImpl implements OnlineWorkflowWcaResults {

	// the id of the workflow where the rules have been applied
	private final String workflowId;
	// the time horizon used for the workflow where the rules have been applied
	private final TimeHorizon timeHorizon;
	// <contingency, cluster>
	private Map<String, Integer> contingenciesWithClusters = new HashMap<String, Integer>();
	// <contingency, cause>
	private Map<String, List<String>> contingenciesWithCauses = new HashMap<>();
	
	OnlineWorkflowWcaResultsImpl(String workflowId, TimeHorizon timeHorizon) {
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

	@Override
	public Collection<String> getContingencies() {
		return contingenciesWithClusters.keySet();
	}

	@Override
	public int getClusterIndex(String contingencyId) {
		return contingenciesWithClusters.get(contingencyId).intValue();
	}

	@Override
	public List<String> getCauses(String contingencyId) {
		return contingenciesWithCauses.get(contingencyId);
	}
	
	void addContingencyWithCluster(String contingencyId, int cluster, List<String> causes) {
		contingenciesWithClusters.put(contingencyId, Integer.valueOf(cluster));
		contingenciesWithCauses.put(contingencyId, causes);
	}



}
