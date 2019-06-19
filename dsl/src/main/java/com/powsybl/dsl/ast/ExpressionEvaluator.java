/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dsl.ast;

import com.powsybl.commons.PowsyblException;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExpressionEvaluator extends DefaultExpressionVisitor<Object, Void> {

    public static Object evaluate(ExpressionNode node) {
        return node.accept(new ExpressionEvaluator(), null);
    }

    @Override
    public Object visitLiteral(AbstractLiteralNode node, Void arg) {
        return node.getValue();
    }

    @Override
    public Object visitComparisonOperator(ComparisonOperatorNode node, Void arg) {
        Object result1 = node.getLeft().accept(this, arg);
        Object result2 = node.getRight().accept(this, arg);
        if (!(result1 instanceof Number)) {
            throw new PowsyblException("Left operand of comparison should return a number");
        }
        if (!(result2 instanceof Number)) {
            throw new PowsyblException("Right operand of comparison should return a number");
        }
        double value1 = ((Number) result1).doubleValue();
        double value2 = ((Number) result2).doubleValue();
        switch (node.getOperator()) {
            case EQUALS:
                return Objects.equals(value1, value2);
            case NOT_EQUALS:
                return !Objects.equals(value1, value2);
            case GREATER_THAN:
                return value1 > value2;
            case LESS_THAN:
                return value1 < value2;
            case GREATER_THAN_OR_EQUALS_TO:
                return value1 >= value2;
            case LESS_THAN_OR_EQUALS_TO:
                return value1 <= value2;
            default:
                throw createUnexpectedOperatorException(node.getOperator().name());
        }
    }

    @Override
    public Object visitNotOperator(LogicalNotOperator node, Void arg) {
        Object result = node.getChild().accept(this, arg);
        if (!(result instanceof Boolean)) {
            throw new PowsyblException("Operand of not operator should return a boolean");
        }
        return !(Boolean) result;
    }

    @Override
    public Object visitLogicalOperator(LogicalBinaryOperatorNode node, Void arg) {
        Object result1 = node.getLeft().accept(this, arg);
        if (!(result1 instanceof Boolean)) {
            throw new PowsyblException("Left operand of comparison should return a boolean");
        }

        boolean value1 = (Boolean) result1;
        switch (node.getOperator()) {
            case AND:
                if (!value1) {
                    return false;
                }
                break;
            case OR:
                if (value1) {
                    return true;
                }
                break;
            default:
                throw createUnexpectedOperatorException(node.getOperator().name());
        }

        Object result2 = node.getRight().accept(this, arg);
        if (!(result2 instanceof Boolean)) {
            throw new PowsyblException("Right operand of comparison should return a boolean");
        }
        return result2;
    }

    @Override
    public Object visitArithmeticOperator(ArithmeticBinaryOperatorNode node, Void arg) {
        Object result1 = node.getLeft().accept(this, arg);
        Object result2 = node.getRight().accept(this, arg);
        if (!(result1 instanceof Number)) {
            throw new PowsyblException("Left operand of arithmetic operation should return a number (" + result1.getClass() + ")");
        }
        if (!(result2 instanceof Number)) {
            throw new PowsyblException("Right operand of arithmetic operation should return a number (" + result2.getClass() + ")");
        }
        double value1 = ((Number) result1).doubleValue();
        double value2 = ((Number) result2).doubleValue();
        switch (node.getOperator()) {
            case PLUS:
                return value1 + value2;
            case MINUS:
                return value1 - value2;
            case MULTIPLY:
                return value1 * value2;
            case DIVIDE:
                return value1 / value2;
            default:
                throw createUnexpectedOperatorException(node.getOperator().name());
        }
    }

    private static AssertionError createUnexpectedOperatorException(String operatorName) {
        return new AssertionError("Unexpected operator: " + operatorName);
    }
}
