/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.powsybl.timeseries.DoubleTimeSeries;

import static com.powsybl.dynamicsimulation.DynamicSimulationResult.Status.SUCCESS;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public interface DynamicSimulationResult {

    enum Status {
        SUCCESS,
        FAILURE
    }

    Status getStatus();

    String getStatusText();

    /**
     * @deprecated use DynamicSimulationResult.Status instead
     */
    @Deprecated(since = "6.1.0")
    default boolean isOk() {
        return SUCCESS == getStatus();
    }

    Map<String, DoubleTimeSeries> getCurves();

    default DoubleTimeSeries getCurve(String curve) {
        return getCurves().get(curve);
    }

    /**
     * The Timeline contains information about relevant events that may have happened during the time domain simulation.
     */
    List<TimelineEvent> getTimeLine();

    /**
     * Returns an empty timeline.
     * @return an empty timeline
     */
    static List<TimelineEvent> emptyTimeLine() {
        return Collections.emptyList();
    }
}
