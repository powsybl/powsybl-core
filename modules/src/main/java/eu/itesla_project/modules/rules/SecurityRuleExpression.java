/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules;

import eu.itesla_project.modules.histo.HistoDbAttributeId;
import eu.itesla_project.modules.rules.expr.ExpressionEvaluator;
import eu.itesla_project.modules.rules.expr.ExpressionNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityRuleExpression implements Serializable {

    private final RuleId ruleId;

    private final SecurityRuleStatus status;

    private final ExpressionNode condition;

    public SecurityRuleExpression(RuleId ruleId, SecurityRuleStatus status, ExpressionNode condition) {
        Objects.requireNonNull(ruleId);
        Objects.requireNonNull(status);
        this.ruleId = ruleId;
        this.status = status;
        this.condition = condition;
    }

    public RuleId getRuleId() {
        return ruleId;
    }

    public SecurityRuleStatus getStatus() {
        return status;
    }

    public SecurityRuleCheckReport check(Map<HistoDbAttributeId, Object> values) {
        List<HistoDbAttributeId> missingAttributes = new ArrayList<>();
        boolean safe;
        switch (status) {
            case ALWAYS_SECURE:
                safe = true;
                break;
            case ALWAYS_UNSECURE:
                safe = false;
                break;
            case SECURE_IF:
                safe = ExpressionEvaluator.eval(condition, values, missingAttributes);
                break;
            default:
                throw new AssertionError(status.name());
        }
        return new SecurityRuleCheckReport(safe, missingAttributes);
    }

    public ExpressionNode getCondition() {
        return condition;
    }

}
