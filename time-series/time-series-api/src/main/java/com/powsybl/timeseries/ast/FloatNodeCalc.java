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
import java.util.Deque;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class FloatNodeCalc implements LiteralNodeCalc {

    static final String NAME = "float";

    private final float value;

    public FloatNodeCalc(float value) {
        this.value = value;
    }

    @Override
    public double toDouble() {
        return value;
    }

    @Override
    public <R, A> R accept(NodeCalcVisitor<R, A> visitor, A arg, int depth) {
        return visitor.visit(this, arg);
    }

    @Override
    public <R, A> R acceptHandle(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> resultsStack) {
        return visitor.visit(this, arg);
    }

    @Override
    public <R, A> void acceptIterate(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> nodesStack) {
        // nothing to do
    }

    public float getValue() {
        return value;
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeNumberField(NAME, value);
    }

    static NodeCalc parseJson(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        if (token != null) {
            if (token == JsonToken.VALUE_NUMBER_FLOAT) {
                return new FloatNodeCalc(parser.getFloatValue());
            } else if (token == JsonToken.VALUE_NUMBER_INT) {
                return new FloatNodeCalc(parser.getIntValue());
            } else {
                throw NodeCalc.createUnexpectedToken(token);
            }
        }
        throw new TimeSeriesException("Invalid float node calc JSON");
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FloatNodeCalc floatNodeCalc) {
            return floatNodeCalc.value == value;
        }
        return false;
    }
}
