/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

/**
 * Indicate for a state estimation which tap changers are to be estimated (i.e. their tap positions should be outputs).
 * If a tap changer is not to be estimated, it should not be changed during a state estimation (i.e its tap position is only an input).
 *
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface ThreeWindingsTransformerToBeEstimated extends Extension<ThreeWindingsTransformer> {

    @Override
    default String getName() {
        return "threeWindingsTransformerToBeEstimated";
    }

    /**
     * Return true if the ratio tap changer of the leg 1 of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean containsRatioTapChanger1();

    /**
     * Return true if the ratio tap changer of the leg 2 of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean containsRatioTapChanger2();

    /**
     * Return true if the ratio tap changer of the leg 3 of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean containsRatioTapChanger3();

    /**
     * Return true if the ratio tap changer of the leg of the given side of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean containsRatioTapChanger(ThreeWindingsTransformer.Side side);

    /**
     * Return true if the phase tap changer of the leg 1 of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean containsPhaseTapChanger1();

    /**
     * Return true if the phase tap changer of the leg 2 of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean containsPhaseTapChanger2();

    /**
     * Return true if the phase tap changer of the leg 3 of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean containsPhaseTapChanger3();


    /**
     * Return true if the phase tap changer of the leg of the given side of the extended three windings transformer is to be estimated during a state estimation.
     * Else, return false.
     */
    boolean containsPhaseTapChanger(ThreeWindingsTransformer.Side side);

    /**
     * Specify if the ratio tap changer of the leg 1 of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated setRatioTapChanger1Status(boolean toBeEstimated);

    /**
     * Specify if the ratio tap changer of the leg 2 of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated setRatioTapChanger2Status(boolean toBeEstimated);

    /**
     * Specify if the ratio tap changer of the leg 3 of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated setRatioTapChanger3Status(boolean toBeEstimated);

    /**
     * Specify if the ratio tap changer of the leg of the given side of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated setRatioTapChangerStatus(boolean toBeEstimated, ThreeWindingsTransformer.Side side);

    /**
     * Specify if the phase tap changer of the leg 1 of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated setPhaseTapChanger1Status(boolean toBeEstimated);

    /**
     * Specify if the phase tap changer of the leg 2 of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated setPhaseTapChanger2Status(boolean toBeEstimated);

    /**
     * Specify if the phase tap changer of the leg 3 of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated setPhaseTapChanger3Status(boolean toBeEstimated);

    /**
     * Specify if the phase tap changer of the leg of the given side of the extended three windings transformer is to be estimated during a state estimation.
     */
    ThreeWindingsTransformerToBeEstimated setPhaseTapChangerStatus(boolean toBeEstimated, ThreeWindingsTransformer.Side side);
}
