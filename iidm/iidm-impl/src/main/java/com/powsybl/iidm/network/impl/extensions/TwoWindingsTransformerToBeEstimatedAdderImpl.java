/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimatedAdder;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class TwoWindingsTransformerToBeEstimatedAdderImpl extends AbstractExtensionAdder<TwoWindingsTransformer, TwoWindingsTransformerToBeEstimated>
        implements TwoWindingsTransformerToBeEstimatedAdder {

    private boolean rtcStatus = false;
    private boolean ptcStatus = false;

    protected TwoWindingsTransformerToBeEstimatedAdderImpl(TwoWindingsTransformer extendable) {
        super(extendable);
    }

    @Override
    protected TwoWindingsTransformerToBeEstimated createExtension(TwoWindingsTransformer extendable) {
        return new TwoWindingsTransformerToBeEstimatedImpl(rtcStatus, ptcStatus);
    }

    @Override
    public TwoWindingsTransformerToBeEstimatedAdder withRatioTapChangerStatus(boolean toBeEstimated) {
        this.rtcStatus = toBeEstimated;
        return this;
    }

    @Override
    public TwoWindingsTransformerToBeEstimatedAdder withPhaseTapChangerStatus(boolean toBeEstimated) {
        this.ptcStatus = toBeEstimated;
        return this;
    }
}
