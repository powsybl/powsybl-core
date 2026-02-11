/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.regulation.*;

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

    @Deprecated(forRemoval = true, since = "7.2.0")
    enum RatioTapChangerRegulationMode {
        VOLTAGE,
        REACTIVE_POWER
    }

    /**
     * Use {@link #getVoltageRegulation()} and {@link VoltageRegulation#getMode()}
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    RatioTapChangerRegulationMode getRegulationMode();

    /**
     * Use {@link #getVoltageRegulation()} and {@link VoltageRegulation#setMode(RegulationMode)}
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    RatioTapChanger setRegulationMode(RatioTapChangerRegulationMode regulationMode);

    /**
     * Use {@link #getVoltageRegulation()} and {@link VoltageRegulation#getTargetValue()}
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    double getRegulationValue();

    /**
     * Use {@link #getVoltageRegulation()} and {@link VoltageRegulation#setTargetValue(Double)}
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    RatioTapChanger setRegulationValue(double regulationValue);

    /**
     * Get the target voltage in kV.
     * <p>
     * Returns NaN if the regulation mode is not {@link RatioTapChangerRegulationMode#VOLTAGE}.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getTargetV();

    /**
     * Set the target voltage in kV and sets regulating mode to {@link RatioTapChangerRegulationMode#VOLTAGE}.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     * @return itself for method chaining.
     */
    RatioTapChanger setTargetV(double targetV);
}
