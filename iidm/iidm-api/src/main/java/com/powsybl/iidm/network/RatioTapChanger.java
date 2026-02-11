/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

/**
 * A ratio tap changer that is associated to a transformer to control the voltage or reactive power.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface RatioTapChanger extends TapChanger<
    RatioTapChanger,
    RatioTapChangerStep,
    RatioTapChangerStepsReplacer,
    RatioTapChangerStepsReplacer.StepAdder>,
    VoltageRegulationHolder {

    enum RegulationMode {
        VOLTAGE,
        REACTIVE_POWER
    }

    /**
     * Get the regulation mode.
     * Supported modes are {@link com.powsybl.iidm.network.regulation.RegulationMode#VOLTAGE} and {@link com.powsybl.iidm.network.regulation.RegulationMode#REACTIVE_POWER}.
     * @return the regulation mode.
     */
    RegulationMode getRegulationMode();

    /**
     * Set the regulation mode.
     * Supported modes are {@link com.powsybl.iidm.network.regulation.RegulationMode#VOLTAGE} and {@link com.powsybl.iidm.network.regulation.RegulationMode#REACTIVE_POWER}.
     * @param regulationMode the regulation mode.
     * @return itself for method chaining
     */
    RatioTapChanger setRegulationMode(RegulationMode regulationMode);

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
     * Returns NaN if the regulation mode is not {@link com.powsybl.iidm.network.regulation.RegulationMode#VOLTAGE}.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getTargetV();

    /**
     * Set the target voltage in kV and sets regulating mode to {@link com.powsybl.iidm.network.regulation.RegulationMode#VOLTAGE}.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     * @return itself for method chaining.
     */
    RatioTapChanger setTargetV(double targetV);
}
