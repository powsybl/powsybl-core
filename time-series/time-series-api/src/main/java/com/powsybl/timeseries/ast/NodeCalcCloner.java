/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NodeCalcCloner<A> implements NodeCalcVisitor<NodeCalc, A> {
    @Override
    public NodeCalc visit(IntegerNodeCalc nodeCalc, A arg) {
        return new IntegerNodeCalc(nodeCalc.getValue());
    }

    @Override
    public NodeCalc visit(IntegerNodeCalc nodeCalc, A arg, int depth) {
        return new IntegerNodeCalc(nodeCalc.getValue());
    }

    @Override
    public NodeCalc visit(FloatNodeCalc nodeCalc, A arg) {
        return new FloatNodeCalc(nodeCalc.getValue());
    }

    @Override
    public NodeCalc visit(FloatNodeCalc nodeCalc, A arg, int depth) {
        return new FloatNodeCalc(nodeCalc.getValue());
    }

    @Override
    public NodeCalc visit(DoubleNodeCalc nodeCalc, A arg) {
        return new DoubleNodeCalc(nodeCalc.getValue());
    }

    @Override
    public NodeCalc visit(DoubleNodeCalc nodeCalc, A arg, int depth) {
        return new DoubleNodeCalc(nodeCalc.getValue());
    }

    @Override
    public NodeCalc visit(BigDecimalNodeCalc nodeCalc, A arg) {
        return new BigDecimalNodeCalc(nodeCalc.getValue());
    }

    @Override
    public NodeCalc visit(BigDecimalNodeCalc nodeCalc, A arg, int depth) {
        return new BigDecimalNodeCalc(nodeCalc.getValue());
    }

    @Override
    public NodeCalc visit(BinaryOperation nodeCalc, A arg, NodeCalc left, NodeCalc right) {
        return new BinaryOperation(left, right, nodeCalc.getOperator());
    }

    @Override
    public NodeCalc visit(BinaryOperation nodeCalc, A arg, int depth) {
        if (depth < NodeCalcVisitors.RECURSION_THRESHOLD) {
            NodeCalc left = nodeCalc.getLeft().accept(this, arg, depth + 1);
            NodeCalc right = nodeCalc.getRight().accept(this, arg, depth + 1);
            return new BinaryOperation(left, right, nodeCalc.getOperator());
        } else {
            return NodeCalcVisitors.visit(nodeCalc, arg, this);
        }
    }

    @Override
    public NodeCalc visit(BinaryMinCalc nodeCalc, A arg, NodeCalc left, NodeCalc right) {
        return new BinaryMinCalc(left, right);
    }

    @Override
    public NodeCalc visit(BinaryMinCalc nodeCalc, A arg, int depth) {
        if (depth < NodeCalcVisitors.RECURSION_THRESHOLD) {
            NodeCalc left = nodeCalc.getLeft().accept(this, arg, depth + 1);
            NodeCalc right = nodeCalc.getRight().accept(this, arg, depth + 1);
            return new BinaryMinCalc(left, right);
        } else {
            return NodeCalcVisitors.visit(nodeCalc, arg, this);
        }
    }

    @Override
    public NodeCalc visit(BinaryMaxCalc nodeCalc, A arg, NodeCalc left, NodeCalc right) {
        return new BinaryMaxCalc(left, right);
    }

    @Override
    public NodeCalc visit(BinaryMaxCalc nodeCalc, A arg, int depth) {
        if (depth < NodeCalcVisitors.RECURSION_THRESHOLD) {
            NodeCalc left = nodeCalc.getLeft().accept(this, arg, depth + 1);
            NodeCalc right = nodeCalc.getRight().accept(this, arg, depth + 1);
            return new BinaryMaxCalc(left, right);
        } else {
            return NodeCalcVisitors.visit(nodeCalc, arg, this);
        }
    }

    @Override
    public Pair<NodeCalc, NodeCalc> iterate(AbstractBinaryNodeCalc nodeCalc, A arg) {
        return Pair.of(nodeCalc.getLeft(), nodeCalc.getRight());
    }

    @Override
    public NodeCalc visit(UnaryOperation nodeCalc, A arg, NodeCalc child) {
        return new UnaryOperation(child, nodeCalc.getOperator());
    }

    @Override
    public NodeCalc visit(UnaryOperation nodeCalc, A arg, int depth) {
        if (depth < NodeCalcVisitors.RECURSION_THRESHOLD) {
            NodeCalc child = nodeCalc.getChild().accept(this, arg, depth + 1);
            return new UnaryOperation(child, nodeCalc.getOperator());
        } else {
            return NodeCalcVisitors.visit(nodeCalc, arg, this);
        }
    }

    @Override
    public NodeCalc iterate(UnaryOperation nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc visit(MinNodeCalc nodeCalc, A arg, NodeCalc child) {
        return new MinNodeCalc(child, nodeCalc.getMin());
    }

    @Override
    public NodeCalc visit(MinNodeCalc nodeCalc, A arg, int depth) {
        if (depth < NodeCalcVisitors.RECURSION_THRESHOLD) {
            NodeCalc child = nodeCalc.getChild().accept(this, arg, depth + 1);
            return new MinNodeCalc(child, nodeCalc.getMin());
        } else {
            return NodeCalcVisitors.visit(nodeCalc, arg, this);
        }
    }

    @Override
    public NodeCalc iterate(MinNodeCalc nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc visit(MaxNodeCalc nodeCalc, A arg, NodeCalc child) {
        return new MaxNodeCalc(child, nodeCalc.getMax());
    }

    @Override
    public NodeCalc visit(MaxNodeCalc nodeCalc, A arg, int depth) {
        if (depth < NodeCalcVisitors.RECURSION_THRESHOLD) {
            NodeCalc child = nodeCalc.getChild().accept(this, arg, depth + 1);
            return new MaxNodeCalc(child, nodeCalc.getMax());
        } else {
            return NodeCalcVisitors.visit(nodeCalc, arg, this);
        }
    }

    @Override
    public NodeCalc iterate(MaxNodeCalc nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc visit(TimeNodeCalc nodeCalc, A arg, NodeCalc child) {
        return new TimeNodeCalc(child);
    }

    @Override
    public NodeCalc visit(TimeNodeCalc nodeCalc, A arg, int depth) {
        if (depth < NodeCalcVisitors.RECURSION_THRESHOLD) {
            NodeCalc child = nodeCalc.getChild().accept(this, arg, depth + 1);
            return new TimeNodeCalc(child);
        } else {
            return NodeCalcVisitors.visit(nodeCalc, arg, this);
        }
    }

    @Override
    public NodeCalc iterate(TimeNodeCalc nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc visit(TimeSeriesNameNodeCalc nodeCalc, A arg) {
        return new TimeSeriesNameNodeCalc(nodeCalc.getTimeSeriesName());
    }

    @Override
    public NodeCalc visit(TimeSeriesNameNodeCalc nodeCalc, A arg, int depth) {
        return new TimeSeriesNameNodeCalc(nodeCalc.getTimeSeriesName());
    }

    @Override
    public NodeCalc visit(TimeSeriesNumNodeCalc nodeCalc, A arg) {
        return new TimeSeriesNumNodeCalc(nodeCalc.getTimeSeriesNum());
    }

    @Override
    public NodeCalc visit(TimeSeriesNumNodeCalc nodeCalc, A arg, int depth) {
        return new TimeSeriesNumNodeCalc(nodeCalc.getTimeSeriesNum());
    }
}
