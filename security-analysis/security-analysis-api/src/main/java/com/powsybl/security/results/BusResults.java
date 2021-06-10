/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.results;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 *
 * provide electrical information on a bus after a security analysis.
 * it belongs to pre and post Contingency results.
 * it is the result of the bus id specified in StateMonitor.
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class BusResults {

    private final String voltageLevelId;

    private final String busId;

    private final double v;

    private final double angle;

    @JsonCreator
    public BusResults(@JsonProperty("voltageLevelId") String voltageLevelId, @JsonProperty("busId") String busId,
                      @JsonProperty("v") double v, @JsonProperty("angle") double angle) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
        this.busId = Objects.requireNonNull(busId);
        this.v = Objects.requireNonNull(v);
        this.angle = Objects.requireNonNull(angle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BusResults that = (BusResults) o;
        return Double.compare(that.v, v) == 0 &&
            Double.compare(that.angle, angle) == 0 &&
            Objects.equals(voltageLevelId, that.voltageLevelId) &&
            Objects.equals(busId, that.busId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(voltageLevelId, busId, v, angle);
    }

    public String getBusId() {
        return busId;
    }

    public double getAngle() {
        return angle;
    }

    public double getV() {
        return v;
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    @Override
    public String toString() {
        return "BusResults{" +
            "voltageLevelId='" + voltageLevelId + '\'' +
            ", busId='" + busId + '\'' +
            ", v=" + v +
            ", angle=" + angle +
            '}';
    }
}
