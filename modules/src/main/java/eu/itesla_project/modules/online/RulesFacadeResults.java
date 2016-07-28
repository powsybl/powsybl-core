/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.online;

import java.util.List;
import java.util.Map;

import eu.itesla_project.modules.securityindexes.SecurityIndexType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class RulesFacadeResults {

    private final String stateId;
    private final String contingencyId;
    private final StateStatus stateStatus;
    private final Map<SecurityIndexType, StateStatus> indexesResults;
    private final List<SecurityIndexType> invalidRules;
    private final boolean rulesAvailable;

    public RulesFacadeResults(String stateId, String contingencyId, StateStatus stateStatus, Map<SecurityIndexType, StateStatus> indexesResults, 
            List<SecurityIndexType> invalidRules, boolean rulesAvailable) {
        this.stateId = stateId;
        this.contingencyId = contingencyId;
        this.stateStatus = stateStatus;
        this.indexesResults = indexesResults;
        this.invalidRules = invalidRules;
        this.rulesAvailable = rulesAvailable;
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

	public List<SecurityIndexType> getInvalidRules() {
		return invalidRules;
	}
	
	public boolean areRulesAvailable() {
		return rulesAvailable;
	}

}
