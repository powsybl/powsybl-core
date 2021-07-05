/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimated;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class ThreeWindingsTransformerToBeEstimatedImpl extends AbstractExtension<ThreeWindingsTransformer> implements ThreeWindingsTransformerToBeEstimated {

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
    public boolean containsRatioTapChanger1() {
        return rtc1Status;
    }

    @Override
    public boolean containsRatioTapChanger2() {
        return rtc2Status;
    }

    @Override
    public boolean containsRatioTapChanger3() {
        return rtc3Status;
    }

    @Override
    public boolean containsPhaseTapChanger1() {
        return ptc1Status;
    }

    @Override
    public boolean containsPhaseTapChanger2() {
        return ptc2Status;
    }

    @Override
    public boolean containsPhaseTapChanger3() {
        return ptc3Status;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated setRatioTapChanger1Status(boolean toBeEstimated) {
        this.rtc1Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated setRatioTapChanger2Status(boolean toBeEstimated) {
        this.rtc2Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated setRatioTapChanger3Status(boolean toBeEstimated) {
        this.rtc3Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated setPhaseTapChanger1Status(boolean toBeEstimated) {
        this.ptc1Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated setPhaseTapChanger2Status(boolean toBeEstimated) {
        this.ptc2Status = toBeEstimated;
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated setPhaseTapChanger3Status(boolean toBeEstimated) {
        this.ptc3Status = toBeEstimated;
        return this;
    }
}
