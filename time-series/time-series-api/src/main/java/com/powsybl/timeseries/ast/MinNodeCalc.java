/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.util.Deque;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MinNodeCalc extends AbstractMinMaxNodeCalc {

    static final String NAME = "min";

    public MinNodeCalc(NodeCalc child, double min) {
        super(child, min);
    }

    public double getMin() {
        return value;
    }

    @Override
    public <R, A> R acceptVisit(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> children) {
        Object o = children.pop();
        o = o == NodeCalcVisitors.NULL ? null : o;
        return visitor.visit(this, arg, (R) o);
    }

    @Override
    public <R, A> void acceptIterate(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> visitQueue) {
        NodeCalc n = visitor.iterate(this, arg);
        visitQueue.push(n == null ? NodeCalcVisitors.NULL : n);
    }

    @Override
    protected String getJsonName() {
        return NAME;
    }

    static NodeCalc parseJson(JsonParser parser) throws IOException {
        ParsingContext context = parseJson2(parser);
        return new MinNodeCalc(context.child, context.value);
    }

    @Override
    public int hashCode() {
        return child.hashCode() + Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MinNodeCalc) {
            return ((MinNodeCalc) obj).child.equals(child) && ((MinNodeCalc) obj).value == value;
        }
        return false;
    }
}
