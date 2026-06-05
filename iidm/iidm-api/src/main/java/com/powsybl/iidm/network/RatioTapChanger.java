/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
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

    /**
     * Get the regulation mode.
     * Supported modes are {@link com.powsybl.iidm.network.regulation.RegulationMode#VOLTAGE} and {@link com.powsybl.iidm.network.regulation.RegulationMode#REACTIVE_POWER}.
     * @return the regulation mode.
     * @deprecated use {@link VoltageRegulation#getMode()} instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    RegulationMode getRegulationMode();

    /**
     * Set the regulation mode.
     * Supported modes are {@link com.powsybl.iidm.network.regulation.RegulationMode#VOLTAGE} and {@link com.powsybl.iidm.network.regulation.RegulationMode#REACTIVE_POWER}.
     * @param regulationMode the regulation mode.
     * @return itself for method chaining
     * @deprecated use {@link #newVoltageRegulation()} with {@link com.powsybl.iidm.network.regulation.VoltageRegulationAdderOrBuilder#withMode(RegulationMode)} instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    RatioTapChanger setRegulationMode(RegulationMode regulationMode);

    /**
     * @deprecated use {@link VoltageRegulation#getTargetValue()} instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    double getRegulationValue();

    /**
     * Set the regulation value.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     * @param regulationValue the regulation value.
     * @return itself for method chaining.
     * @deprecated use {@link VoltageRegulation#setTargetValue(double)} instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    RatioTapChanger setRegulationValue(double regulationValue);

    /**
     * Get the target voltage in kV.
     * <p>
     * Returns NaN if the regulation mode is not {@link RegulationMode#VOLTAGE}.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     * @deprecated use {@link VoltageRegulation#getTargetValue()} instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    double getTargetV();

    /**
     * Set the target voltage in kV and sets regulating mode to {@link com.powsybl.iidm.network.regulation.RegulationMode#VOLTAGE}.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     * @return itself for method chaining.
     * @deprecated use {@link VoltageRegulation#setTargetValue(double)} instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    RatioTapChanger setTargetV(double targetV);

    @Override
    default boolean isRegulating() {
        return VoltageRegulationHolder.super.isRegulating();
    }

    /**
     * Get the tap changer's deadband (in kV) used to avoid excessive update of discrete control while regulating.
     * This attribute is necessary only if the tap changer is regulating.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     * @deprecated use {@link VoltageRegulation#getTargetDeadband()} instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    default double getTargetDeadband() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the tap changer's deadband (in kV) used to avoid excessive update of discrete control while regulating.
     * This attribute is necessary only if the tap changer is regulating. It must be positive.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     * @deprecated use {@link VoltageRegulation#setTargetDeadband(double)}  instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    default RatioTapChanger setTargetDeadband(double targetDeadband) {
        throw new UnsupportedOperationException();
    }
}
