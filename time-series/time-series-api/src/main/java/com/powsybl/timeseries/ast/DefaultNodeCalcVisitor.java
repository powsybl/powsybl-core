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
    public R visit(BinaryOperation nodeCalc, A arg, R left, R right) {
        return null;
    }

    @Override
    public R visit(UnaryOperation nodeCalc, A arg, R child) {
        return null;
    }

    @Override
    public NodeCalc iterate(UnaryOperation nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public R visit(MinNodeCalc nodeCalc, A arg, R child) {
        return null;
    }

    @Override
    public NodeCalc iterate(MinNodeCalc nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public R visit(MaxNodeCalc nodeCalc, A arg, R child) {
        return null;
    }

    @Override
    public NodeCalc iterate(MaxNodeCalc nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public R visit(CachedNodeCalc nodeCalc, A arg, R child) {
        return null;
    }

    @Override
    public NodeCalc iterate(CachedNodeCalc nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public R visit(TimeNodeCalc nodeCalc, A arg, R child) {
        return null;
    }

    @Override
    public NodeCalc iterate(TimeNodeCalc nodeCalc, A arg) {
        return nodeCalc.getChild();
    }

    @Override
    public R visit(TimeSeriesNameNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public R visit(TimeSeriesNumNodeCalc nodeCalc, A arg) {
        return null;
    }

    @Override
    public R visit(BinaryMinCalc nodeCalc, A arg, R left, R right) {
        return null;
    }

    @Override
    public R visit(BinaryMaxCalc nodeCalc, A arg, R left, R right) {
        return null;
    }

    @Override
    public Pair<NodeCalc, NodeCalc> iterate(AbstractBinaryNodeCalc nodeCalc, A arg) {
        return Pair.of(nodeCalc.getLeft(), nodeCalc.getRight());
    }
}
