/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Deque;
import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class CachedNodeCalc extends AbstractSingleChildNodeCalc {

    static final String NAME = "cached";

    public CachedNodeCalc(NodeCalc child) {
        super(child);
    }

    @Override
    public <R, A> R accept(NodeCalcVisitor<R, A> visitor, A arg, int depth) {
        if (depth < NodeCalcVisitors.RECURSION_THRESHOLD) {
            NodeCalc child = visitor.iterate(this, arg);
            R childValue = null;
            if (child != null) {
                childValue = child.accept(visitor, arg, depth + 1);
            }
            return visitor.visit(this, arg, childValue);
        } else {
            return NodeCalcVisitors.visit(this, arg, visitor);
        }
    }

    @Override
    public <R, A> void acceptIterate(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> nodesStack) {
        NodeCalc childNode = visitor.iterate(this, arg);
        nodesStack.push(childNode == null ? NodeCalcVisitors.NULL : childNode);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R, A> R acceptHandle(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> resultsStack) {
        Object childResult = resultsStack.pop();
        childResult = childResult == NodeCalcVisitors.NULL ? null : childResult;
        return visitor.visit(this, arg, (R) childResult);
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        child.writeJson(generator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME, child);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CachedNodeCalc cachedNodeCalc) {
            return cachedNodeCalc.child.equals(child);
        }
        return false;
    }
}
