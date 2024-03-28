/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import com.powsybl.timeseries.DoubleMultiPoint;
import org.apache.commons.lang3.tuple.Pair;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NodeCalcEvaluator implements NodeCalcVisitor<Double, NodeCalcEvaluator.EvalContext> {

    class EvalContext {
        DoubleMultiPoint multiPoint;
        Map<NodeCalc, Double> cache;

        EvalContext(DoubleMultiPoint point, Map<NodeCalc, Double> cache) {
            this.multiPoint = point;
            this.cache = cache;
        }
    }

    public static double eval(NodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return new NodeCalcEvaluator().evaluateWithCache(nodeCalc, multiPoint);
    }

    private double evaluateWithCache(NodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        EvalContext evalContext = new EvalContext(multiPoint, new IdentityHashMap<>());
        return nodeCalc.accept(this, evalContext, 0);
    }

    @Override
    public Double visit(IntegerNodeCalc nodeCalc, EvalContext evalContext) {
        return nodeCalc.toDouble();
    }

    @Override
    public Double visit(FloatNodeCalc nodeCalc, EvalContext evalContext) {
        return nodeCalc.toDouble();
    }

    @Override
    public Double visit(DoubleNodeCalc nodeCalc, EvalContext evalContext) {
        return nodeCalc.getValue();
    }

    @Override
    public Double visit(BigDecimalNodeCalc nodeCalc, EvalContext evalContext) {
        return nodeCalc.toDouble();
    }

    @Override
    public Double visit(BinaryOperation nodeCalc, EvalContext evalContext, Double left, Double right) {
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
    public Double visit(UnaryOperation nodeCalc, EvalContext evalContext, Double child) {
        double childValue = child;
        return switch (nodeCalc.getOperator()) {
            case ABS -> Math.abs(childValue);
            case NEGATIVE -> -childValue;
            case POSITIVE -> childValue;
        };
    }

    @Override
    public NodeCalc iterate(UnaryOperation nodeCalc, EvalContext evalContext) {
        return nodeCalc.getChild();
    }

    @Override
    public Double visit(MinNodeCalc nodeCalc, EvalContext evalContext, Double child) {
        double childValue = child;
        return Math.min(childValue, nodeCalc.getMin());
    }

    @Override
    public NodeCalc iterate(MinNodeCalc nodeCalc, EvalContext evalContext) {
        return nodeCalc.getChild();
    }

    @Override
    public Double visit(MaxNodeCalc nodeCalc, EvalContext evalContext, Double child) {
        double childValue = child;
        return Math.max(childValue, nodeCalc.getMax());
    }

    @Override
    public NodeCalc iterate(MaxNodeCalc nodeCalc, EvalContext evalContext) {
        return nodeCalc.getChild();
    }

    @Override
    public Double visit(CachedNodeCalc nodeCalc, EvalContext evalContext, Double child) {
        double childValue;
        if (child == null) {
            childValue = evalContext.cache.get(nodeCalc);
        } else {
            childValue = child;
            evalContext.cache.put(nodeCalc, childValue);
        }
        return childValue;
    }

    @Override
    public NodeCalc iterate(CachedNodeCalc nodeCalc, EvalContext evalContext) {
        return evalContext.cache.containsKey(nodeCalc) ? null : nodeCalc.getChild();
    }

    @Override
    public Double visit(TimeNodeCalc nodeCalc, EvalContext evalContext, Double child) {
        return (double) (evalContext.multiPoint).getTime();
    }

    @Override
    public NodeCalc iterate(TimeNodeCalc nodeCalc, EvalContext evalContext) {
        return null;
    }

    @Override
    public Double visit(TimeSeriesNumNodeCalc nodeCalc, EvalContext evalContext) {
        if (evalContext.multiPoint == null) {
            throw new IllegalStateException("Multi point is null");
        }
        return evalContext.multiPoint.getValue(nodeCalc.getTimeSeriesNum());
    }

    @Override
    public Double visit(TimeSeriesNameNodeCalc nodeCalc, EvalContext evalContext) {
        throw new IllegalStateException("NodeCalc should have been resolved before");
    }

    @Override
    public Double visit(BinaryMinCalc nodeCalc, EvalContext evalContext, Double left, Double right) {
        double leftValue = left;
        double rightValue = right;
        return Math.min(leftValue, rightValue);
    }

    @Override
    public Double visit(BinaryMaxCalc nodeCalc, EvalContext evalContext, Double left, Double right) {
        double leftValue = left;
        double rightValue = right;
        return Math.max(leftValue, rightValue);
    }

    @Override
    public Pair<NodeCalc, NodeCalc> iterate(AbstractBinaryNodeCalc nodeCalc, EvalContext evalContext) {
        return Pair.of(nodeCalc.getLeft(), nodeCalc.getRight());
    }
}
