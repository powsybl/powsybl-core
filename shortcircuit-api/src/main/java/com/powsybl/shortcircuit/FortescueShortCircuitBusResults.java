/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

/**
 * Results detailed on the three phases of the voltages on a bus.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class FortescueShortCircuitBusResults extends AbstractShortCircuitBusResults {

    private final FortescueValue voltage;

    public FortescueShortCircuitBusResults(String voltageLevelId,
                                           String busId,
                                           double initialVoltageMagnitude,
                                           FortescueValue voltage,
                                           double voltageDropProportional) {
        super(voltageLevelId, busId, initialVoltageMagnitude, voltageDropProportional);
        this.voltage = voltage;
    }

    /**
     * Returns the voltage on the three phases after the fault [in kV].
     */
    public FortescueValue getVoltage() {
        return voltage;
    }

}
