/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl.ast;

import com.powsybl.dsl.DslException;
import com.powsybl.dsl.ast.ExpressionEvaluator;
import com.powsybl.dsl.ast.ExpressionNode;

import java.util.Objects;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class LimitsExpressionEvaluator extends ExpressionEvaluator implements LimitsExpressionVisitor<Object, Void> {

    private final LimitsEvaluationContext context;

    public LimitsExpressionEvaluator(LimitsEvaluationContext context) {
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public Object visitVariable(LimitsVariableNode node, Void arg) {
        switch (node.getType()) {
            case VOLTAGE:
                return context.getBranch().getTerminal(context.getSide()).getVoltageLevel().getNominalV();
            case DURATION:
                if (context.getLimit() == null) {
                    return null;
                }
                return context.getLimit().getAcceptableDuration();
            default:
                throw new DslException("Unknown variable type: " + node.getType());
        }
    }

    public static Object evaluate(ExpressionNode node, LimitsEvaluationContext context) {
        return node.accept(new LimitsExpressionEvaluator(context), null);
    }

}
