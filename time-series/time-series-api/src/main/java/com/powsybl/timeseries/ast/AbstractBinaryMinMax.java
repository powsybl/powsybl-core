package com.powsybl.timeseries.ast;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.powsybl.timeseries.TimeSeriesException;

import java.io.IOException;
import java.util.Objects;

public abstract class AbstractBinaryMinMax implements NodeCalc {

    protected NodeCalc left;
    protected NodeCalc right;

    protected AbstractBinaryMinMax(NodeCalc left, NodeCalc right) {
        this.left = Objects.requireNonNull(left);
        this.right = Objects.requireNonNull(right);
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
                        throw new TimeSeriesException("Invalid binary operation node calc JSON");
                    }
                    return context;
                }
                case FIELD_NAME -> parseFieldName(parser, token, context);
                default -> throw NodeCalc.createUnexpectedToken(token);
            }
        }
        throw NodeCalc.createUnexpectedToken(token);
    }
}
