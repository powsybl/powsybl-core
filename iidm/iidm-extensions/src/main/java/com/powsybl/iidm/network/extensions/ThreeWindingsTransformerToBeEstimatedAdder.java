/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface ThreeWindingsTransformerToBeEstimatedAdder extends ExtensionAdder<ThreeWindingsTransformer, ThreeWindingsTransformerToBeEstimated> {

    @Override
    default Class<? super ThreeWindingsTransformerToBeEstimated> getExtensionClass() {
        return ThreeWindingsTransformerToBeEstimated.class;
    }

    ThreeWindingsTransformerToBeEstimatedAdder withRatioTapChanger1Status(boolean toBeEstimated);

    ThreeWindingsTransformerToBeEstimatedAdder withRatioTapChanger2Status(boolean toBeEstimated);

    ThreeWindingsTransformerToBeEstimatedAdder withRatioTapChanger3Status(boolean toBeEstimated);

    ThreeWindingsTransformerToBeEstimatedAdder withRatioTapChangerStatus(ThreeSides side, boolean toBeEstimated);

    ThreeWindingsTransformerToBeEstimatedAdder withPhaseTapChanger1Status(boolean toBeEstimated);

    ThreeWindingsTransformerToBeEstimatedAdder withPhaseTapChanger2Status(boolean toBeEstimated);

    ThreeWindingsTransformerToBeEstimatedAdder withPhaseTapChanger3Status(boolean toBeEstimated);

    ThreeWindingsTransformerToBeEstimatedAdder withPhaseTapChangerStatus(ThreeSides side, boolean toBeEstimated);
}
