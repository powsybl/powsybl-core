/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

import com.powsybl.timeseries.DoubleMultiPoint;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeCalcEvaluator implements NodeCalcVisitor<Double, DoubleMultiPoint> {

    public static double eval(NodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return nodeCalc.accept(new NodeCalcEvaluator(), multiPoint);
    }

    @Override
    public Double visit(IntegerNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return nodeCalc.toDouble();
    }

    @Override
    public Double visit(FloatNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return nodeCalc.toDouble();
    }

    @Override
    public Double visit(DoubleNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return nodeCalc.getValue();
    }

    @Override
    public Double visit(BigDecimalNodeCalc nodeCalc, DoubleMultiPoint arg) {
        return nodeCalc.toDouble();
    }

    @Override
    public Double visit(BinaryOperation nodeCalc, DoubleMultiPoint multiPoint) {
        double leftValue = nodeCalc.getLeft().accept(this, multiPoint);
        double rightValue = nodeCalc.getRight().accept(this, multiPoint);
        switch (nodeCalc.getOperator()) {
            case PLUS: return leftValue + rightValue;
            case MINUS: return leftValue - rightValue;
            case MULTIPLY: return leftValue * rightValue;
            case DIVIDE: return leftValue / rightValue;
            case LESS_THAN: return leftValue < rightValue ? 1d : 0d;
            case LESS_THAN_OR_EQUALS_TO: return leftValue <= rightValue ? 1d : 0d;
            case GREATER_THAN: return leftValue > rightValue ? 1d : 0d;
            case GREATER_THAN_OR_EQUALS_TO: return leftValue >= rightValue ? 1d : 0d;
            case EQUALS: return leftValue == rightValue ? 1d : 0d;
            case NOT_EQUALS: return leftValue != rightValue ? 1d : 0d;
            default: throw new AssertionError();
        }
    }

    @Override
    public Double visit(UnaryOperation nodeCalc, DoubleMultiPoint multiPoint) {
        double childValue = nodeCalc.getChild().accept(this, multiPoint);
        switch (nodeCalc.getOperator()) {
            case ABS: return Math.abs(childValue);
            case NEGATIVE: return -childValue;
            case POSITIVE: return childValue;
            default: throw new AssertionError();
        }
    }

    @Override
    public Double visit(MinNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        double childValue = nodeCalc.getChild().accept(this, multiPoint);
        return Math.min(childValue, nodeCalc.getMin());
    }

    @Override
    public Double visit(MaxNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        double childValue = nodeCalc.getChild().accept(this, multiPoint);
        return Math.max(childValue, nodeCalc.getMax());
    }

    @Override
    public Double visit(TimeNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return (double) multiPoint.getTime();
    }

    @Override
    public Double visit(TimeSeriesNumNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        if (multiPoint == null) {
            throw new AssertionError("Multi point is null");
        }
        return multiPoint.getValue(nodeCalc.getTimeSeriesNum());
    }

    @Override
    public Double visit(TimeSeriesNameNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        throw new AssertionError("NodeCalc should have been resolved before");
    }
}
