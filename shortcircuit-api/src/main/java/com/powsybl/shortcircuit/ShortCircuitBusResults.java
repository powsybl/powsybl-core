/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import java.util.Objects;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ShortCircuitBusResults {
    // FIXME
    // delta voltages are missing for the moment,
    // but not useful for the global design.

    private final String voltageLevelId;

    private final String busId;

    private final FortescueValue voltage;

    public ShortCircuitBusResults(String voltageLevelId, String busId, FortescueValue voltage) {
        this.voltageLevelId = voltageLevelId;
        this.busId = busId;
        this.voltage = Objects.requireNonNull(voltage);
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public String getBusId() {
        return busId;
    }

    public FortescueValue getVoltage() {
        return voltage;
    }
}
