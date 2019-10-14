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
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeNodeCalc extends AbstractSingleChildNodeCalc {

    static final String NAME = "time";

    public TimeNodeCalc(NodeCalc child) {
        super(child);
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
        child.writeJson(generator);
        generator.writeEndObject();
    }

    static NodeCalc parseJson(JsonParser parser) throws IOException {
        NodeCalc child = null;
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            if (token == JsonToken.START_OBJECT) {
                // skip
            } else if (token == JsonToken.END_OBJECT) {
                if (child == null) {
                    throw new TimeSeriesException("Invalid time node calc JSON");
                }
                return new TimeNodeCalc(child);
            } else if (token == JsonToken.FIELD_NAME) {
                child = NodeCalc.parseJson(parser, token);
            } else {
                throw NodeCalc.createUnexpectedToken(token);
            }
        }
        throw NodeCalc.createUnexpectedToken(token);
    }

    @Override
    public int hashCode() {
        return child.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeNodeCalc) {
            return ((TimeNodeCalc) obj).child.equals(child);
        }
        return false;
    }
}
