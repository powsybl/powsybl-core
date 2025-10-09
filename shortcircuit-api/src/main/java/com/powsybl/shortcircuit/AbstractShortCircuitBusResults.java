/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
abstract class AbstractShortCircuitBusResults implements ShortCircuitBusResults {

    private final String voltageLevelId;

    private final String busId;

    private final double initialVoltageMagnitude;

    private final double voltageDropProportional;

    protected AbstractShortCircuitBusResults(String voltageLevelId,
                                             String busId, double initialVoltageMagnitude,
                                             double voltageDropProportional) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
        this.busId = Objects.requireNonNull(busId);
        this.initialVoltageMagnitude = initialVoltageMagnitude;
        this.voltageDropProportional = voltageDropProportional;
    }

    @Override
    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    @Override
    public String getBusId() {
        return busId;
    }

    @Override
    public double getInitialVoltageMagnitude() {
        return initialVoltageMagnitude;
    }

    @Override
    public double getVoltageDropProportional() {
        return voltageDropProportional;
    }

}
