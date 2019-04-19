/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl;

import com.powsybl.contingency.Contingency;
import com.powsybl.dsl.ast.ExpressionNode;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.security.dsl.ast.LimitsEvaluationContext;
import com.powsybl.security.dsl.ast.LimitsExpressionEvaluator;

import java.util.Objects;

/**
 * Implementation of {@link LimitMatcher} which relies on an underlying condition
 * described as an {@link ExpressionNode}, which may be created from a groovy DSL.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
class ExpressionLimitMatcher implements LimitMatcher {

    private final ExpressionNode expression;

    public ExpressionLimitMatcher(ExpressionNode expression) {
        this.expression = Objects.requireNonNull(expression);
    }

    public boolean matches(Branch branch, Branch.Side side, CurrentLimits.TemporaryLimit limit, Contingency contingency) {
        return LimitsExpressionEvaluator.evaluate(expression, new LimitsEvaluationContext(contingency, branch, side, limit)).equals(Boolean.TRUE);
    }
}
