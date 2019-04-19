/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dsl

import com.powsybl.commons.PowsyblException
import com.powsybl.dsl.ast.*
import org.codehaus.groovy.control.CompilationFailedException

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ExpressionDslLoader extends DslLoader {

    ExpressionDslLoader(GroovyCodeSource dslSrc) {
        super(dslSrc)
    }

    ExpressionDslLoader(File dslFile) {
        super(dslFile)
    }

    ExpressionDslLoader(String script) {
        super(script)
    }

    static void prepareClosures(Binding binding) {

        // operator overloading
        for (op in [['plus', 'PLUS'],
                    ['minus', 'MINUS'],
                    ['multiply', 'MULTIPLY'],
                    ['div', 'DIVIDE']]) {
            def op0 = op[0]
            def op1 = op[1]

            // integer
            ExpressionNode.metaClass."$op0" = { Integer value ->
                ExpressionHelper.newArithmeticBinaryOperator(delegate, ExpressionHelper.newIntegerLiteral(value), ArithmeticBinaryOperator."$op1")
            }

            Integer.metaClass."$op0" = { ExpressionNode value ->
                ExpressionHelper.newArithmeticBinaryOperator(ExpressionHelper.newIntegerLiteral(delegate), value, ArithmeticBinaryOperator."$op1")
            }

            // float
            ExpressionNode.metaClass."$op0" = { Float value ->
                ExpressionHelper.newArithmeticBinaryOperator(delegate, ExpressionHelper.newFloatLiteral(value), ArithmeticBinaryOperator."$op1")
            }

            Float.metaClass."$op0" = { ExpressionNode value ->
                ExpressionHelper.newArithmeticBinaryOperator(ExpressionHelper.newFloatLiteral(delegate), value, ArithmeticBinaryOperator."$op1")
            }

            // double
            ExpressionNode.metaClass."$op0" = { Double value ->
                ExpressionHelper.newArithmeticBinaryOperator(delegate, ExpressionHelper.newDoubleLiteral(value), ArithmeticBinaryOperator."$op1")
            }

            Double.metaClass."$op0" = { ExpressionNode value ->
                ExpressionHelper.newArithmeticBinaryOperator(ExpressionHelper.newDoubleLiteral(delegate), value, ArithmeticBinaryOperator."$op1")
            }

            // big decimal
            ExpressionNode.metaClass."$op0" = { BigDecimal value ->
                ExpressionHelper.newArithmeticBinaryOperator(delegate, ExpressionHelper.newBigDecimalLiteral(value), ArithmeticBinaryOperator."$op1")
            }

            BigDecimal.metaClass."$op0" = { ExpressionNode value ->
                ExpressionHelper.newArithmeticBinaryOperator(ExpressionHelper.newBigDecimalLiteral(delegate), value, ArithmeticBinaryOperator."$op1")
            }

        }

        // comparison

        Object.metaClass.compareTo2 = { Object value, String op ->
            switch (op) {
                case ">":
                    return delegate > value
                case ">=":
                    return delegate >= value
                case "<":
                    return delegate < value
                case "<=":
                    return delegate <= value
                case "==":
                    return delegate == value
                case "!=":
                    return delegate != value
                default:
                    throw createUnexpectedOperatorException(op)
            }
        }

        ExpressionNode.metaClass.compareTo2 = { ExpressionNode value, String op ->
            nodeCompareToNode(delegate, value, op)
        }
        ExpressionNode.metaClass.compareTo2 = { Integer value, String op ->
            nodeCompareToNode(delegate, ExpressionHelper.newIntegerLiteral(value), op)
        }
        Integer.metaClass.compareTo2 = { ExpressionNode value, String op ->
            nodeCompareToNode(ExpressionHelper.newIntegerLiteral(delegate), value, op)
        }
        ExpressionNode.metaClass.compareTo2 = { Float value, String op ->
            nodeCompareToNode(delegate, ExpressionHelper.newFloatLiteral(value), op)
        }
        Float.metaClass.compareTo2 = { ExpressionNode value, String op ->
            nodeCompareToNode(ExpressionHelper.newFloatLiteral(delegate), value, op)
        }
        ExpressionNode.metaClass.compareTo2 = { Double value, String op ->
            nodeCompareToNode(delegate, ExpressionHelper.newDoubleLiteral(value), op)
        }
        Double.metaClass.compareTo2 = { ExpressionNode value, String op ->
            nodeCompareToNode(ExpressionHelper.newDoubleLiteral(delegate), value, op)
        }
        ExpressionNode.metaClass.compareTo2 = { BigDecimal value, String op ->
            nodeCompareToNode(delegate, ExpressionHelper.newBigDecimalLiteral(value), op)
        }
        BigDecimal.metaClass.compareTo2 = { ExpressionNode value, String op ->
            nodeCompareToNode(ExpressionHelper.newBigDecimalLiteral(delegate), value, op)
        }

        // boolean
        Boolean.metaClass.and2 = { Boolean value ->
            delegate && value
        }
        ExpressionNode.metaClass.and2 = { Boolean value ->
            ExpressionHelper.newLogicalBinaryOperator(delegate, ExpressionHelper.newBooleanLiteral(value), LogicalBinaryOperator.AND)
        }
        Boolean.metaClass.and2 = { ExpressionNode value ->
            ExpressionHelper.newLogicalBinaryOperator(ExpressionHelper.newBooleanLiteral(delegate), value, LogicalBinaryOperator.AND)
        }
        ExpressionNode.metaClass.and2 = { ExpressionNode value ->
            ExpressionHelper.newLogicalBinaryOperator(delegate, value, LogicalBinaryOperator.AND)
        }

        Boolean.metaClass.or2 = { Boolean value ->
            delegate || value
        }
        ExpressionNode.metaClass.or2 = { Boolean value ->
            ExpressionHelper.newLogicalBinaryOperator(delegate, ExpressionHelper.newBooleanLiteral(value), LogicalBinaryOperator.OR)
        }
        Boolean.metaClass.or2 = { ExpressionNode value ->
            ExpressionHelper.newLogicalBinaryOperator(ExpressionHelper.newBooleanLiteral(delegate), value, LogicalBinaryOperator.OR)
        }
        ExpressionNode.metaClass.or2 = { ExpressionNode value ->
            ExpressionHelper.newLogicalBinaryOperator(delegate, value, LogicalBinaryOperator.OR)
        }

        Boolean.metaClass.not = {
            !delegate
        }
        ExpressionNode.metaClass.not = {
            ExpressionHelper.newLogicalNotOperator(delegate)
        }
    }

    private static ExpressionNode nodeCompareToNode(ExpressionNode left, ExpressionNode right, String op) {
        switch (op) {
            case ">":
                return ExpressionHelper.newComparisonOperator(left, right, ComparisonOperator.GREATER_THAN)
            case ">=":
                return ExpressionHelper.newComparisonOperator(left, right, ComparisonOperator.GREATER_THAN_OR_EQUALS_TO)
            case "<":
                return ExpressionHelper.newComparisonOperator(left, right, ComparisonOperator.LESS_THAN)
            case "<=":
                return ExpressionHelper.newComparisonOperator(left, right, ComparisonOperator.LESS_THAN_OR_EQUALS_TO)
            case "==":
                return ExpressionHelper.newComparisonOperator(left, right, ComparisonOperator.EQUALS)
            case "!=":
                return ExpressionHelper.newComparisonOperator(left, right, ComparisonOperator.NOT_EQUALS)
            default:
                throw createUnexpectedOperatorException(op)
        }
    }

    private static AssertionError createUnexpectedOperatorException(String operator) {
        return new AssertionError("Unexpected operator: " + operator)
    }

    static ExpressionNode createExpressionNode(Object value) {
        if (value instanceof ExpressionNode) {
            value
        } else if (value instanceof Integer){
            ExpressionHelper.newIntegerLiteral(value)
        } else if (value instanceof Float){
            ExpressionHelper.newFloatLiteral(value)
        } else if (value instanceof Double){
            ExpressionHelper.newDoubleLiteral(value)
        } else if (value instanceof BigDecimal){
            ExpressionHelper.newBigDecimalLiteral(value)
        } else if (value instanceof Boolean) {
            ExpressionHelper.newBooleanLiteral(value)
        } else if (value instanceof String) {
            ExpressionHelper.newStringLiteral(value)
        } else {
            throw new AssertionError(value?.getClass())
        }
    }

    Object load() {
        try {
            Binding binding = new Binding()

            prepareClosures(binding)

            def shell = createShell(binding)

            def value = shell.evaluate(dslSrc)
            createExpressionNode(value)
        } catch (CompilationFailedException e) {
            throw new PowsyblException(e.getMessage(), e)
        }
    }
}
