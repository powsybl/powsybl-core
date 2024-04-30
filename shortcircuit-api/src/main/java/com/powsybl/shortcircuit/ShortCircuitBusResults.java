/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public interface ShortCircuitBusResults {

    String getVoltageLevelId();

    String getBusId();

    /**
     * Returns the magnitude of the voltage before the fault (in kV).
     */
    double getInitialVoltageMagnitude();

    /**
     * Returns the voltage drop between the voltage magnitude before and after the fault, as a percentage.
     */
    double getVoltageDropProportional();

}
