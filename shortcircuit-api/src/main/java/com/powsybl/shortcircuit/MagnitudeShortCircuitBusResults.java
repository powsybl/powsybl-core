/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

/**
 * Three-phase voltage results on a bus after the short-circuit computation.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class MagnitudeShortCircuitBusResults extends AbstractShortCircuitBusResults {

    private final double voltage;

    public MagnitudeShortCircuitBusResults(String voltageLevelId,
                                           String busId, double initialVoltageMagnitude,
                                           double voltage,
                                           double voltageDropProportional) {
        super(voltageLevelId, busId, initialVoltageMagnitude, voltageDropProportional);
        this.voltage = voltage;
    }

    /**
     * Returns the three phased voltage magnitude after the fault (in kV).
     */
    public double getVoltage() {
        return voltage;
    }

}
