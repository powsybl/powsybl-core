/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Deque;
import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractBinaryNodeCalc implements NodeCalc {

    protected NodeCalc left;
    protected NodeCalc right;

    protected AbstractBinaryNodeCalc(NodeCalc left, NodeCalc right) {
        this.left = Objects.requireNonNull(left);
        this.right = Objects.requireNonNull(right);
    }

    protected abstract <R, A> R accept(NodeCalcVisitor<R, A> visitor, A arg, R leftValue, R rightValue);

    @Override
    public <R, A> R accept(NodeCalcVisitor<R, A> visitor, A arg, int depth) {
        if (depth < NodeCalcVisitors.RECURSION_THRESHOLD) {
            Pair<NodeCalc, NodeCalc> p = visitor.iterate(this, arg);
            R leftValue = null;
            NodeCalc leftNode = p.getLeft();
            if (leftNode != null) {
                leftValue = leftNode.accept(visitor, arg, depth + 1);
            }
            R rightValue = null;
            NodeCalc rightNode = p.getRight();
            if (rightNode != null) {
                rightValue = rightNode.accept(visitor, arg, depth + 1);
            }
            return accept(visitor, arg, leftValue, rightValue);
        } else {
            return NodeCalcVisitors.visit(this, arg, visitor);
        }
    }

    @Override
    public <R, A> void acceptIterate(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> nodesStack) {
        Pair<NodeCalc, NodeCalc> p = visitor.iterate(this, arg);
        Object leftNode = p.getLeft();
        leftNode = leftNode == null ? NodeCalcVisitors.NULL : leftNode;
        Object rightNode = p.getRight();
        rightNode = rightNode == null ? NodeCalcVisitors.NULL : rightNode;
        nodesStack.push(leftNode);
        nodesStack.push(rightNode);
    }

    protected abstract <R, A> R acceptHandle(NodeCalcVisitor<R, A> visitor, A arg, R leftResult, R rightResult);

    @SuppressWarnings("unchecked")
    @Override
    public <R, A> R acceptHandle(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> resultsStack) {
        Object rightResult = resultsStack.pop();
        rightResult = rightResult == NodeCalcVisitors.NULL ? null : rightResult;
        Object leftResult = resultsStack.pop();
        leftResult = leftResult == NodeCalcVisitors.NULL ? null : leftResult;
        return acceptHandle(visitor, arg, (R) leftResult, (R) rightResult);
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    public NodeCalc getLeft() {
        return left;
    }

    public void setLeft(NodeCalc left) {
        this.left = Objects.requireNonNull(left);
    }

    public NodeCalc getRight() {
        return right;
    }

    public void setRight(NodeCalc right) {
        this.right = Objects.requireNonNull(right);
    }
}
