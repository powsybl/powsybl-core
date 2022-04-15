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
    // FIXME: delta voltages are missing for the moment,
    //  but not useful for the global design.

    private final String voltageLevelId;

    private final String busId;

    private ThreePhaseValue voltage;

    private ThreePhaseValue current;

    public ShortCircuitBusResults(String voltageLevelId, String busId, ThreePhaseValue voltage) {
        this(voltageLevelId, busId, voltage, null);
    }

    public ShortCircuitBusResults(String voltageLevelId, String busId, ThreePhaseValue voltage, ThreePhaseValue current) {
        this.voltageLevelId = voltageLevelId;
        this.busId = busId;
        this.voltage = voltage;
        this.current = current;
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public String getBusId() {
        return busId;
    }

    public ThreePhaseValue getVoltage() {
        return voltage;
    }

    public ThreePhaseValue getCurrent() {
        return current;
    }
}
