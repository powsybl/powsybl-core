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
public class SimpleShortCircuitBusResults extends AbstractShortCircuitBusResults {

    private final double voltage;

    public SimpleShortCircuitBusResults(String voltageLevelId,
                                        String busId,
                                        double voltage,
                                        double voltageDropProportional) {
        super(voltageLevelId, busId, voltageDropProportional);
        this.voltage = voltage;
    }

    /**
     * Returns the 3-phase voltage magnitude after the fault (in V).
     */
    public double getVoltage() {
        return voltage;
    }

}
