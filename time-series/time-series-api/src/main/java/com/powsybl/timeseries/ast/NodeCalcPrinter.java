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
public class NodeCalcPrinter implements NodeCalcVisitor<NodeCalcPrintable, Void> {

    public static String print(NodeCalc nodeCalc) {
        NodeCalcPrintable root = nodeCalc.accept(new NodeCalcPrinter(), null, 0);
        StringBuilder builder = new StringBuilder();
        Deque<Object> stack = new ArrayDeque<>();
        stack.push(root);

        // Build the output iteratively using a StringBuilder, each printable pushes its children and its expression parts onto the stack in reverse order,
        // Then process the stored expressions in their printing order, and the String parts are appended once to the shared StringBuilder,
        // Unlike recursive string concatenation, this does not repeatedly copy the strings of entire subtree, resulting in linear time complexity.
        while (!stack.isEmpty()) {
            Object node = stack.pop();
            if (node instanceof String text) {
                builder.append(text);
            } else if (node instanceof NodeCalcPrintable printable) {
                printable.pushTo(stack);
            }
        }
        return builder.toString();
    }

    @Override
    public NodeCalcPrintable visit(IntegerNodeCalc nodeCalc, Void arg) {
        return stack -> stack.push(Integer.toString(nodeCalc.getValue()));
    }

    @Override
    public NodeCalcPrintable visit(FloatNodeCalc nodeCalc, Void arg) {
        return stack -> stack.push(Float.toString(nodeCalc.getValue()));
    }

    @Override
    public NodeCalcPrintable visit(DoubleNodeCalc nodeCalc, Void arg) {
        return stack -> stack.push(Double.toString(nodeCalc.getValue()));
    }

    @Override
    public NodeCalcPrintable visit(BigDecimalNodeCalc nodeCalc, Void arg) {
        return stack -> stack.push(nodeCalc.getValue().toString());
    }

    @Override
    public NodeCalcPrintable visit(BinaryOperation nodeCalc, Void arg, NodeCalcPrintable left, NodeCalcPrintable right) {
        return stack -> {
            stack.push(")");
            stack.push(right);
            stack.push(" " + nodeCalc.getOperator() + " ");
            stack.push(left);
            stack.push("(");
        };
    }

    @Override
    public NodeCalcPrintable visit(UnaryOperation nodeCalc, Void arg, NodeCalcPrintable child) {
        return stack -> {
            stack.push(")." + nodeCalc.getOperator() + "()");
            stack.push(child);
            stack.push("(");
        };
    }

    @Override
    public NodeCalcPrintable visit(MinNodeCalc nodeCalc, Void arg, NodeCalcPrintable child) {
        return stack -> {
            stack.push(".min(" + nodeCalc.getMin() + ")");
            stack.push(child);
        };
    }

    @Override
    public NodeCalcPrintable visit(MaxNodeCalc nodeCalc, Void arg, NodeCalcPrintable child) {
        return stack -> {
            stack.push(".max(" + nodeCalc.getMax() + ")");
            stack.push(child);
        };
    }

    @Override
    public NodeCalcPrintable visit(CachedNodeCalc nodeCalc, Void arg, NodeCalcPrintable child) {
        return child;
    }

    @Override
    public NodeCalcPrintable visit(TimeNodeCalc nodeCalc, Void arg, NodeCalcPrintable child) {
        return stack -> {
            stack.push(").time()");
            stack.push(child);
            stack.push("(");
        };
    }

    @Override
    public NodeCalcPrintable visit(TimeSeriesNameNodeCalc nodeCalc, Void arg) {
        return stack -> stack.push("timeSeries['" + nodeCalc.getTimeSeriesName() + "']");
    }

    @Override
    public NodeCalcPrintable visit(TimeSeriesNumNodeCalc nodeCalc, Void arg) {
        return stack -> stack.push("timeSeries[" + nodeCalc.getTimeSeriesNum() + "]");
    }

    @Override
    public NodeCalcPrintable visit(BinaryMinCalc nodeCalc, Void arg, NodeCalcPrintable left, NodeCalcPrintable right) {
        return stack -> {
            stack.push(")");
            stack.push(right);
            stack.push(", ");
            stack.push(left);
            stack.push("min(");
        };
    }

    @Override
    public NodeCalcPrintable visit(BinaryMaxCalc nodeCalc, Void arg, NodeCalcPrintable left, NodeCalcPrintable right) {
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
