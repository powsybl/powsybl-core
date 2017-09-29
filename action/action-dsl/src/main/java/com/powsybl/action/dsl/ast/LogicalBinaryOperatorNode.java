/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LogicalBinaryOperatorNode extends AbstractBinaryOperatorNode {

    private final LogicalBinaryOperator operator;

    public LogicalBinaryOperatorNode(ExpressionNode left, ExpressionNode right, LogicalBinaryOperator operator) {
        super(left, right);
        this.operator = Objects.requireNonNull(operator);
    }

    public LogicalBinaryOperator getOperator() {
        return operator;
    }

    @Override
    public <R, A> R accept(ExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visitLogicalOperator(this, arg);
    }
}
