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
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.timeseries.TimeSeriesException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Deque;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface NodeCalc {

    <R, A> void acceptIterate(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> visitQueue);

    <R, A> R acceptVisit(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> childrenQueue);

    void writeJson(JsonGenerator generator) throws IOException;

    static void writeJson(NodeCalc node, JsonGenerator generator) {
        try {
            generator.writeStartObject();
            node.writeJson(generator);
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String toJson(NodeCalc node) {
        Objects.requireNonNull(node);
        return JsonUtil.toJson(generator -> writeJson(node, generator));
    }

    static NodeCalc parseJson(String json) {
        return JsonUtil.parseJson(json, NodeCalc::parseJson);
    }

    static NodeCalc parseJson(JsonParser parser) {
        Objects.requireNonNull(parser);
        try {
            NodeCalc nodeCalc = null;
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.START_OBJECT) {
                    // skip
                } else if (token == JsonToken.END_OBJECT) {
                    break;
                } else {
                    nodeCalc = parseJson(parser, token);
                }
            }
            return nodeCalc;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static TimeSeriesException createUnexpectedToken(JsonToken token) {
        return new TimeSeriesException("Unexpected JSON token: " + token);
    }

    static NodeCalc parseJson(JsonParser parser, JsonToken token) throws IOException {
        Objects.requireNonNull(parser);
        Objects.requireNonNull(token);
        if (token == JsonToken.FIELD_NAME) {
            String fieldName = parser.getCurrentName();
            switch (fieldName) {
                case IntegerNodeCalc.NAME:
                    return IntegerNodeCalc.parseJson(parser);

                case FloatNodeCalc.NAME:
                    return FloatNodeCalc.parseJson(parser);

                case DoubleNodeCalc.NAME:
                    return DoubleNodeCalc.parseJson(parser);

                case BigDecimalNodeCalc.NAME:
                    return BigDecimalNodeCalc.parseJson(parser);

                case BinaryOperation.NAME:
                    return BinaryOperation.parseJson(parser);

                case UnaryOperation.NAME:
                    return UnaryOperation.parseJson(parser);

                case MinNodeCalc.NAME:
                    return MinNodeCalc.parseJson(parser);

                case MaxNodeCalc.NAME:
                    return MaxNodeCalc.parseJson(parser);

                case TimeSeriesNameNodeCalc.NAME:
                    return TimeSeriesNameNodeCalc.parseJson(parser);

                case TimeNodeCalc.NAME:
                    return TimeNodeCalc.parseJson(parser);

                default:
                    break;
            }
        }
        throw createUnexpectedToken(token);
    }
}
