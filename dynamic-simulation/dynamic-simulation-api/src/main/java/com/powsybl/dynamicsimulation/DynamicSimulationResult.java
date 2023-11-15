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

import static com.powsybl.dynamicsimulation.DynamicSimulationResult.Status.SUCCEED;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public interface DynamicSimulationResult {

    enum Status {
        SUCCEED,
        FAILED
    }

    Status getStatus();

    String getError();

    //TODO remove or flag as deprecated ?
    default boolean isOk() {
        return SUCCEED == getStatus();
    }

    Map<String, TimeSeries> getCurves();

    default TimeSeries getCurve(String curve) {
        return getCurves().get(curve);
    }

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
