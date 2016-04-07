/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.histo.HistoDbAttributeId;
import eu.itesla_project.modules.histo.IIDM2DB;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityRuleUtil {

    private SecurityRuleUtil() {
    }

    public static Map<String, Map<SecurityIndexType, SecurityRuleCheckStatus>>
            checkRules(Network network, RulesDbClient rulesDb, String workflowId, RuleAttributeSet attributeSet,
                       Set<SecurityIndexType> securityIndexTypes, Set<String> contingencies, double purityThreshold) {
        Map<HistoDbAttributeId, Object> values = IIDM2DB.extractCimValues(network, new IIDM2DB.Config(null, false)).getSingleValueMap();

        // check rules
        Map<String, Map<SecurityIndexType, SecurityRuleCheckStatus>> checkStatusPerContingency = new LinkedHashMap<>();

        // get rules from db
        Collection<RuleId> ruleIds = rulesDb.listRules(workflowId, attributeSet).stream()
                .filter(ruleId -> securityIndexTypes.contains(ruleId.getSecurityIndexId().getSecurityIndexType())
                        && (contingencies == null || contingencies.contains(ruleId.getSecurityIndexId().getContingencyId())))
                .collect(Collectors.toList());

        // TODO filter rules that does not apply to the network

        // sort rules per contingency
        Multimap<String, RuleId> ruleIdsPerContingency = Multimaps.index(ruleIds, ruleId -> {
            return ruleId.getSecurityIndexId().getContingencyId();
        });

        for (Map.Entry<String, Collection<RuleId>> entry : ruleIdsPerContingency.asMap().entrySet()) {
            String contingencyId = entry.getKey();

            Map<SecurityIndexType, SecurityRuleCheckStatus> checkStatus = new EnumMap<>(SecurityIndexType.class);
            for (SecurityIndexType securityIndexType : securityIndexTypes) {
                checkStatus.put(securityIndexType, SecurityRuleCheckStatus.NA);
            }

            for (RuleId ruleId : entry.getValue()) {
                List<SecurityRule> rules = rulesDb.getRules(workflowId, attributeSet, contingencyId, ruleId.getSecurityIndexId().getSecurityIndexType());
                if (rules.size() > 0) {
                    SecurityRule rule = rules.get(0);
                    SecurityRuleExpression securityRuleExpression = rule.toExpression(purityThreshold);
                    SecurityRuleCheckReport report = securityRuleExpression.check(values);
                    SecurityRuleCheckStatus status;
                    if (report.getMissingAttributes().isEmpty()) {
                        status = report.isSafe() ? SecurityRuleCheckStatus.OK : SecurityRuleCheckStatus.NOK;
                    } else {
                        status = SecurityRuleCheckStatus.NA;
                    }
                    checkStatus.put(rule.getId().getSecurityIndexId().getSecurityIndexType(), status);
                }
            }

            checkStatusPerContingency.put(contingencyId, checkStatus);
        }

        return checkStatusPerContingency;
    }

}
