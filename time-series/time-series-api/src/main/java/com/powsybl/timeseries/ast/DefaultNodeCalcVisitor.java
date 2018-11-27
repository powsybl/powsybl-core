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
public class DefaultNodeCalcVisitor<R, A> implements NodeCalcVisitor<R, A> {
    @Override
    public R visit(IntegerNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public R visit(FloatNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public R visit(DoubleNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public R visit(BigDecimalNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public R visit(BinaryOperation nodeCalc, A arg) {
        nodeCalc.getLeft().accept(this, arg);
        nodeCalc.getRight().accept(this, arg);
        return null;
    }

    @Override
    public R visit(UnaryOperation nodeCalc, A arg) {
        nodeCalc.getChild().accept(this, arg);
        return null;
    }

    @Override
    public R visit(MinNodeCalc nodeCalc, A arg) {
        nodeCalc.getChild().accept(this, arg);
        return null;
    }

    @Override
    public R visit(MaxNodeCalc nodeCalc, A arg) {
        nodeCalc.getChild().accept(this, arg);
        return null;
    }

    @Override
    public R visit(TimeNodeCalc nodeCalc, A arg) {
        nodeCalc.getChild().accept(this, arg);
        return null;
    }

    @Override
    public R visit(TimeSeriesNameNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public R visit(TimeSeriesNumNodeCalc nodeCalc, A arg) {
        return null;
    }
}
