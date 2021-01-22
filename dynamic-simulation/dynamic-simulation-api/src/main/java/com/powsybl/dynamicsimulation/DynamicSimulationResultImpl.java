/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.powsybl.timeseries.StringTimeSeries;
import com.powsybl.timeseries.TimeSeries;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class DynamicSimulationResultImpl implements DynamicSimulationResult {

    private final boolean status;
    private final String logs;
    private final Map<String, TimeSeries> curves;
    private final StringTimeSeries timeLine;

    public DynamicSimulationResultImpl(boolean status, String logs, Map<String, TimeSeries> curves, StringTimeSeries timeLine) {
        this.status = status;
        this.logs = logs;
        this.curves = Objects.requireNonNull(curves);
        this.timeLine = Objects.requireNonNull(timeLine);
    }

    @Override
    public boolean isOk() {
        return status;
    }

    @Override
    public String getLogs() {
        return logs;
    }

    @Override
    public Map<String, TimeSeries> getCurves() {
        return Collections.unmodifiableMap(curves);
    }

    @Override
    public TimeSeries getCurve(String curve) {
        return curves.get(curve);
    }

    @Override
    public StringTimeSeries getTimeLine() {
        return timeLine;
    }
}
