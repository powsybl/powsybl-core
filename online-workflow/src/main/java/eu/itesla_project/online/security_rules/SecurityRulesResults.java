/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.security_rules;

import eu.itesla_project.modules.online.StateStatus;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;

import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class SecurityRulesResults {

	private final String stateId;
	private final String contingencyId;
	private final StateStatus stateStatus;
	private final Map<SecurityIndexType, StateStatus> indexesResults;

	public SecurityRulesResults(String stateId, String contingencyId, StateStatus stateStatus, Map<SecurityIndexType, StateStatus> indexesResults) {
		this.stateId = stateId;
		this.contingencyId = contingencyId;
		this.stateStatus = stateStatus;
		this.indexesResults = indexesResults;
	}

	public String getStateId() {
		return stateId;
	}

	public String getContingencyId() {
		return contingencyId;
	}

	public StateStatus getStateStatus() {
		return stateStatus;
	}

	public Map<SecurityIndexType, StateStatus> getIndexesResults() {
		return indexesResults;
	}

}
