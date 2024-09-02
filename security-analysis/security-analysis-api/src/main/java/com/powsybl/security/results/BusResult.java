/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.powsybl.commons.extensions.AbstractExtendable;

import java.util.Objects;

/**
 * Contains state variables of a bus which is monitored during security analysis through a {@link com.powsybl.security.monitor.StateMonitor}.
 * Note that the busId depends on the topology used for the computation: it can be the id of the bus from the bus view
 * or the id of the bus from the bus/breaker view or the id of the bus bar section if the network is in node/breaker
 * topology. The voltage id is provided by the {@link com.powsybl.security.monitor.StateMonitor}.
 * The state variables supported are the voltage angle and the voltage magnitude at the bus.
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class BusResult extends AbstractExtendable<BusResult> {

    private final String voltageLevelId;

    private final String busId;

    private final double v;

    private final double angle;

    public BusResult(String voltageLevelId, String busId, double v, double angle) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
        this.busId = Objects.requireNonNull(busId);
        this.v = v;
        this.angle = angle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BusResult that = (BusResult) o;
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
