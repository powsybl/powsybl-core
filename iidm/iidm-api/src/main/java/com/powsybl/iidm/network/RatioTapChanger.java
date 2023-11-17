/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A ratio tap changer that is associated to a transformer to control the voltage.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface RatioTapChanger extends TapChanger<RatioTapChanger, RatioTapChangerStep> {

    enum RegulationMode {
        VOLTAGE,
        REACTIVE_POWER_CONTROL
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
    RatioTapChanger setRegulationMode(RatioTapChanger.RegulationMode regulationMode);

    /**
     * Get the regulation value.
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
     */
    RatioTapChanger setRegulationValue(double regulationValue);


    /**
     * Get the target voltage in kV. Depends on the working variant.
     * @see VariantManager
     */
    double getTargetV();


    /**
     * Set the target voltage in kV. Depends on the working variant.
     * @see VariantManager
     */
    RatioTapChanger setTargetV(double targetV);

    /**
     * Get the load tap changing capabilities status.
     */
    boolean hasLoadTapChangingCapabilities();

    /**
     * Set the load tap changing capabilities status.
     */
    RatioTapChanger setLoadTapChangingCapabilities(boolean status);
}
