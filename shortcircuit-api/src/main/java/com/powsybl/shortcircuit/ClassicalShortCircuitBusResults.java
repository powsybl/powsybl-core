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
public class ClassicalShortCircuitBusResults extends AbstractShortCircuitBusResults {

    private final double initialVoltageMagnitude;

    private final FortescueValue voltage;

    public ClassicalShortCircuitBusResults(String voltageLevelId,
                                           String busId,
                                           double initialVoltageMagnitude,
                                           FortescueValue voltage,
                                           double voltageDropProportional) {
        super(voltageLevelId, busId, voltageDropProportional);
        this.initialVoltageMagnitude = initialVoltageMagnitude;
        this.voltage = voltage;
    }

    public FortescueValue getVoltage() {
        return voltage;
    }

    public double getInitialVoltageMagnitude() {
        return initialVoltageMagnitude;
    }

}
