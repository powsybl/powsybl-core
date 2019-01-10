/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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
    public NodeCalc visit(BinaryOperation nodeCalc, A arg) {
        NodeCalc newLeft = nodeCalc.getLeft().accept(this, arg);
        if (newLeft != null) {
            nodeCalc.setLeft(newLeft);
        }
        NodeCalc newRight = nodeCalc.getRight().accept(this, arg);
        if (newRight != null) {
            nodeCalc.setRight(newRight);
        }
        return null;
    }

    @Override
    public NodeCalc visit(UnaryOperation nodeCalc, A arg) {
        NodeCalc newChild = nodeCalc.getChild().accept(this, arg);
        if (newChild != null) {
            nodeCalc.setChild(newChild);
        }
        return null;
    }

    @Override
    public NodeCalc visit(MinNodeCalc nodeCalc, A arg) {
        NodeCalc newChild = nodeCalc.getChild().accept(this, arg);
        if (newChild != null) {
            nodeCalc.setChild(newChild);
        }
        return null;
    }

    @Override
    public NodeCalc visit(MaxNodeCalc nodeCalc, A arg) {
        NodeCalc newChild = nodeCalc.getChild().accept(this, arg);
        if (newChild != null) {
            nodeCalc.setChild(newChild);
        }
        return null;
    }

    @Override
    public NodeCalc visit(TimeNodeCalc nodeCalc, A arg) {
        NodeCalc newChild = nodeCalc.getChild().accept(this, arg);
        if (newChild != null) {
            nodeCalc.setChild(newChild);
        }
        return null;
    }

    @Override
    public NodeCalc visit(TimeSeriesNameNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public NodeCalc visit(TimeSeriesNumNodeCalc nodeCalc, A arg) {
        return null;
    }
}
