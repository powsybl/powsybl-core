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
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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
    public <R, A> R accept(NodeCalcVisitor<R, A> visitor, A arg, int depth, Map<Integer, R> cache) {
        if (depth < NodeCalcVisitors.RECURSION_THRESHOLD) {
            if (visitor instanceof NodeCalcEvaluator) {
                if (cache.containsKey(this.hashCode())) {
                    return cache.get(this.hashCode());
                } else {
                    R result = acceptNotCached(visitor, arg, depth, cache);
                    cache.put(this.hashCode(), result);
                    return result;
                }
            } else {
                return acceptNotCached(visitor, arg, depth, cache);
            }
        } else {
            return NodeCalcVisitors.visit(this, arg, visitor);
        }
    }

    public <R, A> R acceptNotCached(NodeCalcVisitor<R, A> visitor, A arg, int depth, Map<Integer, R> cache) {
        NodeCalc child = visitor.iterate(this, arg);
        R childValue = null;
        if (child != null) {
            childValue = child.accept(visitor, arg, depth + 1, cache);
        }
        return visitor.visit(this, arg, childValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R, A> R acceptHandle(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> resultsStack) {
        Object childResult = resultsStack.pop();
        childResult = childResult == NodeCalcVisitors.NULL ? null : childResult;
        return visitor.visit(this, arg, (R) childResult);
    }

    @Override
    public <R, A> void acceptIterate(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> nodesStack) {
        NodeCalc childNode = visitor.iterate(this, arg);
        nodesStack.push(childNode == null ? NodeCalcVisitors.NULL : childNode);
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
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MinNodeCalc minNodeCalc) {
            return minNodeCalc.child.equals(child) && minNodeCalc.value == value;
        }
        return false;
    }
}
