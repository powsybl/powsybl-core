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

import java.io.IOException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractMinMaxNodeCalc implements NodeCalc {

    protected NodeCalc child;

    protected final double value;

    protected AbstractMinMaxNodeCalc(NodeCalc child, double value) {
        this.child = Objects.requireNonNull(child);
        this.value = value;
    }

    public NodeCalc getChild() {
        return child;
    }

    public void setChild(NodeCalc child) {
        this.child = Objects.requireNonNull(child);
    }

    protected abstract String getJsonName();

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeFieldName(getJsonName());
        generator.writeStartObject();
        child.writeJson(generator);
        generator.writeNumberField("value", value);
        generator.writeEndObject();
    }

    protected static class ParsingContext {
        NodeCalc child = null;
        double value = Double.NaN;
    }

    static void parseFieldName(JsonParser parser, JsonToken token, ParsingContext context) throws IOException {
        String fieldName = parser.getCurrentName();
        if ("value".equals(fieldName)) {
            parser.nextValue();
            context.value = parser.getValueAsDouble();
        } else {
            if (context.child == null) {
                context.child = NodeCalc.parseJson(parser, token);
            } else {
                throw new TimeSeriesException("Only 1 operand expected for a min/max");
            }
        }
    }

    protected static ParsingContext parseJson2(JsonParser parser) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            if (token == JsonToken.START_OBJECT) {
                // skip
            } else if (token == JsonToken.END_OBJECT) {
                if (context.child == null || Double.isNaN(context.value)) {
                    throw new TimeSeriesException("Invalid min/max node calc JSON");
                }
                return context;
            } else if (token == JsonToken.FIELD_NAME) {
                parseFieldName(parser, token, context);
            } else {
                throw NodeCalc.createUnexpectedToken(token);
            }
        }
        throw NodeCalc.createUnexpectedToken(token);
    }
}
