/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ShortCircuitBusResults {
    // FIXME: delta voltages are missing for the moment, but not useful or the global design.

    private final String voltageLevelId;

    private final String busId;

    private DetailedShortCircuitValue voltage;

    public ShortCircuitBusResults(String voltageLevelId, String busId, DetailedShortCircuitValue voltage) {
        this.voltageLevelId = voltageLevelId;
        this.busId = busId;
        this.voltage = voltage;
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public String getBusId() {
        return busId;
    }

    public DetailedShortCircuitValue getVoltage() {
        return voltage;
    }
}
