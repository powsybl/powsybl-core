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
    public NodeCalc visit(FloatNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public NodeCalc visit(DoubleNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public NodeCalc visit(BigDecimalNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public NodeCalc visit(BinaryOperation nodeCalc, A arg, NodeCalc left, NodeCalc right) {
        NodeCalc newLeft = left;
        if (newLeft != null) {
            nodeCalc.setLeft(newLeft);
        }
        NodeCalc newRight = right;
        if (newRight != null) {
            nodeCalc.setRight(newRight);
        }
        return null;
    }

    @Override
    public Pair<NodeCalc, NodeCalc> iterate(BinaryOperation nodeCalc, A arg) {
        return Pair.of(nodeCalc.getLeft(), nodeCalc.getRight());
    }

    @Override
    public NodeCalc visit(UnaryOperation nodeCalc, A arg, NodeCalc child) {
        NodeCalc newChild = child;
        if (newChild != null) {
            nodeCalc.setChild(newChild);
        }
        return null;
    }

    @Override
    public NodeCalc iterate(UnaryOperation nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc visit(MinNodeCalc nodeCalc, A arg, NodeCalc child) {
        NodeCalc newChild = child;
        if (newChild != null) {
            nodeCalc.setChild(newChild);
        }
        return null;
    }

    @Override
    public NodeCalc iterate(MinNodeCalc nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc visit(MaxNodeCalc nodeCalc, A arg, NodeCalc child) {
        NodeCalc newChild = child;
        if (newChild != null) {
            nodeCalc.setChild(newChild);
        }
        return null;
    }

    @Override
    public NodeCalc iterate(MaxNodeCalc nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc visit(TimeNodeCalc nodeCalc, A arg, NodeCalc child) {
        NodeCalc newChild = child;
        if (newChild != null) {
            nodeCalc.setChild(newChild);
        }
        return null;
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
    public NodeCalc visit(TimeSeriesNumNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public NodeCalc visit(BinaryMinCalc nodeCalc, A arg, NodeCalc left, NodeCalc right) {
        NodeCalc newLeft = left;
        if (newLeft != null) {
            nodeCalc.setLeft(newLeft);
        }
        NodeCalc newRight = right;
        if (newRight != null) {
            nodeCalc.setRight(newRight);
        }
        return null;
    }

    @Override
    public Pair<NodeCalc, NodeCalc> iterate(BinaryMinCalc nodeCalc, A arg) {
        return Pair.of(nodeCalc.getLeft(), nodeCalc.getRight());
    }
}
