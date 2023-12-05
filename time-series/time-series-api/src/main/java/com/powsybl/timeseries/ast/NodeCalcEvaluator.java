/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

import com.powsybl.timeseries.DoubleMultiPoint;

import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NodeCalcEvaluator implements NodeCalcVisitor<Double, DoubleMultiPoint> {

    public static double eval(NodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return nodeCalc.accept(new NodeCalcEvaluator(), multiPoint, 0);
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
    public Double visit(BinaryOperation nodeCalc, DoubleMultiPoint multiPoint, Double left, Double right) {
        double leftValue = left;
        double rightValue = right;
        return switch (nodeCalc.getOperator()) {
            case PLUS -> leftValue + rightValue;
            case MINUS -> leftValue - rightValue;
            case MULTIPLY -> leftValue * rightValue;
            case DIVIDE -> leftValue / rightValue;
            case LESS_THAN -> leftValue < rightValue ? 1d : 0d;
            case LESS_THAN_OR_EQUALS_TO -> leftValue <= rightValue ? 1d : 0d;
            case GREATER_THAN -> leftValue > rightValue ? 1d : 0d;
            case GREATER_THAN_OR_EQUALS_TO -> leftValue >= rightValue ? 1d : 0d;
            case EQUALS -> leftValue == rightValue ? 1d : 0d;
            case NOT_EQUALS -> leftValue != rightValue ? 1d : 0d;
        };
    }

    @Override
    public Pair<NodeCalc, NodeCalc> iterate(BinaryOperation nodeCalc, DoubleMultiPoint multiPoint) {
        return Pair.of(nodeCalc.getLeft(), nodeCalc.getRight());
    }

    @Override
    public Double visit(UnaryOperation nodeCalc, DoubleMultiPoint multiPoint, Double child) {
        double childValue = child;
        return switch (nodeCalc.getOperator()) {
            case ABS -> Math.abs(childValue);
            case NEGATIVE -> -childValue;
            case POSITIVE -> childValue;
        };
    }

    @Override
    public NodeCalc iterate(UnaryOperation nodeCalc, DoubleMultiPoint multiPoint) {
        return nodeCalc.getChild();
    }

    @Override
    public Double visit(MinNodeCalc nodeCalc, DoubleMultiPoint multiPoint, Double child) {
        double childValue = child;
        return Math.min(childValue, nodeCalc.getMin());
    }

    @Override
    public NodeCalc iterate(MinNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return nodeCalc.getChild();
    }

    @Override
    public Double visit(MaxNodeCalc nodeCalc, DoubleMultiPoint multiPoint, Double child) {
        double childValue = child;
        return Math.max(childValue, nodeCalc.getMax());
    }

    @Override
    public NodeCalc iterate(MaxNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return nodeCalc.getChild();
    }

    @Override
    public Double visit(TimeNodeCalc nodeCalc, DoubleMultiPoint multiPoint, Double child) {
        return (double) multiPoint.getTime();
    }

    @Override
    public NodeCalc iterate(TimeNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return null;
    }

    @Override
    public Double visit(TimeSeriesNumNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        if (multiPoint == null) {
            throw new IllegalStateException("Multi point is null");
        }
        return multiPoint.getValue(nodeCalc.getTimeSeriesNum());
    }

    @Override
    public Double visit(TimeSeriesNameNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        throw new IllegalStateException("NodeCalc should have been resolved before");
    }

    @Override
    public Double visit(BinaryMinCalc nodeCalc, DoubleMultiPoint multiPoint, Double left, Double right) {
        double leftValue = left;
        double rightValue = right;
        return Math.min(leftValue, rightValue);
    }

    @Override
    public Pair<NodeCalc, NodeCalc> iterate(BinaryMinCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return Pair.of(nodeCalc.getLeft(), nodeCalc.getRight());
    }

    @Override
    public Double visit(BinaryMaxCalc nodeCalc, DoubleMultiPoint multiPoint, Double left, Double right) {
        double leftValue = left;
        double rightValue = right;
        return Math.max(leftValue, rightValue);
    }

    @Override
    public Pair<NodeCalc, NodeCalc> iterate(BinaryMaxCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return Pair.of(nodeCalc.getLeft(), nodeCalc.getRight());
    }
}
