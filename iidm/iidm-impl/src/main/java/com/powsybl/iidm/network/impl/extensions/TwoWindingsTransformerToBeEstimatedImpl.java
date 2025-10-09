/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimated;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class TwoWindingsTransformerToBeEstimatedImpl extends AbstractExtension<TwoWindingsTransformer> implements TwoWindingsTransformerToBeEstimated {

    private boolean rtcStatus;
    private boolean ptcStatus;

    TwoWindingsTransformerToBeEstimatedImpl(boolean rtcStatus, boolean ptcStatus) {
        this.rtcStatus = rtcStatus;
        this.ptcStatus = ptcStatus;
    }

    @Override
    public boolean shouldEstimateRatioTapChanger() {
        return rtcStatus;
    }

    @Override
    public boolean shouldEstimatePhaseTapChanger() {
        return ptcStatus;
    }

    @Override
    public TwoWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger(boolean toBeEstimated) {
        this.rtcStatus = toBeEstimated;
        return this;
    }

    @Override
    public TwoWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger(boolean toBeEstimated) {
        this.ptcStatus = toBeEstimated;
        return this;
    }
}
