/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * Indicate for a state estimation which tap changers are to be estimated (i.e. their tap positions should be outputs).
 * If a tap changer is not to be estimated, it should not be changed during a state estimation (i.e its tap position is only an input).
 *
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface TwoWindingsTransformerToBeEstimated extends Extension<TwoWindingsTransformer> {

    String NAME = "twoWindingsTransformerToBeEstimated";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Return true if the ratio tap changer of the extended two windings transformer is to be estimated during a state estimation. Else, return false.
     */
    boolean shouldEstimateRatioTapChanger();

    /**
     * Return true if the phase tap changer of the extended two windings transformer is to be estimated during a state estimation. Else, return false.
     */
    boolean shouldEstimatePhaseTapChanger();

    /**
     * Specify if the ratio tap changer of the extended two windings transformer is to be estimated during a state estimation.
     */
    TwoWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger(boolean toBeEstimated);

    /**
     * Specify if the phase tap changer of the extended two windings transformer is to be estimated during a state estimation.
     */
    TwoWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger(boolean toBeEstimated);
}
