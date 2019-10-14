/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.timeseries.TimeSeriesException;
import com.powsybl.timeseries.ast.NodeCalcVisitors.NodeWrapper;

import java.util.Deque;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeSeriesNumNodeCalc implements NodeCalc {

    private final int timeSeriesNum;

    public TimeSeriesNumNodeCalc(int timeSeriesNum) {
        if (timeSeriesNum <  0) {
            throw new IllegalArgumentException("time series num cannot be negative");
        }
        this.timeSeriesNum = timeSeriesNum;
    }

    public int getTimeSeriesNum() {
        return timeSeriesNum;
    }

    @Override
    public <R, A> R acceptVisit(NodeCalcVisitor<R, A> visitor, A arg, Deque<Optional<R>> children) {
        return visitor.visit(this, arg);
    }

    @Override
    public <R, A> void acceptIterate(NodeCalcVisitor<R, A> visitor, A arg, Deque<NodeWrapper> visitQueue) {
        // nothing to do
    }

    @Override
    public void writeJson(JsonGenerator generator) {
        throw new TimeSeriesException("Resolved node calc cannot be serialized to JSON");
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(timeSeriesNum);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeSeriesNumNodeCalc) {
            return ((TimeSeriesNumNodeCalc) obj).timeSeriesNum == timeSeriesNum;
        }
        return false;
    }
}
