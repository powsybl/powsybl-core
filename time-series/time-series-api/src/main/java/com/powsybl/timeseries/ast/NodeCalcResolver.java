/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NodeCalcResolver extends NodeCalcCloner<Void> {

    private final Map<String, Integer> timeSeriesNums;
    private final TimeSeriesNumNodeCalc[] timeSeriesNumNodeCalcs;

    public NodeCalcResolver(Map<String, Integer> timeSeriesNums) {
        this.timeSeriesNums = Objects.requireNonNull(timeSeriesNums);
        this.timeSeriesNumNodeCalcs = new TimeSeriesNumNodeCalc[timeSeriesNums.size()];
    }

    public static NodeCalc resolve(NodeCalc nodeCalc, Map<String, Integer> timeSeriesNums) {
        Objects.requireNonNull(nodeCalc);
        return nodeCalc.accept(new NodeCalcResolver(timeSeriesNums), null, 0, null);
    }

    @Override
    public NodeCalc visit(TimeSeriesNameNodeCalc nodeCalc, Void arg) {
        Integer num = timeSeriesNums.get(nodeCalc.getTimeSeriesName());
        if (num == null) {
            throw new IllegalStateException("Num of time series " + nodeCalc.getTimeSeriesName() + " not found");
        }
        TimeSeriesNumNodeCalc timeSeriesNumNodeCalc = timeSeriesNumNodeCalcs[num];
        if (timeSeriesNumNodeCalc == null) {
            timeSeriesNumNodeCalc = new TimeSeriesNumNodeCalc(num);
            timeSeriesNumNodeCalcs[num] = timeSeriesNumNodeCalc;
        }
        return timeSeriesNumNodeCalc;
    }
}
