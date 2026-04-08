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

import java.io.IOException;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractBinaryMinMax extends AbstractBinaryNodeCalc {

    protected AbstractBinaryMinMax(NodeCalc left, NodeCalc right) {
        super(left, right);
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

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
