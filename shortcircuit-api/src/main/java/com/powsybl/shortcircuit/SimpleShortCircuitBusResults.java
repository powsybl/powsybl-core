/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class SimpleShortCircuitBusResults {

    public static final String VERSION = "1.0";

    private final String voltageLevelId;

    private final String busId;

    private final double voltage;

    private final double voltageDropProportional;

    public SimpleShortCircuitBusResults(String voltageLevelId,
                                  String busId,
                                  double voltage,
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

    public double getVoltage() {
        return voltage;
    }

    public double getVoltageDropProportional() {
        return voltageDropProportional;
    }
}
