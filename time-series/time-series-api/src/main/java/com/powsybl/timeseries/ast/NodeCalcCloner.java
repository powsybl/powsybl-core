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
public class NodeCalcCloner<A> implements NodeCalcVisitor<NodeCalc, A> {
    @Override
    public NodeCalc visit(IntegerNodeCalc nodeCalc, A arg) {
        return new IntegerNodeCalc(nodeCalc.getValue());
    }

    @Override
    public NodeCalc visit(FloatNodeCalc nodeCalc, A arg) {
        return new FloatNodeCalc(nodeCalc.getValue());
    }

    @Override
    public NodeCalc visit(DoubleNodeCalc nodeCalc, A arg) {
        return new DoubleNodeCalc(nodeCalc.getValue());
    }

    @Override
    public NodeCalc visit(BigDecimalNodeCalc nodeCalc, A arg) {
        return new BigDecimalNodeCalc(nodeCalc.getValue());
    }

    @Override
    public NodeCalc visit(BinaryOperation nodeCalc, A arg) {
        return new BinaryOperation(nodeCalc.getLeft().accept(this, arg),
                                   nodeCalc.getRight().accept(this, arg),
                                   nodeCalc.getOperator());
    }

    @Override
    public NodeCalc visit(UnaryOperation nodeCalc, A arg) {
        return new UnaryOperation(nodeCalc.getChild().accept(this, arg), nodeCalc.getOperator());
    }

    @Override
    public NodeCalc visit(MinNodeCalc nodeCalc, A arg) {
        return new MinNodeCalc(nodeCalc.getChild().accept(this, arg), nodeCalc.getMin());
    }

    @Override
    public NodeCalc visit(MaxNodeCalc nodeCalc, A arg) {
        return new MaxNodeCalc(nodeCalc.getChild().accept(this, arg), nodeCalc.getMax());
    }

    @Override
    public NodeCalc visit(TimeNodeCalc nodeCalc, A arg) {
        return new TimeNodeCalc(nodeCalc.getChild().accept(this, arg));
    }

    @Override
    public NodeCalc visit(TimeSeriesNameNodeCalc nodeCalc, A arg) {
        return new TimeSeriesNameNodeCalc(nodeCalc.getTimeSeriesName());
    }

    @Override
    public NodeCalc visit(TimeSeriesNumNodeCalc nodeCalc, A arg) {
        return new TimeSeriesNumNodeCalc(nodeCalc.getTimeSeriesNum());
    }
}
