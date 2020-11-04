/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import java.util.Map;

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
     * Each dynamic simulation engine could provide a set of fields that describe the event: as an example, an identifier 
     *  of the model where the event happened and a description of the kind of event.
     * The fields that describe the event would be available as a collection of TimeSeries.
     */
    Map<String, TimeSeries> getTimeLine();

    TimeSeries getTimeLine(String field);
}
