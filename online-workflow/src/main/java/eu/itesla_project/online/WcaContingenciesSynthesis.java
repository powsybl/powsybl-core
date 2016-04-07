/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import eu.itesla_project.modules.wca.WCACluster;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class WcaContingenciesSynthesis implements Serializable{
	
	
	private static final long serialVersionUID = 1L;
	
	private String workflowId;
	
	private Map<String, Integer> contingenciesCluster= new Hashtable<String, Integer>();
	
	

	public WcaContingenciesSynthesis( String workflowId, Map<String, WCACluster> contingenciesWithClusters ){
		this.workflowId=workflowId;
		Set<Entry<String,WCACluster>> contingencyWithCluster=contingenciesWithClusters.entrySet();
		
		for (Entry<String,WCACluster> cc: contingencyWithCluster)
			contingenciesCluster.put(cc.getKey(), new Integer(cc.getValue().getNum().toIntValue()));
		
	}
	
	public String getWorkflowId() {
		return workflowId;
	}
	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	
	
	public Map<String, Integer> getContingenciesCluster() {
		return contingenciesCluster;
	}

	public void setContingenciesCluster(Map<String, Integer> contingenciesCluster) {
		this.contingenciesCluster = contingenciesCluster;
	}

	

}
