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

    // VERSION = 1.0 voltageLevelId, busId, voltage
    // VERSION = 1.1 voltageDropProportional
    public static final String VERSION = "1.1";

    private final String voltageLevelId;

    private final String busId;

    private final FortescueValue voltage;

    private final double voltageDropProportional;

    public ShortCircuitBusResults(String voltageLevelId,
                                  String busId,
                                  FortescueValue voltage,
                                  double voltageDropProportional) {
        this.voltageLevelId = voltageLevelId;
        this.busId = busId;
        this.voltage = voltage;
        this.voltageDropProportional = voltageDropProportional;
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

    public double getVoltageDropProportional() {
        return voltageDropProportional;
    }
}
