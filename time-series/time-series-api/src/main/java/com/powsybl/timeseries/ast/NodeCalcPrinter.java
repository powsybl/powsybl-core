/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NodeCalcPrinter implements NodeCalcVisitor<Printable, Void> {

    public static String print(NodeCalc nodeCalc) {
        Printable root = nodeCalc.accept(new NodeCalcPrinter(), null, 0);
        StringBuilder builder = new StringBuilder();
        Deque<Object> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Object node = stack.pop();
            if (node instanceof String text) {
                builder.append(text);
            } else if (node instanceof Printable printable) {
                printable.pushTo(stack);
            }
        }
        return builder.toString();
    }

    @Override
    public Printable visit(IntegerNodeCalc nodeCalc, Void arg) {
        return stack -> stack.push(Integer.toString(nodeCalc.getValue()));
    }

    @Override
    public Printable visit(FloatNodeCalc nodeCalc, Void arg) {
        return stack -> stack.push(Float.toString(nodeCalc.getValue()));
    }

    @Override
    public Printable visit(DoubleNodeCalc nodeCalc, Void arg) {
        return stack -> stack.push(Double.toString(nodeCalc.getValue()));
    }

    @Override
    public Printable visit(BigDecimalNodeCalc nodeCalc, Void arg) {
        return stack -> stack.push(nodeCalc.getValue().toString());
    }

    @Override
    public Printable visit(BinaryOperation nodeCalc, Void arg, Printable left, Printable right) {
        return stack -> {
            stack.push(")");
            stack.push(right);
            stack.push(" " + nodeCalc.getOperator() + " ");
            stack.push(left);
            stack.push("(");
        };
    }

    @Override
    public Printable visit(UnaryOperation nodeCalc, Void arg, Printable child) {
        return stack -> {
            stack.push(")." + nodeCalc.getOperator() + "()");
            stack.push(child);
            stack.push("(");
        };
    }

    @Override
    public Printable visit(MinNodeCalc nodeCalc, Void arg, Printable child) {
        return stack -> {
            stack.push(".min(" + nodeCalc.getMin() + ")");
            stack.push(child);
        };
    }

    @Override
    public Printable visit(MaxNodeCalc nodeCalc, Void arg, Printable child) {
        return stack -> {
            stack.push(".max(" + nodeCalc.getMax() + ")");
            stack.push(child);
        };
    }

    @Override
    public Printable visit(CachedNodeCalc nodeCalc, Void arg, Printable child) {
        return child;
    }

    @Override
    public Printable visit(TimeNodeCalc nodeCalc, Void arg, Printable child) {
        return stack -> {
            stack.push(").time()");
            stack.push(child);
            stack.push("(");
        };
    }

    @Override
    public Printable visit(TimeSeriesNameNodeCalc nodeCalc, Void arg) {
        return stack -> stack.push("timeSeries['" + nodeCalc.getTimeSeriesName() + "']");
    }

    @Override
    public Printable visit(TimeSeriesNumNodeCalc nodeCalc, Void arg) {
        return stack -> stack.push("timeSeries[" + nodeCalc.getTimeSeriesNum() + "]");
    }

    @Override
    public Printable visit(BinaryMinCalc nodeCalc, Void arg, Printable left, Printable right) {
        return stack -> {
            stack.push(")");
            stack.push(right);
            stack.push(", ");
            stack.push(left);
            stack.push("min(");
        };
    }

    @Override
    public Printable visit(BinaryMaxCalc nodeCalc, Void arg, Printable left, Printable right) {
        return stack -> {
            stack.push(")");
            stack.push(right);
            stack.push(", ");
            stack.push(left);
            stack.push("max(");
        };
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
