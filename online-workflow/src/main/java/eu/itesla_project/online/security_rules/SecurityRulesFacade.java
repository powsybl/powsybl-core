/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.security_rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.Contingency;
import eu.itesla_project.modules.online.OnlineRulesFacade;
import eu.itesla_project.modules.online.RulesFacadeParameters;
import eu.itesla_project.modules.online.RulesFacadeResults;
import eu.itesla_project.modules.rules.RuleAttributeSet;
import eu.itesla_project.modules.rules.RulesDbClient;
import eu.itesla_project.modules.rules.SecurityRule;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class SecurityRulesFacade implements OnlineRulesFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityRulesFacade.class);

	private final RulesDbClient rulesDbClient;

	private final Map<String, ContingencyEvaluator> evaluators = new HashMap<>();

	public SecurityRulesFacade(RulesDbClient rulesDbClient) {
		this.rulesDbClient = rulesDbClient;
	}

	@Override
	public void init(RulesFacadeParameters parameters) throws Exception {
		Objects.requireNonNull(parameters, "parameters is null");
        // preload security rules
		SecurityIndexType[] securityIndexTypes = parameters.getSecurityIndexTypes() == null ? SecurityIndexType.values()
                : parameters.getSecurityIndexTypes().toArray(new SecurityIndexType[parameters.getSecurityIndexTypes().size()]);
        for (Contingency contingency : parameters.getContingencies()) {
            List<SecurityRule> mcRules = new ArrayList<>(); // rules for the current contingency
            List<SecurityRule> wcaRules = new ArrayList<>(); // wca rules for the current contingency
            for (SecurityIndexType securityIndexType :securityIndexTypes) {
                LOGGER.info("Getting mc security rule for {} contingency and {} index", contingency.getId(), securityIndexType);
                mcRules.addAll(rulesDbClient.getRules(parameters.getOfflineWorkflowId(), RuleAttributeSet.MONTE_CARLO, contingency.getId(), securityIndexType));
                if ( parameters.wcaRules() )  { // get wca rules for validation
                	LOGGER.info("Getting wca security rule for {} contingency and {} index", contingency.getId(), securityIndexType);
                	wcaRules.addAll(rulesDbClient.getRules(parameters.getOfflineWorkflowId(), RuleAttributeSet.WORST_CASE, contingency.getId(), securityIndexType));
                }
            }
            if ( parameters.wcaRules() ) // store wca rules for validation
            	evaluators.put(contingency.getId(), new ContingencyEvaluator(contingency, mcRules, wcaRules, parameters.getPurityThreshold()));
            else
            	evaluators.put(contingency.getId(), new ContingencyEvaluator(contingency, mcRules, parameters.getPurityThreshold()));
        }
	}

	public ContingencyEvaluator getContingencyEvaluator(Contingency contingency) {
		Objects.requireNonNull(contingency, "contingency is null");
        ContingencyEvaluator evaluator = evaluators.get(contingency.getId());
        if (evaluator == null) {
            throw new RuntimeException("Security rules for contingency {} has not been preloaded");
        }
        return evaluator;
	}

	@Override
	public RulesFacadeResults evaluate(Contingency contingency, Network network) {
		return getContingencyEvaluator(contingency).evaluate(network);
	}

	@Override
	public RulesFacadeResults wcaEvaluate(Contingency contingency, Network network) {
		return getContingencyEvaluator(contingency).wcaEvaluate(network);
	}



}
