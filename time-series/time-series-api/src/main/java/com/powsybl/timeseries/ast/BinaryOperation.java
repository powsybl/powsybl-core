/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BinaryOperation implements NodeCalc {

    static final String NAME = "binaryOp";

    public enum Operator {
        PLUS("+"),
        MINUS("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        LESS_THAN("<"),
        LESS_THAN_OR_EQUALS_TO("<="),
        GREATER_THAN(">"),
        GREATER_THAN_OR_EQUALS_TO(">="),
        EQUALS("=="),
        NOT_EQUALS("!=");

        Operator(String str) {
            this.str = Objects.requireNonNull(str);
        }

        private final String str;

        @Override
        public String toString() {
            return str;
        }
    }

    public static BinaryOperation plus(NodeCalc left, NodeCalc right) {
        return new BinaryOperation(left, right, Operator.PLUS);
    }

    public static BinaryOperation minus(NodeCalc left, NodeCalc right) {
        return new BinaryOperation(left, right, Operator.MINUS);
    }

    public static BinaryOperation multiply(NodeCalc left, NodeCalc right) {
        return new BinaryOperation(left, right, Operator.MULTIPLY);
    }

    public static BinaryOperation div(NodeCalc left, NodeCalc right) {
        return new BinaryOperation(left, right, Operator.DIVIDE);
    }

    public static BinaryOperation lessThan(NodeCalc left, NodeCalc right) {
        return new BinaryOperation(left, right, Operator.LESS_THAN);
    }

    public static BinaryOperation lessThanOrEqualsTo(NodeCalc left, NodeCalc right) {
        return new BinaryOperation(left, right, Operator.LESS_THAN_OR_EQUALS_TO);
    }

    public static BinaryOperation greaterThan(NodeCalc left, NodeCalc right) {
        return new BinaryOperation(left, right, Operator.GREATER_THAN);
    }

    public static BinaryOperation greaterThanOrEqualsTo(NodeCalc left, NodeCalc right) {
        return new BinaryOperation(left, right, Operator.GREATER_THAN_OR_EQUALS_TO);
    }

    public static BinaryOperation equals(NodeCalc left, NodeCalc right) {
        return new BinaryOperation(left, right, Operator.EQUALS);
    }

    public static BinaryOperation notEquals(NodeCalc left, NodeCalc right) {
        return new BinaryOperation(left, right, Operator.NOT_EQUALS);
    }

    private NodeCalc left;

    private NodeCalc right;

    private final Operator operator;

    BinaryOperation(NodeCalc left, NodeCalc right, Operator operator) {
        this.left = Objects.requireNonNull(left);
        this.right = Objects.requireNonNull(right);
        this.operator = Objects.requireNonNull(operator);
    }

    public Operator getOperator() {
        return operator;
    }

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

    @SuppressWarnings("unchecked")
    @Override
    public <R, A> R acceptHandle(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> resultsStack) {
        Object rightResult = resultsStack.pop();
        rightResult = rightResult == NodeCalcVisitors.NULL ? null : rightResult;
        Object leftResult = resultsStack.pop();
        leftResult = leftResult == NodeCalcVisitors.NULL ? null : leftResult;
        return visitor.visit(this, arg, (R) leftResult, (R) rightResult);
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
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeFieldName(NAME);
        generator.writeStartObject();
        generator.writeStringField("op", operator.name());
        left.writeJson(generator);
        right.writeJson(generator);
        generator.writeEndObject();
    }

    static class ParsingContext {
        NodeCalc left;
        NodeCalc right;
        Operator operator;
    }

    static void parseFieldName(JsonParser parser, JsonToken token, ParsingContext context) throws IOException {
        String fieldName = parser.getCurrentName();
        if ("op".equals(fieldName)) {
            context.operator = Operator.valueOf(parser.nextTextValue());
        } else {
            if (context.left == null) {
                context.left = NodeCalc.parseJson(parser, token);
            } else if (context.right == null) {
                context.right = NodeCalc.parseJson(parser, token);
            } else {
                throw new TimeSeriesException("2 operands expected for a binary operation");
            }
        }
    }

    static NodeCalc parseJson(JsonParser parser) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            if (token == JsonToken.START_OBJECT) {
                // skip
            } else if (token == JsonToken.END_OBJECT) {
                if (context.left == null || context.right == null || context.operator == null) {
                    throw new TimeSeriesException("Invalid binary operation node calc JSON");
                }
                return new BinaryOperation(context.left, context.right, context.operator);
            } else if (token == JsonToken.FIELD_NAME) {
                parseFieldName(parser, token, context);
            } else {
                throw NodeCalc.createUnexpectedToken(token);
            }
        }
        throw NodeCalc.createUnexpectedToken(token);
    }

    @Override
    public int hashCode() {
        return left.hashCode() + right.hashCode() + operator.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinaryOperation) {
            return ((BinaryOperation) obj).left.equals(left) && ((BinaryOperation) obj).right.equals(right) && ((BinaryOperation) obj).operator == operator;
        }
        return false;
    }
}
