/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.modules.online.TimeHorizon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class OnlineWorkflowContext {

    private String offlineWorkflowId;

	// workflow id
    String workflowId;
    // network under analysis
	Network network;
	// time horizon of the analysis
	TimeHorizon timeHorizon;
	// contingencies that need to be analyzed, result of the WSA step
	List<Contingency> contingenciesToAnalyze = new ArrayList<>();
	// counter for the states
	private final AtomicInteger stateCounter = new AtomicInteger(0);
	// analysis results
	ForecastAnalysisResults results;
	// security rules application results
	SecurityRulesApplicationResults securityRulesResults;
	// wca results
	WCAResults wcaResults;
	// wca security rules application results
	SecurityRulesApplicationResults wcaSecurityRulesResults;

	
	public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
	
	public String getOfflineWorkflowId() {
        return offlineWorkflowId;
    }

    public void setOfflineWorkflowId(String offlineWorkflowId) {
        this.offlineWorkflowId = offlineWorkflowId;
    }

	public List<Contingency> getContingenciesToAnalyze() {
		return contingenciesToAnalyze;
	}

	public void setContingenciesToAnalyze(List<Contingency> contingenciesToAnalyze) {
		this.contingenciesToAnalyze = contingenciesToAnalyze;
	}

	public TimeHorizon getTimeHorizon() {
		return timeHorizon;
	}

	public void setTimeHorizon(TimeHorizon timeHorizon) {
		this.timeHorizon = timeHorizon;
	}

	public Network getNetwork() {
		return network;
	}

	public void setNetwork(Network network) {
		this.network=network;
	}

	public int incrementStateCounter() {
        return stateCounter.getAndIncrement();
    }

	public void setResults(ForecastAnalysisResults forecastAnalysisResults) {
		results = forecastAnalysisResults;
	}

	public ForecastAnalysisResults getResults() {
		return results;
	}
	
	public void setSecurityRulesResults(SecurityRulesApplicationResults securityRulesResults) {
		this.securityRulesResults = securityRulesResults;
	}

	public SecurityRulesApplicationResults getSecurityRulesResults() {
		return securityRulesResults;
	}
	
	public void setWcaResults(WCAResults wcaResults) {
		this.wcaResults = wcaResults;
	}
	
    public WCAResults getWcaResults() {
		return wcaResults;
	}
    
    public void setWcaSecurityRulesResults(SecurityRulesApplicationResults wcaSecurityRulesResults) {
		this.wcaSecurityRulesResults = wcaSecurityRulesResults;
	}

	public SecurityRulesApplicationResults getWcaSecurityRulesResults() {
		return wcaSecurityRulesResults;
	}
}

