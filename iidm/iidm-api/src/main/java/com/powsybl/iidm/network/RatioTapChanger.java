/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * A ratio tap changer that is associated to a transformer to control the voltage or reactive power.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface RatioTapChanger extends TapChanger<
    RatioTapChanger,
    RatioTapChangerStep,
    RatioTapChangerStepsReplacer,
    RatioTapChangerStepsReplacer.StepAdder> {

    enum RegulationMode {
        VOLTAGE,
        REACTIVE_POWER
    }

    /**
     * Get the regulation mode.
     * Supported modes are {@link RegulationMode#VOLTAGE} and {@link RegulationMode#REACTIVE_POWER}.
     * @return the regulation mode.
     */
    RegulationMode getRegulationMode();

    /**
     * Set the regulation mode.
     * Supported modes are {@link RegulationMode#VOLTAGE} and {@link RegulationMode#REACTIVE_POWER}.
     * @param regulationMode the regulation mode.
     * @return itself for method chaining
     */
    RatioTapChanger setRegulationMode(RatioTapChanger.RegulationMode regulationMode);


    /**
     * Get the regulation value.
     *   - a setpoint in kV in case of {@link RegulationMode#VOLTAGE} regulation
     *   - a setpoint in MVar in case of {@link RegulationMode#REACTIVE_POWER} regulation
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getRegulationValue();

    /**
     * Set the regulation value.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     * @param regulationValue the regulation value.
     * @return itself for method chaining.
     */
    RatioTapChanger setRegulationValue(double regulationValue);

    /**
     * Get the target voltage in kV.
     * <p>
     * Returns NaN if the regulation mode is not {@link RegulationMode#VOLTAGE}.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getTargetV();

    /**
     * Set the target voltage in kV and sets regulating mode to {@link RegulationMode#VOLTAGE}.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     * @return itself for method chaining.
     */
    RatioTapChanger setTargetV(double targetV);
}
