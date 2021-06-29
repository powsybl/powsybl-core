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
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface TwoWindingsTransformerToBeEstimated extends Extension<TwoWindingsTransformer> {

    enum TapChanger {
        RATIO_TAP_CHANGER,
        PHASE_TAP_CHANGER
    }

    @Override
    default String getName() {
        return "twoWindingsTransformerTapChangersToBeEstimated";
    }

    Set<TapChanger> getTapChangersToBeEstimated();

    boolean tobeEstimated(TapChanger tapChanger);

    TwoWindingsTransformerToBeEstimated addTapChanger(TapChanger tapChanger);

    TwoWindingsTransformerToBeEstimated removeTapChanger(TapChanger tapChanger);

    default void cleanIfEmpty() {
        if (getTapChangersToBeEstimated().isEmpty()) {
            getExtendable().removeExtension(TwoWindingsTransformerToBeEstimated.class);
        }
    }
}
