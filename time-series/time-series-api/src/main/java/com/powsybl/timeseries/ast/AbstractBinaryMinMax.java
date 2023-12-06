/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.powsybl.timeseries.TimeSeriesException;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Deque;
import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractBinaryMinMax implements NodeCalc {

    protected NodeCalc left;
    protected NodeCalc right;

    protected AbstractBinaryMinMax(NodeCalc left, NodeCalc right) {
        this.left = Objects.requireNonNull(left);
        this.right = Objects.requireNonNull(right);
    }

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
            return visitor.visit(this, arg, leftValue, rightValue);
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

    @Override
    public <R, A> R acceptHandle(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> resultsStack) {
        Object rightResult = resultsStack.pop();
        rightResult = rightResult == NodeCalcVisitors.NULL ? null : rightResult;
        Object leftResult = resultsStack.pop();
        leftResult = leftResult == NodeCalcVisitors.NULL ? null : leftResult;
        return visitor.visit(this, arg, (R) leftResult, (R) rightResult);
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

    protected abstract String getJsonName();

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeFieldName(getJsonName());
        generator.writeStartObject();
        left.writeJson(generator);
        right.writeJson(generator);
        generator.writeEndObject();
    }

    protected static class ParsingContext {
        NodeCalc left = null;
        NodeCalc right = null;
    }

    static void parseFieldName(JsonParser parser, JsonToken token, ParsingContext context) throws IOException {
        if (context.left == null) {
            context.left = NodeCalc.parseJson(parser, token);
        } else if (context.right == null) {
            context.right = NodeCalc.parseJson(parser, token);
        } else {
            throw new TimeSeriesException("2 operands expected for a binary min/max comparison");
        }
    }

    protected static ParsingContext parseJson2(JsonParser parser) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            switch (token) {
                case START_OBJECT -> {
                    // Do nothing
                }
                case END_OBJECT -> {
                    if (context.left == null || context.right == null) {
                        throw new TimeSeriesException("Invalid binary min/max node calc JSON");
                    }
                    return context;
                }
                case FIELD_NAME -> parseFieldName(parser, token, context);
                default -> throw NodeCalc.createUnexpectedToken(token);
            }
        }
        throw NodeCalc.createUnexpectedToken(null);
    }
}
