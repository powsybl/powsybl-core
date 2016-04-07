/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.rulesdb.fs;

import eu.itesla_project.modules.rules.RuleId;
import eu.itesla_project.modules.rules.SecurityRule;
import eu.itesla_project.modules.rules.SecurityRuleExpression;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityRuleMock implements SecurityRule {

    private final RuleId ruleId;

    private final String workflowId;

    public SecurityRuleMock(RuleId ruleId, String workflowId) {
        this.ruleId = ruleId;
        this.workflowId = workflowId;
    }

    @Override
    public RuleId getId() {
        return ruleId;
    }

    @Override
    public String getWorkflowId() {
        return workflowId;
    }

    @Override
    public SecurityRuleExpression toExpression() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SecurityRuleExpression toExpression(double purityThreshold) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId, workflowId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SecurityRuleMock) {
            SecurityRuleMock other = (SecurityRuleMock) obj;
            return other.getId().equals(ruleId) && other.getWorkflowId().equals(workflowId);
        }
        return false;
    }
}
