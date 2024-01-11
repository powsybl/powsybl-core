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
public interface RatioTapChanger extends TapChanger<
    RatioTapChanger,
    RatioTapChangerStep,
    RatioTapChangerStepsReplacer,
    RatioTapChangerStepsReplacerStepAdder> {

    /**
     * Get the target voltage in kV.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getTargetV();

    /**
     * Set the target voltage in kV.
     * <p>
     * Depends on the working variant.
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
