/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

import com.powsybl.timeseries.DoubleMultiPoint;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NodeCalcEvaluator implements NodeCalcVisitor<Double, DoubleMultiPoint> {

    private static final Map<NodeCalc, Double> CACHE = new HashMap<>();

    public static double eval(NodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return new NodeCalcEvaluator().evaluateWithCache(nodeCalc, multiPoint);
    }

    private double evaluateWithCache(NodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        double result = nodeCalc.accept(this, multiPoint, 0);
        invalidateCache(); // Invalider le cache après l'évaluation du nœud racine
        return result;
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
        // Check if already in cache
        Double cachedValue = CACHE.get(nodeCalc);
        if (cachedValue != null) {
            return cachedValue;
        }

        // Compute if not in cache
        double leftValue = left;
        double rightValue = right;
        double result = switch (nodeCalc.getOperator()) {
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

        // Put in the cache
        CACHE.put(nodeCalc, result);
        return result;
    }

    @Override
    public Double visit(UnaryOperation nodeCalc, DoubleMultiPoint multiPoint, Double child) {
        // Check if already in cache
        Double cachedValue = CACHE.get(nodeCalc);
        if (cachedValue != null) {
            return cachedValue;
        }

        // Compute if not in cache
        double childValue = child;
        double result = switch (nodeCalc.getOperator()) {
            case ABS -> Math.abs(childValue);
            case NEGATIVE -> -childValue;
            case POSITIVE -> childValue;
        };

        // Put in the cache
        CACHE.put(nodeCalc, result);
        return result;
    }

    @Override
    public NodeCalc iterate(UnaryOperation nodeCalc, DoubleMultiPoint multiPoint) {
        return nodeCalc.getChild();
    }

    @Override
    public Double visit(MinNodeCalc nodeCalc, DoubleMultiPoint multiPoint, Double child) {
        // Check if already in cache
        Double cachedValue = CACHE.get(nodeCalc);
        if (cachedValue != null) {
            return cachedValue;
        }

        // Compute if not in cache
        double childValue = child;
        double result = Math.min(childValue, nodeCalc.getMin());

        // Put in the cache
        CACHE.put(nodeCalc, result);
        return result;
    }

    @Override
    public NodeCalc iterate(MinNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return nodeCalc.getChild();
    }

    @Override
    public Double visit(MaxNodeCalc nodeCalc, DoubleMultiPoint multiPoint, Double child) {
        // Check if already in cache
        Double cachedValue = CACHE.get(nodeCalc);
        if (cachedValue != null) {
            return cachedValue;
        }

        // Compute if not in cache
        double childValue = child;
        double result = Math.max(childValue, nodeCalc.getMax());

        // Put in the cache
        CACHE.put(nodeCalc, result);
        return result;
    }

    @Override
    public NodeCalc iterate(MaxNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return nodeCalc.getChild();
    }

    @Override
    public Double visit(TimeNodeCalc nodeCalc, DoubleMultiPoint multiPoint, Double child) {
        // Check if already in cache
        Double cachedValue = CACHE.get(nodeCalc);
        if (cachedValue != null) {
            return cachedValue;
        }

        // Compute if not in cache
        double result = multiPoint.getTime();

        // Put in the cache
        CACHE.put(nodeCalc, result);
        return result;
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
        // Check if already in cache
        Double cachedValue = CACHE.get(nodeCalc);
        if (cachedValue != null) {
            return cachedValue;
        }

        // Compute if not in cache
        double result = multiPoint.getValue(nodeCalc.getTimeSeriesNum());

        // Put in the cache
        CACHE.put(nodeCalc, result);
        return result;
    }

    @Override
    public Double visit(TimeSeriesNameNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        throw new IllegalStateException("NodeCalc should have been resolved before");
    }

    @Override
    public Double visit(BinaryMinCalc nodeCalc, DoubleMultiPoint multiPoint, Double left, Double right) {
        // Check if already in cache
        Double cachedValue = CACHE.get(nodeCalc);
        if (cachedValue != null) {
            return cachedValue;
        }

        // Compute if not in cache
        double leftValue = left;
        double rightValue = right;
        double result = Math.min(leftValue, rightValue);

        // Put in the cache
        CACHE.put(nodeCalc, result);
        return result;
    }

    @Override
    public Double visit(BinaryMaxCalc nodeCalc, DoubleMultiPoint multiPoint, Double left, Double right) {
        // Check if already in cache
        Double cachedValue = CACHE.get(nodeCalc);
        if (cachedValue != null) {
            return cachedValue;
        }

        // Compute if not in cache
        double leftValue = left;
        double rightValue = right;
        double result = Math.max(leftValue, rightValue);

        // Put in the cache
        CACHE.put(nodeCalc, result);
        return result;
    }

    @Override
    public Pair<NodeCalc, NodeCalc> iterate(AbstractBinaryNodeCalc nodeCalc, DoubleMultiPoint multiPoint) {
        return Pair.of(nodeCalc.getLeft(), nodeCalc.getRight());
    }

    private void invalidateCache() {
        CACHE.clear();
    }
}
