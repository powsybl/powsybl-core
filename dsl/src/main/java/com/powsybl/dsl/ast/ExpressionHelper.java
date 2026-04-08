/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dsl.ast;

import java.math.BigDecimal;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class ExpressionHelper {

    private ExpressionHelper() {
    }

    public static ComparisonOperatorNode newComparisonOperator(ExpressionNode left, ExpressionNode right, ComparisonOperator operator) {
        return new ComparisonOperatorNode(left, right, operator);
    }

    public static LogicalBinaryOperatorNode newLogicalBinaryOperator(ExpressionNode left, ExpressionNode right, LogicalBinaryOperator operator) {
        return new LogicalBinaryOperatorNode(left, right, operator);
    }

    public static ArithmeticBinaryOperatorNode newArithmeticBinaryOperator(ExpressionNode left, ExpressionNode right, ArithmeticBinaryOperator operator) {
        return new ArithmeticBinaryOperatorNode(left, right, operator);
    }

    public static LogicalNotOperator newLogicalNotOperator(ExpressionNode child) {
        return new LogicalNotOperator(child);
    }

    public static FloatLiteralNode newFloatLiteral(float value) {
        return new FloatLiteralNode(value);
    }

    public static DoubleLiteralNode newDoubleLiteral(double value) {
        return new DoubleLiteralNode(value);
    }

    public static BigDecimalLiteralNode newBigDecimalLiteral(BigDecimal value) {
        return new BigDecimalLiteralNode(value);
    }

    public static IntegerLiteralNode newIntegerLiteral(int value) {
        return new IntegerLiteralNode(value);
    }

    public static BooleanLiteralNode newBooleanLiteral(boolean value) {
        return new BooleanLiteralNode(value);
    }

    public static ExpressionNode newStringLiteral(String value) {
        return new StringLiteralNode(value);
    }
}
