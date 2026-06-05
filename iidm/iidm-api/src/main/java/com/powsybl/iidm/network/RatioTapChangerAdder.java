/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolderAdder;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface RatioTapChangerAdder extends TapChangerAdder<
    RatioTapChangerAdder,
    RatioTapChangerStep,
    RatioTapChangerAdder.StepAdder,
    RatioTapChangerStepsReplacer.StepAdder,
    RatioTapChangerStepsReplacer,
    RatioTapChanger>,
    VoltageRegulationHolderAdder<RatioTapChangerAdder> {

    /**
     * Interface for classes responsible for building a single step when using {@link RatioTapChangerAdder}.
     */
    interface StepAdder extends RatioTapChangerStepAdder<StepAdder, RatioTapChangerAdder> {
    }

    /**
     * @deprecated use {@link #newVoltageRegulation()} with {@link com.powsybl.iidm.network.regulation.VoltageRegulationAdderOrBuilder#withMode(RegulationMode)} instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    RatioTapChangerAdder setRegulationMode(RegulationMode regulationMode);

    /**
     * @deprecated use {@link #newVoltageRegulation()} with {@link com.powsybl.iidm.network.regulation.VoltageRegulationAdderOrBuilder#withMode(RegulationMode)} instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    RatioTapChangerAdder setRegulationValue(double regulationValue);

    /**
     * @deprecated use {@link #newVoltageRegulation()} with {@link com.powsybl.iidm.network.regulation.VoltageRegulationAdderOrBuilder#withTargetValue(double)} instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    RatioTapChangerAdder setTargetV(double targetV);

    /**
     * @deprecated use {@link #newVoltageRegulation()} with {@link com.powsybl.iidm.network.regulation.VoltageRegulationAdderOrBuilder#withRegulating(boolean)} instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    RatioTapChangerAdder setRegulating(boolean regulating);

    /**
     * @deprecated use {@link #newVoltageRegulation()} with {@link com.powsybl.iidm.network.regulation.VoltageRegulationAdderOrBuilder#withTerminal(Terminal)} instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    RatioTapChangerAdder setRegulationTerminal(Terminal regulationTerminal);

    /**
     * @deprecated use {@link #newVoltageRegulation()} with {@link com.powsybl.iidm.network.regulation.VoltageRegulationAdderOrBuilder#withMode(RegulationMode)} instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    RatioTapChangerAdder setTargetDeadband(double targetDeadband);
}
