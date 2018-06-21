/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A phase tap changer that is associated to a transformer to control the phase.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface PhaseTapChanger extends TapChanger<PhaseTapChanger, PhaseTapChangerStep> {

    enum RegulationMode {
        CURRENT_LIMITER,
        ACTIVE_POWER_CONTROL,
        FIXED_TAP
    }

    /**
     * Get the regulation mode.
     * @return the regulation mode
     */
    RegulationMode getRegulationMode();

    /**
     * Set the regulation mode
     * @param regulationMode the regulation mode
     * @return itself for method chaining
     */
    PhaseTapChanger setRegulationMode(RegulationMode regulationMode);

    /**
     * Get the regulation value.
     *   - a threshold in A in case of current limiter regulation
     *   - a setpoint in MW in case of active power control regulation
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    double getRegulationValue();

    /**
     * Set the regulation value.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    PhaseTapChanger setRegulationValue(double regulationValue);

}
