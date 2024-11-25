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
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.timeseries.TimeSeriesException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Deque;
import java.util.Objects;

/**
 * A NodeCalc is an element of the timeseries computation tree. These
 * computation trees are typically the results of running user scripts, for
 * example written in groovy. They can be serialized to json or traversed by
 * visitors. Traversing them with visitor allows to compute various results,
 * such as evaluating the tree or find the names of the timeseries used in the
 * tree.
 *
 * <p>The writeJson method is used to serialize the tree to json.
 *
 * <p>The accept, acceptIterate and acceptHandle methods together with the
 * {@link NodeCalcVisitor} interface form the
 * hybrid recursive/iterative visitor pattern. This visitor pattern
 * uses recursion on the children up to a stack depth limit because
 * performance is almost 5 times better when using recursion compared
 * to the iterative algorithm using stacks, but excessive depths cause
 * StackOverflowErrors.
 *
 * <p>The accept method are the main entrypoint of the hybrid visit and
 * implements the recursive visit as well as performing the switch from
 * the recursive to iterative behavior.
 *
 * <p>The acceptIterate and acceptHandle methods are used by {@link NodeCalcVisitors}
 * during the iterative traversal of the tree. The
 * acceptIterate method push children nodes the be traversed in the
 * stack.  The acceptHandle method extract the already calculated
 * children results from the stack and use them to compute and return
 * the result for this node.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface NodeCalc {

    <R, A> R accept(NodeCalcVisitor<R, A> visitor, A arg, int depth);

    <R, A> void acceptIterate(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> nodesStack);

    <R, A> R acceptHandle(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> resultsStack);

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
            boolean continueLoop = true;
            while (continueLoop && (token = parser.nextToken()) != null) {
                switch (token) {
                    case START_OBJECT -> {
                        // Do nothing
                    }
                    case END_OBJECT -> continueLoop = false;
                    default -> nodeCalc = parseJson(parser, token);
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
            String fieldName = parser.currentName();
            switch (fieldName) {
                case IntegerNodeCalc.NAME -> {
                    return IntegerNodeCalc.parseJson(parser);
                }
                case FloatNodeCalc.NAME -> {
                    return FloatNodeCalc.parseJson(parser);
                }
                case DoubleNodeCalc.NAME -> {
                    return DoubleNodeCalc.parseJson(parser);
                }
                case BigDecimalNodeCalc.NAME -> {
                    return BigDecimalNodeCalc.parseJson(parser);
                }
                case BinaryOperation.NAME -> {
                    return BinaryOperation.parseJson(parser);
                }
                case UnaryOperation.NAME -> {
                    return UnaryOperation.parseJson(parser);
                }
                case MinNodeCalc.NAME -> {
                    return MinNodeCalc.parseJson(parser);
                }
                case MaxNodeCalc.NAME -> {
                    return MaxNodeCalc.parseJson(parser);
                }
                case TimeSeriesNameNodeCalc.NAME -> {
                    return TimeSeriesNameNodeCalc.parseJson(parser);
                }
                case TimeNodeCalc.NAME -> {
                    return TimeNodeCalc.parseJson(parser);
                }
                case BinaryMinCalc.NAME -> {
                    return BinaryMinCalc.parseJson(parser);
                }
                case BinaryMaxCalc.NAME -> {
                    return BinaryMaxCalc.parseJson(parser);
                }
                default -> {
                    // Do nothing
                }
            }
        }
        throw createUnexpectedToken(token);
    }
}
