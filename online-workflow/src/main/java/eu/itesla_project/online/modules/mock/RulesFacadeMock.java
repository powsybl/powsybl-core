/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.modules.mock;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.Contingency;
import eu.itesla_project.modules.online.OnlineRulesFacade;
import eu.itesla_project.modules.online.RulesFacadeParameters;
import eu.itesla_project.modules.online.RulesFacadeResults;
import eu.itesla_project.modules.online.StateStatus;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;

/**
*
* @author Quinary <itesla@quinary.com>
*/
public class RulesFacadeMock implements OnlineRulesFacade {

	RulesFacadeParameters parameters;
	
	@Override
	public void init(RulesFacadeParameters parameters) throws Exception {
		this.parameters = parameters;
	}

	@Override
	public RulesFacadeResults evaluate(Contingency contingency, Network network) {
		Objects.requireNonNull(contingency, "contingency is null");
		Objects.requireNonNull(network, "network is null");
		return getMockResults(contingency, network);
	}

	@Override
	public RulesFacadeResults wcaEvaluate(Contingency contingency, Network network) {
		Objects.requireNonNull(contingency, "contingency is null");
		Objects.requireNonNull(network, "network is null");
		return getMockResults(contingency, network);
	}
	
	private RulesFacadeResults getMockResults(Contingency contingency, Network network) {
		Map<SecurityIndexType, StateStatus> indexesResults = new EnumMap<>(SecurityIndexType.class);
		for (SecurityIndexType indexType : parameters.getSecurityIndexTypes()) {
			indexesResults.put(indexType, StateStatus.SAFE);
		}
		return new RulesFacadeResults(network.getStateManager().getWorkingStateId(), contingency.getId(), StateStatus.SAFE, indexesResults);
	}

}
