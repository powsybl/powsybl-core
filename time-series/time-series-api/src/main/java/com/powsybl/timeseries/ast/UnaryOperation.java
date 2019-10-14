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
import com.powsybl.timeseries.ast.NodeCalcVisitors.NodeWrapper;

import java.io.IOException;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UnaryOperation extends AbstractSingleChildNodeCalc {

    static final String NAME = "unaryOp";

    public enum Operator {
        ABS("abs"),
        NEGATIVE("negative"),
        POSITIVE("positive");

        Operator(String str) {
            this.str = Objects.requireNonNull(str);
        }

        @Override
        public String toString() {
            return str;
        }

        private final String str;
    }

    public static UnaryOperation abs(NodeCalc child) {
        return new UnaryOperation(child, Operator.ABS);
    }

    public static UnaryOperation negative(NodeCalc child) {
        return new UnaryOperation(child, Operator.NEGATIVE);
    }

    public static UnaryOperation positive(NodeCalc child) {
        return new UnaryOperation(child, Operator.POSITIVE);
    }

    private final Operator operator;

    UnaryOperation(NodeCalc child, Operator operator) {
        super(child);
        this.operator = Objects.requireNonNull(operator);
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public <R, A> R acceptVisit(NodeCalcVisitor<R, A> visitor, A arg, Deque<Optional<R>> children) {
        return visitor.visit(this, arg, children.pop().orElse(null));
    }

    @Override
    public <R, A> void acceptIterate(NodeCalcVisitor<R, A> visitor, A arg, Deque<NodeWrapper> visitQueue) {
        visitQueue.push(new NodeWrapper(visitor.iterate(this, arg)));
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeFieldName(NAME);
        generator.writeStartObject();
        generator.writeStringField("op", operator.name());
        child.writeJson(generator);
        generator.writeEndObject();
    }

    static class ParsingContext {
        NodeCalc child;
        Operator operator;
    }

    static void parseFieldName(JsonParser parser, JsonToken token, ParsingContext context) throws IOException {
        String fieldName = parser.getCurrentName();
        if ("op".equals(fieldName)) {
            context.operator = Operator.valueOf(parser.nextTextValue());
        } else {
            if (context.child != null) {
                throw new TimeSeriesException("Only 1 operand expected for an unary operation");
            }
            context.child = NodeCalc.parseJson(parser, token);
        }
    }

    static NodeCalc parseJson(JsonParser parser) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            if (token == JsonToken.START_OBJECT) {
                // skip
            } else if (token == JsonToken.END_OBJECT) {
                if (context.child == null || context.operator == null) {
                    throw new TimeSeriesException("Invalid unary operation node calc JSON");
                }
                return new UnaryOperation(context.child, context.operator);
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
        return child.hashCode() + operator.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UnaryOperation) {
            return (((UnaryOperation) obj).child).equals(child) && ((UnaryOperation) obj).operator == operator;
        }
        return false;
    }
}
