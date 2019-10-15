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
public class MaxNodeCalc extends AbstractMinMaxNodeCalc {

    static final String NAME = "max";

    public MaxNodeCalc(NodeCalc child, double max) {
        super(child, max);
    }

    public double getMax() {
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
        return new MaxNodeCalc(context.child, context.value);
    }

    @Override
    public int hashCode() {
        return child.hashCode() + Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MaxNodeCalc) {
            return ((MaxNodeCalc) obj).child.equals(child) && ((MaxNodeCalc) obj).value == value;
        }
        return false;
    }
}
