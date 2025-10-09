/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimated;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class ThreeWindingsTransformerToBeEstimatedImpl extends AbstractExtension<ThreeWindingsTransformer> implements ThreeWindingsTransformerToBeEstimated {

    private static final String UNEXPECTED_SIDE = "Unexpected side: ";

    private boolean rtc1Status;
    private boolean rtc2Status;
    private boolean rtc3Status;
    private boolean ptc1Status;
    private boolean ptc2Status;
    private boolean ptc3Status;

    ThreeWindingsTransformerToBeEstimatedImpl(boolean rtc1Status, boolean rtc2Status, boolean rtc3Status, boolean ptc1Status,
                                              boolean ptc2Status, boolean ptc3Status) {
        this.rtc1Status = rtc1Status;
        this.rtc2Status = rtc2Status;
        this.rtc3Status = rtc3Status;
        this.ptc1Status = ptc1Status;
        this.ptc2Status = ptc2Status;
        this.ptc3Status = ptc3Status;
    }

    @Override
    public boolean shouldEstimateRatioTapChanger1() {
        return rtc1Status;
    }

    @Override
    public boolean shouldEstimateRatioTapChanger2() {
        return rtc2Status;
    }

    @Override
    public boolean shouldEstimateRatioTapChanger3() {
        return rtc3Status;
    }

    @Override
    public boolean shouldEstimateRatioTapChanger(ThreeSides side) {
        switch (side) {
            case ONE:
                return rtc1Status;
            case TWO:
                return rtc2Status;
            case THREE:
                return rtc3Status;
        }
        throw new IllegalStateException(UNEXPECTED_SIDE + side);
    }

    @Override
    public boolean shouldEstimatePhaseTapChanger1() {
        return ptc1Status;
    }

    @Override
    public boolean shouldEstimatePhaseTapChanger2() {
        return ptc2Status;
    }

    @Override
    public boolean shouldEstimatePhaseTapChanger3() {
        return ptc3Status;
    }

    @Override
    public boolean shouldEstimatePhaseTapChanger(ThreeSides side) {
        switch (side) {
            case ONE:
                return ptc1Status;
            case TWO:
                return ptc2Status;
            case THREE:
                return ptc3Status;
        }
        throw new IllegalStateException(UNEXPECTED_SIDE + side);
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger1(boolean toBeEstimated) {
        this.rtc1Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger2(boolean toBeEstimated) {
        this.rtc2Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger3(boolean toBeEstimated) {
        this.rtc3Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger(boolean toBeEstimated, ThreeSides side) {
        switch (side) {
            case ONE:
                rtc1Status = toBeEstimated;
                break;
            case TWO:
                rtc2Status = toBeEstimated;
                break;
            case THREE:
                rtc3Status = toBeEstimated;
                break;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + side);
        }
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger1(boolean toBeEstimated) {
        this.ptc1Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger2(boolean toBeEstimated) {
        this.ptc2Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger3(boolean toBeEstimated) {
        this.ptc3Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger(boolean toBeEstimated, ThreeSides side) {
        switch (side) {
            case ONE:
                ptc1Status = toBeEstimated;
                break;
            case TWO:
                ptc2Status = toBeEstimated;
                break;
            case THREE:
                ptc3Status = toBeEstimated;
                break;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + side);
        }
        return this;
    }
}
