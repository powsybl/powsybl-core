/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface RatioTapChangerAdder extends TapChangerAdder<
    RatioTapChangerAdder,
    RatioTapChangerStep,
    RatioTapChangerAdder.StepAdder,
    RatioTapChangerStepsReplacer.StepAdder,
    RatioTapChangerStepsReplacer,
    RatioTapChanger> {

    /**
     * Interface for classes responsible for building a single step when using {@link RatioTapChangerAdder}.
     */
    interface StepAdder extends RatioTapChangerStepAdder<StepAdder, RatioTapChangerAdder> {
    }

    RatioTapChangerAdder setRegulationMode(RatioTapChanger.RegulationMode regulationMode);

    RatioTapChangerAdder setRegulationValue(double regulationValue);

    RatioTapChangerAdder setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities);

    RatioTapChangerAdder setTargetV(double targetV);
}
