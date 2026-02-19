/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.dsl

import com.powsybl.commons.PowsyblException
import com.powsybl.timeseries.ast.*

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class NodeCalcGroovyExtensionModule {

    // restore default comparison behaviour
    static boolean compareToNodeCalc(Object self, Object value, String op) {
        return switch (op) {
            case ">" -> self > value
            case ">=" -> self >= value
            case "<" -> self < value
            case "<=" -> self <= value
            case "==" -> self == value
            case "!=" -> self != value
            default -> throw new PowsyblException("Unexpected operator: " + op)
        }
    }

    private static NodeCalc createComparisonNode(NodeCalc left, NodeCalc right, String op) {
        return switch (op) {
            case ">" -> BinaryOperation.greaterThan(left, right)
            case ">=" -> BinaryOperation.greaterThanOrEqualsTo(left, right)
            case "<" -> BinaryOperation.lessThan(left, right)
            case "<=" -> BinaryOperation.lessThanOrEqualsTo(left, right)
            case "==" -> BinaryOperation.equals(left, right)
            case "!=" -> BinaryOperation.notEquals(left, right)
            default -> throw new PowsyblException("Unexpected operator: " + op)
        }
    }

    // comparison
    static NodeCalc compareToNodeCalc(NodeCalc self, NodeCalc value, String op) {
        createComparisonNode(self, value, op)
    }

    static NodeCalc compareToNodeCalc(NodeCalc self, Integer value, String op) {
        createComparisonNode(self, new IntegerNodeCalc(value), op)
    }

    static NodeCalc compareToNodeCalc(Integer self, NodeCalc value, String op) {
        createComparisonNode(new IntegerNodeCalc(self), value, op)
    }

    static NodeCalc compareToNodeCalc(NodeCalc self, Float value, String op) {
        createComparisonNode(self, new FloatNodeCalc(value), op)
    }

    static NodeCalc compareToNodeCalc(Float self, NodeCalc value, String op) {
        createComparisonNode(new FloatNodeCalc(self), value, op)
    }

    static NodeCalc compareToNodeCalc(NodeCalc self, Double value, String op) {
        createComparisonNode(self, new DoubleNodeCalc(value), op)
    }
    static NodeCalc compareToNodeCalc(Double self, NodeCalc value, String op) {
        createComparisonNode(new DoubleNodeCalc(self), value, op)
    }

    static NodeCalc compareToNodeCalc(NodeCalc self, BigDecimal value, String op) {
        createComparisonNode(self, new BigDecimalNodeCalc(value), op)
    }

    static NodeCalc compareToNodeCalc(BigDecimal self, NodeCalc value, String op) {
        createComparisonNode(new BigDecimalNodeCalc(self), value, op)
    }

    // plus
    static NodeCalc plus(NodeCalc self, BigDecimal value) {
        BinaryOperation.plus(self, new BigDecimalNodeCalc(value))
    }

    static NodeCalc plus(BigDecimal self, NodeCalc value) {
        BinaryOperation.plus(new BigDecimalNodeCalc(self), value)
    }

    static NodeCalc plus(NodeCalc self, Integer value) {
        BinaryOperation.plus(self, new IntegerNodeCalc(value))
    }

    static NodeCalc plus(Integer self, NodeCalc value) {
        BinaryOperation.plus(new IntegerNodeCalc(self), value)
    }

    static NodeCalc plus(NodeCalc self, Float value) {
        BinaryOperation.plus(self, new FloatNodeCalc(value))
    }

    static NodeCalc plus(Float self, NodeCalc value) {
        BinaryOperation.plus(new FloatNodeCalc(self), value)
    }

    static NodeCalc plus(NodeCalc self, Double value) {
        BinaryOperation.plus(self, new DoubleNodeCalc(value))
    }

    static NodeCalc plus(Double self, NodeCalc value) {
        BinaryOperation.plus(new DoubleNodeCalc(self), value)
    }

    static NodeCalc plus(NodeCalc self, NodeCalc value) {
        BinaryOperation.plus(self, value)
    }

    // minus
    static NodeCalc minus(NodeCalc self, BigDecimal value) {
        BinaryOperation.minus(self, new BigDecimalNodeCalc(value))
    }

    static NodeCalc minus(BigDecimal self, NodeCalc value) {
        BinaryOperation.minus(new BigDecimalNodeCalc(self), value)
    }

    static NodeCalc minus(NodeCalc self, Integer value) {
        BinaryOperation.minus(self, new IntegerNodeCalc(value))
    }

    static NodeCalc minus(Integer self, NodeCalc value) {
        BinaryOperation.minus(new IntegerNodeCalc(self), value)
    }

    static NodeCalc minus(NodeCalc self, Float value) {
        BinaryOperation.minus(self, new FloatNodeCalc(value))
    }

    static NodeCalc minus(Float self, NodeCalc value) {
        BinaryOperation.minus(new FloatNodeCalc(self), value)
    }

    static NodeCalc minus(NodeCalc self, Double value) {
        BinaryOperation.minus(self, new DoubleNodeCalc(value))
    }

    static NodeCalc minus(Double self, NodeCalc value) {
        BinaryOperation.minus(new DoubleNodeCalc(self), value)
    }

    static NodeCalc minus(NodeCalc self, NodeCalc value) {
        BinaryOperation.minus(self, value)
    }

    // multiply
    static NodeCalc multiply(NodeCalc self, BigDecimal value) {
        BinaryOperation.multiply(self, new BigDecimalNodeCalc(value))
    }

    static NodeCalc multiply(BigDecimal self, NodeCalc value) {
        BinaryOperation.multiply(new BigDecimalNodeCalc(self), value)
    }

    static NodeCalc multiply(NodeCalc self, Integer value) {
        BinaryOperation.multiply(self, new IntegerNodeCalc(value))
    }

    static NodeCalc multiply(Integer self, NodeCalc value) {
        BinaryOperation.multiply(new IntegerNodeCalc(self), value)
    }

    static NodeCalc multiply(NodeCalc self, Float value) {
        BinaryOperation.multiply(self, new FloatNodeCalc(value))
    }

    static NodeCalc multiply(Float self, NodeCalc value) {
        BinaryOperation.multiply(new FloatNodeCalc(self), value)
    }

    static NodeCalc multiply(NodeCalc self, Double value) {
        BinaryOperation.multiply(self, new DoubleNodeCalc(value))
    }

    static NodeCalc multiply(Double self, NodeCalc value) {
        BinaryOperation.multiply(new DoubleNodeCalc(self), value)
    }

    static NodeCalc multiply(NodeCalc self, NodeCalc value) {
        BinaryOperation.multiply(self, value)
    }

    // divide
    static NodeCalc div(NodeCalc self, BigDecimal value) {
        BinaryOperation.div(self, new BigDecimalNodeCalc(value))
    }

    static NodeCalc div(BigDecimal self, NodeCalc value) {
        BinaryOperation.div(new BigDecimalNodeCalc(self), value)
    }

    static NodeCalc div(NodeCalc self, Integer value) {
        BinaryOperation.div(self, new IntegerNodeCalc(value))
    }

    static NodeCalc div(Integer self, NodeCalc value) {
        BinaryOperation.div(new IntegerNodeCalc(self), value)
    }

    static NodeCalc div(NodeCalc self, Float value) {
        BinaryOperation.div(self, new FloatNodeCalc(value))
    }

    static NodeCalc div(Float self, NodeCalc value) {
        BinaryOperation.div(new FloatNodeCalc(self), value)
    }

    static NodeCalc div(NodeCalc self, Double value) {
        BinaryOperation.div(self, new DoubleNodeCalc(value))
    }

    static NodeCalc div(Double self, NodeCalc value) {
        BinaryOperation.div(new DoubleNodeCalc(self), value)
    }

    static NodeCalc div(NodeCalc self, NodeCalc value) {
        BinaryOperation.div(self, value)
    }

    // unary
    static NodeCalc abs(NodeCalc self) {
        UnaryOperation.abs(self)
    }

    static NodeCalc negative(NodeCalc self) {
        UnaryOperation.negative(self)
    }

    static NodeCalc positive(NodeCalc self) {
        UnaryOperation.positive(self)
    }

    static NodeCalc time(NodeCalc self) {
        new TimeNodeCalc(self)
    }

    // min
    static NodeCalc min(NodeCalc self, Integer value) {
        new MinNodeCalc(self, value.doubleValue())
    }

    static NodeCalc min(NodeCalc self, Float value) {
        new MinNodeCalc(self, value.doubleValue())
    }

    static NodeCalc min(NodeCalc self, Double value) {
        new MinNodeCalc(self, value)
    }

    static NodeCalc min(NodeCalc self, BigDecimal value) {
        new MinNodeCalc(self, value.doubleValue())
    }

    // max
    static NodeCalc max(NodeCalc self, Integer value) {
        new MaxNodeCalc(self, value.doubleValue())
    }

    static NodeCalc max(NodeCalc self, Float value) {
        new MaxNodeCalc(self, value.doubleValue())
    }

    static NodeCalc max(NodeCalc self, Double value) {
        new MaxNodeCalc(self, value)
    }

    static NodeCalc max(NodeCalc self, BigDecimal value) {
        new MaxNodeCalc(self, value.doubleValue())
    }
}
