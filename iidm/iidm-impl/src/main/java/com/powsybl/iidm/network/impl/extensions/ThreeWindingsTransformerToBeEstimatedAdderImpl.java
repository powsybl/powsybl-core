/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimatedAdder;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class ThreeWindingsTransformerToBeEstimatedAdderImpl extends AbstractExtensionAdder<ThreeWindingsTransformer, ThreeWindingsTransformerToBeEstimated>
        implements ThreeWindingsTransformerToBeEstimatedAdder {

    private boolean rtc1Status = false;
    private boolean rtc2Status = false;
    private boolean rtc3Status = false;
    private boolean ptc1Status = false;
    private boolean ptc2Status = false;
    private boolean ptc3Status = false;

    protected ThreeWindingsTransformerToBeEstimatedAdderImpl(ThreeWindingsTransformer extendable) {
        super(extendable);
    }

    @Override
    protected ThreeWindingsTransformerToBeEstimated createExtension(ThreeWindingsTransformer extendable) {
        return new ThreeWindingsTransformerToBeEstimatedImpl(rtc1Status, rtc2Status, rtc3Status, ptc1Status, ptc2Status, ptc3Status);
    }

    @Override
    public ThreeWindingsTransformerToBeEstimatedAdder withRatioTapChanger1Status(boolean toBeEstimated) {
        this.rtc1Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimatedAdder withRatioTapChanger2Status(boolean toBeEstimated) {
        this.rtc2Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimatedAdder withRatioTapChanger3Status(boolean toBeEstimated) {
        this.rtc3Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimatedAdder withRatioTapChangerStatus(ThreeSides side, boolean toBeEstimated) {
        switch (side) {
            case ONE -> this.rtc1Status = toBeEstimated;
            case TWO -> this.rtc2Status = toBeEstimated;
            case THREE -> this.rtc3Status = toBeEstimated;
        }
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimatedAdder withPhaseTapChanger1Status(boolean toBeEstimated) {
        this.ptc1Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimatedAdder withPhaseTapChanger2Status(boolean toBeEstimated) {
        this.ptc2Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimatedAdder withPhaseTapChanger3Status(boolean toBeEstimated) {
        this.ptc3Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimatedAdder withPhaseTapChangerStatus(ThreeSides side, boolean toBeEstimated) {
        switch (side) {
            case ONE -> this.ptc1Status = toBeEstimated;
            case TWO -> this.ptc2Status = toBeEstimated;
            case THREE -> this.ptc3Status = toBeEstimated;
        }
        return this;
    }
}
