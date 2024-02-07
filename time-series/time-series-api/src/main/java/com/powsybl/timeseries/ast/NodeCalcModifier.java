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
public class NodeCalcModifier<A> implements NodeCalcVisitor<NodeCalc, A> {
    @Override
    public NodeCalc visit(IntegerNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public NodeCalc visit(IntegerNodeCalc nodeCalc, A arg, int depth) {
        return null;
    }

    @Override
    public NodeCalc visit(FloatNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public NodeCalc visit(FloatNodeCalc nodeCalc, A arg, int depth) {
        return null;
    }

    @Override
    public NodeCalc visit(DoubleNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public NodeCalc visit(DoubleNodeCalc nodeCalc, A arg, int depth) {
        return null;
    }

    @Override
    public NodeCalc visit(BigDecimalNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public NodeCalc visit(BigDecimalNodeCalc nodeCalc, A arg, int depth) {
        return null;
    }

    @Override
    public NodeCalc visit(BinaryOperation nodeCalc, A arg, NodeCalc left, NodeCalc right) {
        return visitBinaryNodeCalc(nodeCalc, left, right);
    }

    @Override
    public NodeCalc visit(BinaryOperation nodeCalc, A arg, int depth) {
        return visitBinaryNodeCalc(nodeCalc, arg, depth);
    }

    @Override
    public NodeCalc visit(BinaryMinCalc nodeCalc, A arg, NodeCalc left, NodeCalc right) {
        return visitBinaryNodeCalc(nodeCalc, left, right);
    }

    @Override
    public NodeCalc visit(BinaryMinCalc nodeCalc, A arg, int depth) {
        return visitBinaryNodeCalc(nodeCalc, arg, depth);
    }

    @Override
    public NodeCalc visit(BinaryMaxCalc nodeCalc, A arg, NodeCalc left, NodeCalc right) {
        return visitBinaryNodeCalc(nodeCalc, left, right);
    }

    @Override
    public NodeCalc visit(BinaryMaxCalc nodeCalc, A arg, int depth) {
        return visitBinaryNodeCalc(nodeCalc, arg, depth);
    }

    private NodeCalc visitBinaryNodeCalc(AbstractBinaryNodeCalc nodeCalc, NodeCalc left, NodeCalc right) {
        if (left != null) {
            nodeCalc.setLeft(left);
        }
        if (right != null) {
            nodeCalc.setRight(right);
        }
        return null;
    }

    @Override
    public Pair<NodeCalc, NodeCalc> iterate(AbstractBinaryNodeCalc nodeCalc, A arg) {
        return Pair.of(nodeCalc.getLeft(), nodeCalc.getRight());
    }

    @Override
    public NodeCalc visit(UnaryOperation nodeCalc, A arg, NodeCalc child) {
        if (child != null) {
            nodeCalc.setChild(child);
        }
        return null;
    }

    @Override
    public NodeCalc visit(UnaryOperation nodeCalc, A arg, int depth) {
        return visitSingleChildNodeCalc(nodeCalc, arg, depth);
    }

    @Override
    public NodeCalc iterate(UnaryOperation nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc visit(MinNodeCalc nodeCalc, A arg, NodeCalc child) {
        if (child != null) {
            nodeCalc.setChild(child);
        }
        return null;
    }

    @Override
    public NodeCalc visit(MinNodeCalc nodeCalc, A arg, int depth) {
        return visitSingleChildNodeCalc(nodeCalc, arg, depth);
    }

    @Override
    public NodeCalc iterate(MinNodeCalc nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc visit(MaxNodeCalc nodeCalc, A arg, NodeCalc child) {
        if (child != null) {
            nodeCalc.setChild(child);
        }
        return null;
    }

    @Override
    public NodeCalc visit(MaxNodeCalc nodeCalc, A arg, int depth) {
        return visitSingleChildNodeCalc(nodeCalc, arg, depth);
    }

    @Override
    public NodeCalc iterate(MaxNodeCalc nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc visit(TimeNodeCalc nodeCalc, A arg, NodeCalc child) {
        if (child != null) {
            nodeCalc.setChild(child);
        }
        return null;
    }

    @Override
    public NodeCalc visit(TimeNodeCalc nodeCalc, A arg, int depth) {
        return visitSingleChildNodeCalc(nodeCalc, arg, depth);
    }

    @Override
    public NodeCalc iterate(TimeNodeCalc nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc visit(TimeSeriesNameNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public NodeCalc visit(TimeSeriesNameNodeCalc nodeCalc, A arg, int depth) {
        return null;
    }

    @Override
    public NodeCalc visit(TimeSeriesNumNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public NodeCalc visit(TimeSeriesNumNodeCalc nodeCalc, A arg, int depth) {
        return null;
    }

    private NodeCalc visitBinaryNodeCalc(AbstractBinaryNodeCalc nodeCalc, A arg, int depth) {
        if (depth < NodeCalcVisitors.RECURSION_THRESHOLD) {
            NodeCalc left = nodeCalc.getLeft().accept(this, arg, depth + 1);
            NodeCalc right = nodeCalc.getRight().accept(this, arg, depth + 1);
            if (left != null) {
                nodeCalc.setLeft(left);
            }
            if (right != null) {
                nodeCalc.setRight(right);
            }
            return null;
        } else {
            return NodeCalcVisitors.visit(nodeCalc, arg, this);
        }
    }

    private NodeCalc visitSingleChildNodeCalc(AbstractSingleChildNodeCalc nodeCalc, A arg, int depth) {
        if (depth < NodeCalcVisitors.RECURSION_THRESHOLD) {
            NodeCalc child = nodeCalc.getChild().accept(this, arg, depth + 1);
            if (child != null) {
                nodeCalc.setChild(child);
            }
            return null;
        } else {
            return NodeCalcVisitors.visit(nodeCalc, arg, this);
        }
    }
}
