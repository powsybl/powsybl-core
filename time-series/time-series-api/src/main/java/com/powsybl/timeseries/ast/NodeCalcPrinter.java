/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NodeCalcPrinter implements NodeCalcVisitor<String, Void> {

    public static String print(NodeCalc nodeCalc) {
        return nodeCalc.accept(new NodeCalcPrinter(), null, 0);
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
    public String visit(BinaryOperation nodeCalc, Void arg, String left, String right) {
        return "(" + left + " " + nodeCalc.getOperator() + " " + right + ")";
    }

    @Override
    public String visit(UnaryOperation nodeCalc, Void arg, String child) {
        return "(" + child + ")." + nodeCalc.getOperator() + "()";
    }

    @Override
    public String visit(MinNodeCalc nodeCalc, Void arg, String child) {
        return child + ".min(" + nodeCalc.getMin() + ")";
    }

    @Override
    public String visit(MaxNodeCalc nodeCalc, Void arg, String child) {
        return child + ".max(" + nodeCalc.getMax() + ")";
    }

    @Override
    public String visit(CachedNodeCalc nodeCalc, Void arg, String child) {
        return child;
    }

    @Override
    public String visit(TimeNodeCalc nodeCalc, Void arg, String child) {
        return "(" + child + ").time()";
    }

    @Override
    public String visit(TimeSeriesNameNodeCalc nodeCalc, Void arg) {
        return "timeSeries['" + nodeCalc.getTimeSeriesName() + "']";
    }

    @Override
    public String visit(TimeSeriesNumNodeCalc nodeCalc, Void arg) {
        return "timeSeries[" + nodeCalc.getTimeSeriesNum() + "]";
    }

    @Override
    public String visit(BinaryMinCalc nodeCalc, Void arg, String left, String right) {
        return "min(" + left + ", " + right + ")";
    }

    @Override
    public String visit(BinaryMaxCalc nodeCalc, Void arg, String left, String right) {
        return "max(" + left + ", " + right + ")";
    }

    @Override
    public NodeCalc iterate(TimeNodeCalc nodeCalc, Void arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc iterate(UnaryOperation nodeCalc, Void arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc iterate(MinNodeCalc nodeCalc, Void arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc iterate(MaxNodeCalc nodeCalc, Void arg) {
        return nodeCalc.getChild();
    }

    @Override
    public NodeCalc iterate(CachedNodeCalc nodeCalc, Void arg) {
        return nodeCalc.getChild();
    }

    @Override
    public Pair<NodeCalc, NodeCalc> iterate(AbstractBinaryNodeCalc nodeCalc, Void arg) {
        return Pair.of(nodeCalc.getLeft(), nodeCalc.getRight());
    }
}
