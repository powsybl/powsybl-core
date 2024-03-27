/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

/**
 * Indicate for a state estimation which tap changers are to be estimated (i.e. their tap positions should be outputs).
 * If a tap changer is not to be estimated, it should not be changed during a state estimation (i.e its tap position is only an input).
 *
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface ThreeWindingsTransformerToBeEstimated extends Extension<ThreeWindingsTransformer> {

    String NAME = "threeWindingsTransformerToBeEstimated";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Return true if the ratio tap changer of the leg 1 of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean shouldEstimateRatioTapChanger1();

    /**
     * Return true if the ratio tap changer of the leg 2 of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean shouldEstimateRatioTapChanger2();

    /**
     * Return true if the ratio tap changer of the leg 3 of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean shouldEstimateRatioTapChanger3();

    /**
     * Return true if the ratio tap changer of the leg of the given side of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean shouldEstimateRatioTapChanger(ThreeSides side);

    /**
     * Return true if the phase tap changer of the leg 1 of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean shouldEstimatePhaseTapChanger1();

    /**
     * Return true if the phase tap changer of the leg 2 of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean shouldEstimatePhaseTapChanger2();

    /**
     * Return true if the phase tap changer of the leg 3 of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean shouldEstimatePhaseTapChanger3();


    /**
     * Return true if the phase tap changer of the leg of the given side of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean shouldEstimatePhaseTapChanger(ThreeSides side);

    /**
     * Specify if the ratio tap changer of the leg 1 of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger1(boolean toBeEstimated);

    /**
     * Specify if the ratio tap changer of the leg 2 of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger2(boolean toBeEstimated);

    /**
     * Specify if the ratio tap changer of the leg 3 of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger3(boolean toBeEstimated);

    /**
     * Specify if the ratio tap changer of the leg of the given side of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger(boolean toBeEstimated, ThreeSides side);

    /**
     * Specify if the phase tap changer of the leg 1 of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger1(boolean toBeEstimated);

    /**
     * Specify if the phase tap changer of the leg 2 of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger2(boolean toBeEstimated);

    /**
     * Specify if the phase tap changer of the leg 3 of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger3(boolean toBeEstimated);

    /**
     * Specify if the phase tap changer of the leg of the given side of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger(boolean toBeEstimated, ThreeSides side);
}
