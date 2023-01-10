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

    private final String voltageLevelId;

    private final String busId;

    private final double initialVoltageMagnitude;

    private final FortescueValue voltage;

    private final double voltageDropProportional;

    public ShortCircuitBusResults(String voltageLevelId,
                                  String busId,
                                  double initialVoltageMagnitude,
                                  FortescueValue voltage,
                                  double voltageDropProportional) {
        this.voltageLevelId = voltageLevelId;
        this.busId = busId;
        this.initialVoltageMagnitude = initialVoltageMagnitude;
        this.voltage = voltage;
        this.voltageDropProportional = voltageDropProportional;
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public String getBusId() {
        return busId;
    }

    public double getInitialVoltageMagnitude() {
        return initialVoltageMagnitude;
    }

    public FortescueValue getVoltage() {
        return voltage;
    }

    public double getVoltageDropProportional() {
        return voltageDropProportional;
    }
}
