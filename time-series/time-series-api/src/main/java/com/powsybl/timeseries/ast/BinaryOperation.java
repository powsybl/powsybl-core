/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
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

import java.io.IOException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class BinaryOperation extends AbstractBinaryNodeCalc {

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

    private final Operator operator;

    BinaryOperation(NodeCalc left, NodeCalc right, Operator operator) {
        super(left, right);
        this.operator = Objects.requireNonNull(operator);
    }

    @Override
    public <R, A> R accept(NodeCalcVisitor<R, A> visitor, A arg, R leftValue, R rightValue) {
        return visitor.visit(this, arg, leftValue, rightValue);
    }

    @Override
    public <R, A> R acceptHandle(NodeCalcVisitor<R, A> visitor, A arg, R leftResult, R rightResult) {
        return visitor.visit(this, arg, leftResult, rightResult);
    }

    public Operator getOperator() {
        return operator;
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
            switch (token) {
                case START_OBJECT -> {
                    // Do nothing
                }
                case END_OBJECT -> {
                    if (context.left == null || context.right == null || context.operator == null) {
                        throw new TimeSeriesException("Invalid binary operation node calc JSON");
                    }
                    return new BinaryOperation(context.left, context.right, context.operator);
                }
                case FIELD_NAME -> parseFieldName(parser, token, context);
                default -> throw NodeCalc.createUnexpectedToken(token);
            }
        }
        throw NodeCalc.createUnexpectedToken(token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right, operator, NAME);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinaryOperation binaryOperation) {
            return binaryOperation.left.equals(left) && binaryOperation.right.equals(right) && binaryOperation.operator == operator;
        }
        return false;
    }
}
