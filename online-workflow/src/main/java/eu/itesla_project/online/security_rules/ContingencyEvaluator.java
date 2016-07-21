/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.security_rules;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.Contingency;
import eu.itesla_project.modules.histo.HistoDbAttributeId;
import eu.itesla_project.modules.histo.IIDM2DB;
import eu.itesla_project.modules.online.RulesFacadeResults;
import eu.itesla_project.modules.online.StateStatus;
import eu.itesla_project.modules.rules.SecurityRule;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ContingencyEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContingencyEvaluator.class);

    private final Contingency contingency;
    private final List<SecurityRule> mcRules;
    private final double purityThreshold;
    private List<SecurityRule> wcaRules = new ArrayList<SecurityRule>();
    private Map<SecurityIndexType, List<String>> mcViolatedEquipment = new HashMap<>();
    private Map<SecurityIndexType, List<String>> wcaViolatedEquipment = new HashMap<>();
    private final boolean checkRules;
    private List<SecurityIndexType> bacecaseInvalidMcRulesIndexes = new ArrayList<SecurityIndexType>();
    private List<SecurityIndexType> bacecaseInvalidWcaRulesIndexes = new ArrayList<SecurityIndexType>();

    public ContingencyEvaluator(Contingency contingency, List<SecurityRule> mcRules, double purityThreshold, Map<SecurityIndexType, 
            List<String>> mcViolatedEquipment, boolean checkRules) {
        Objects.requireNonNull(contingency, "contingency is null");
        Objects.requireNonNull(mcRules, "mc rules is null");
        this.contingency = contingency;
        this.mcRules = mcRules;
        this.purityThreshold = purityThreshold;
        this.mcViolatedEquipment = mcViolatedEquipment;
        this.checkRules = checkRules;
    }

    public ContingencyEvaluator(Contingency contingency, List<SecurityRule> mcRules, List<SecurityRule> wcaRules, double purityThreshold,
            Map<SecurityIndexType, List<String>> mcViolatedEquipment, Map<SecurityIndexType, List<String>> wcaViolatedEquipment,
            boolean checkRules) {
        Objects.requireNonNull(contingency, "contingency is null");
        Objects.requireNonNull(mcRules, "mc rules is null");
        Objects.requireNonNull(wcaRules, "wca rules is null");
        this.contingency = contingency;
        this.mcRules = mcRules;
        this.wcaRules = wcaRules;
        this.purityThreshold = purityThreshold;
        this.mcViolatedEquipment = mcViolatedEquipment;
        this.wcaViolatedEquipment = wcaViolatedEquipment;
        this.checkRules = checkRules;
    }

    public Contingency getContingency() {
        return contingency;
    }

    public RulesFacadeResults evaluate(Network network) {
        return evaluate(network, mcRules, bacecaseInvalidMcRulesIndexes);
    }

    public RulesFacadeResults evaluate(String networkId, String stateId, Map<HistoDbAttributeId, Object> networkValues) {
        return evaluate(networkId, stateId, networkValues, mcRules, bacecaseInvalidMcRulesIndexes);
    }

    public RulesFacadeResults wcaEvaluate(Network network) {
        return evaluate(network, wcaRules, bacecaseInvalidWcaRulesIndexes);
    }

    public RulesFacadeResults wcaEvaluate(String networkId, String stateId, Map<HistoDbAttributeId, Object> networkValues) {
        return evaluate(networkId, stateId, networkValues, wcaRules, bacecaseInvalidWcaRulesIndexes);
    }

    private RulesFacadeResults evaluate(Network network, List<SecurityRule> rules, List<SecurityIndexType> bacecaseInvalidRulesIndexes) {
        Objects.requireNonNull(network, "network is null");
        HashMap<HistoDbAttributeId, Object> networkValues = IIDM2DB.extractCimValues(network, new IIDM2DB.Config(null, true)).getSingleValueMap();
        return evaluate(network.getId(), network.getStateManager().getWorkingStateId(), networkValues, rules, bacecaseInvalidRulesIndexes);
    }

    private RulesFacadeResults evaluate(String networkId, String stateId, Map<HistoDbAttributeId, Object> networkValues, List<SecurityRule> rules,
            List<SecurityIndexType> bacecaseInvalidRulesIndexes) {
        Objects.requireNonNull(networkValues, "networkValues is null");
        LOGGER.info("Evaluating {} network, {} state, {} contingency", networkId, stateId, contingency.getId());
        StateStatus stateStatus = StateStatus.SAFE;
        List<SecurityIndexType> invalidRulesIndexes = new ArrayList<SecurityIndexType>();
        boolean staticIndexUnsafe = false;
        boolean dynamicIndexUnsafe = false;
        if ( checkRules ) {
            checkRules(rules, invalidRulesIndexes, bacecaseInvalidRulesIndexes);
            if ( "0".equals(stateId) )
                bacecaseInvalidRulesIndexes = invalidRulesIndexes; // keep for samples evaluation -> rules invalid on basecase are invalid for all samples
        }
        Map<SecurityIndexType, StateStatus> indexesResults = new EnumMap<>(SecurityIndexType.class);
        for (SecurityRule rule : rules) {
            boolean safe = rule.toExpression(purityThreshold).check(networkValues).isSafe();
            LOGGER.debug("{}: Result on {} network, {} state, {} contingency, {} index: safe = {}",
                    rule.getId(), networkId, stateId, contingency.getId(), rule.getId().getSecurityIndexId().getSecurityIndexType(), safe);
            if ( safe ) {
                indexesResults.put(rule.getId().getSecurityIndexId().getSecurityIndexType(), StateStatus.SAFE);
            } else { // one index unsafe -> state-contingency unsafe
                indexesResults.put(rule.getId().getSecurityIndexId().getSecurityIndexType(), StateStatus.UNSAFE);
                if (rule.getId().getSecurityIndexId().getSecurityIndexType().isDynamic()) {
                    dynamicIndexUnsafe = true;
                } else {
                    staticIndexUnsafe = true;
                }
            }
        }
        if (dynamicIndexUnsafe) { // at least one dynamic index unsafe -> status unsafe
            stateStatus = StateStatus.UNSAFE;
        } else if (staticIndexUnsafe) { // all dynamic indexes safe, at least one static index unsafe -> status safe with corrective action
            stateStatus = StateStatus.SAFE_WITH_CORRECTIVE_ACTIONS;
        }
        LOGGER.info("Result on {} network, {} state, {} contingency: {}", networkId, stateId, contingency.getId(), stateStatus);
        return new RulesFacadeResults(stateId, contingency.getId(), stateStatus, indexesResults, invalidRulesIndexes, !rules.isEmpty());
    }

    private void checkRules(List<SecurityRule> rules, List<SecurityIndexType> invalidRulesIndexes,
            List<SecurityIndexType> bacecaseInvalidRulesIndexes) {
        // TODO: check if the rules are still valid, using the violations
        // and keeping into account the rules invalid on basecase
    }

}
