/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import java.util.Map;

import com.powsybl.timeseries.RegularTimeSeriesIndex;
import com.powsybl.timeseries.StringTimeSeries;
import com.powsybl.timeseries.TimeSeries;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public interface DynamicSimulationResult {

    boolean isOk();

    String getLogs();

    Map<String, TimeSeries> getCurves();

    TimeSeries getCurve(String curve);

    /**
     * The Timeline contains information about relevant events that may have happened during the time domain simulation.
     */
    StringTimeSeries getTimeLine();

    /**
     * Returns an empty timeline.
     * @return an empty timeline
     */
    static StringTimeSeries emptyTimeLine() {
        return TimeSeries.createString("timeLine", new RegularTimeSeriesIndex(0, 0, 0));
    }
}
