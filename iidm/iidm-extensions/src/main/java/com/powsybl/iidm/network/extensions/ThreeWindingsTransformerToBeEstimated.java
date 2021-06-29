/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

import java.util.Set;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface ThreeWindingsTransformerToBeEstimated extends Extension<ThreeWindingsTransformer> {

    enum TapChanger {
        RATIO_TAP_CHANGER_1,
        PHASE_TAP_CHANGER_1,
        RATIO_TAP_CHANGER_2,
        PHASE_TAP_CHANGER_2,
        RATIO_TAP_CHANGER_3,
        PHASE_TAP_CHANGER_3
    }

    @Override
    default String getName() {
        return "threeWindingsTransformerToBeEstimated";
    }

    Set<TapChanger> getTapChangers();

    boolean toBeEstimated(TapChanger tapChanger);

    ThreeWindingsTransformerToBeEstimated addTapChanger(TapChanger tapChanger);

    ThreeWindingsTransformerToBeEstimated removeTapChanger(TapChanger tapChanger);

    default void cleanIfEmpty() {
        if (getTapChangers().isEmpty()) {
            getExtendable().removeExtension(ThreeWindingsTransformerToBeEstimated.class);
        }
    }
}
