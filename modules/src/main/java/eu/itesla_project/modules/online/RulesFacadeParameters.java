/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.online;

import eu.itesla_project.modules.contingencies.Contingency;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class RulesFacadeParameters {

    private final String offlineWorkflowId;
	private final List<Contingency> contingencies;
	private final double purityThreshold;
	private final Set<SecurityIndexType> securityIndexTypes; // all security index types if null
	private final boolean wcaRules;

	public RulesFacadeParameters(String offlineWorkflowId, List<Contingency> contingencies, double purityThreshold, 
								   Set<SecurityIndexType> securityIndexTypes, boolean wcaRules) {
        this.offlineWorkflowId = offlineWorkflowId;
		this.contingencies = contingencies;
		this.purityThreshold = purityThreshold;
		this.securityIndexTypes = securityIndexTypes;
		this.wcaRules = wcaRules;
	}

    public String getOfflineWorkflowId() {
        return offlineWorkflowId;
    }

	public List<Contingency> getContingencies() {
		return contingencies;
	}

	public double getPurityThreshold() {
		return purityThreshold;
	}
	
	public Set<SecurityIndexType> getSecurityIndexTypes() {
		return securityIndexTypes;
	}

	public boolean wcaRules() {
		return wcaRules;
	}
}
