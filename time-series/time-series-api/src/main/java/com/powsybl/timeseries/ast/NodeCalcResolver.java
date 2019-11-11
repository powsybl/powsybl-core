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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeCalcResolver extends NodeCalcCloner<Void> {

    private final Map<String, Integer> timeSeriesNums;

    public NodeCalcResolver(Map<String, Integer> timeSeriesNums) {
        this.timeSeriesNums = Objects.requireNonNull(timeSeriesNums);
    }

    public static NodeCalc resolve(NodeCalc nodeCalc, Map<String, Integer> timeSeriesNums) {
        Objects.requireNonNull(nodeCalc);
        return nodeCalc.accept(new NodeCalcResolver(timeSeriesNums), null, 0);
    }

    @Override
    public NodeCalc visit(TimeSeriesNameNodeCalc nodeCalc, Void arg) {
        Integer num = timeSeriesNums.get(nodeCalc.getTimeSeriesName());
        if (num == null) {
            throw new AssertionError("Num of time series " + nodeCalc.getTimeSeriesName() + " not found");
        }
        return new TimeSeriesNumNodeCalc(num);
    }
}
