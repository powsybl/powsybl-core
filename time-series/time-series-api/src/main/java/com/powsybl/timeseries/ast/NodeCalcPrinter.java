/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeCalcPrinter implements NodeCalcVisitor<String, Void> {

    public static String print(NodeCalc nodeCalc) {
        Objects.requireNonNull(nodeCalc);
        return NodeCalc.safeAccept(nodeCalc, new NodeCalcPrinter(), null);
    }

    @Override
    public String visit(IntegerNodeCalc nodeCalc, Void arg) {
        return Integer.toString(nodeCalc.getValue());
    }

    @Override
    public String visit(FloatNodeCalc nodeCalc, Void arg) {
        return Float.toString(nodeCalc.getValue());
    }

    @Override
    public String visit(DoubleNodeCalc nodeCalc, Void arg) {
        return Double.toString(nodeCalc.getValue());
    }

    @Override
    public String visit(BigDecimalNodeCalc nodeCalc, Void arg) {
        return nodeCalc.getValue().toString();
    }

    @Override
    public String visit(BinaryOperation nodeCalc, Void arg) {
        return "(" + nodeCalc.getLeft().accept(this, arg) + " " + nodeCalc.getOperator() +
                " " + nodeCalc.getRight().accept(this, arg) + ")";
    }

    @Override
    public String visit(UnaryOperation nodeCalc, Void arg) {
        return "(" + nodeCalc.getChild().accept(this, arg) + ")." + nodeCalc.getOperator() + "()";
    }

    @Override
    public String visit(MinNodeCalc nodeCalc, Void arg) {
        return nodeCalc.getChild().accept(this, arg) + ".min(" + nodeCalc.getMin() + ")";
    }

    @Override
    public String visit(MaxNodeCalc nodeCalc, Void arg) {
        return nodeCalc.getChild().accept(this, arg) + ".max(" + nodeCalc.getMax() + ")";
    }

    @Override
    public String visit(TimeNodeCalc nodeCalc, Void arg) {
        return "(" + nodeCalc.getChild().accept(this, arg) + ").time()";
    }

    @Override
    public String visit(TimeSeriesNameNodeCalc nodeCalc, Void arg) {
        return "timeSeries['" + nodeCalc.getTimeSeriesName() + "']";
    }

    @Override
    public String visit(TimeSeriesNumNodeCalc nodeCalc, Void arg) {
        return "timeSeries[" + nodeCalc.getTimeSeriesNum() + "]";
    }
}
