/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.TwoWindingsTransformer;

import java.util.Set;

/**
 * Indicate for a state estimation which tap changers are to be estimated (i.e. their tap positions should be outputs).
 * If a tap changer is not to be estimated, it should not be changed during a state estimation (i.e its tap position is only an input).
 *
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface TwoWindingsTransformerToBeEstimated extends Extension<TwoWindingsTransformer> {

    /**
     * A two windings transformer can have a ratio tap changer and/or a phase tap changer that can be estimated during a state estimation.
     */
    enum TapChanger {
        RATIO_TAP_CHANGER,
        PHASE_TAP_CHANGER
    }

    @Override
    default String getName() {
        return "twoWindingsTransformerToBeEstimated";
    }

    /**
     * Get tap changers that are to be estimated during a state estimation.
     */
    Set<TapChanger> getTapChangers();

    /**
     * For a given tap changer, return true if it is to be estimated during a state estimation. Else, return false.
     */
    boolean tobeEstimated(TapChanger tapChanger);

    /**
     * Add a given tap changer to be estimated during state estimation.
     * If this tap changer is already considered to be estimated, do nothing.
     */
    TwoWindingsTransformerToBeEstimated addTapChanger(TapChanger tapChanger);

    /**
     * Remove a given tap changer to the list of tap changers to be estimated during state estimation.
     * If this tap changer is not considered to be estimated, do nothing.
     */
    TwoWindingsTransformerToBeEstimated removeTapChanger(TapChanger tapChanger);

    /**
     * Remove the extension if no tap changer is specified to be estimated.
     */
    default void cleanIfEmpty() {
        if (getTapChangers().isEmpty()) {
            getExtendable().removeExtension(TwoWindingsTransformerToBeEstimated.class);
        }
    }
}
