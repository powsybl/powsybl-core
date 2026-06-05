/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
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
public interface StaticVarCompensatorAdder extends InjectionAdder<StaticVarCompensator, StaticVarCompensatorAdder>,
        VoltageRegulationHolderAdder<StaticVarCompensatorAdder> {

    StaticVarCompensatorAdder setBmin(double bMin);

    StaticVarCompensatorAdder setBmax(double bMax);

    StaticVarCompensatorAdder setLocalTargetQ(double localTargetQ);

    StaticVarCompensatorAdder setLocalTargetV(double localTargetV);

    /**
     * @deprecated use {@link #newVoltageRegulation()}
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    StaticVarCompensatorAdder setVoltageSetpoint(double voltageSetpoint);

    /**
     * @deprecated use {@link #newVoltageRegulation()}
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    StaticVarCompensatorAdder setReactivePowerSetpoint(double reactivePowerSetpoint);

    /**
     * @deprecated use {@link #newVoltageRegulation()} with {@link com.powsybl.iidm.network.regulation.VoltageRegulationAdderOrBuilder#withMode(RegulationMode)} instead
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    StaticVarCompensatorAdder setRegulationMode(RegulationMode regulationMode);

    /**
     * @deprecated use {@link #newVoltageRegulation()}
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    StaticVarCompensatorAdder setRegulating(boolean regulating);

    /**
     * @deprecated use {@link #newVoltageRegulation()}
     */
    @Deprecated(forRemoval = true, since = "7.3.0")
    default StaticVarCompensatorAdder setRegulatingTerminal(Terminal regulatingTerminal) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    StaticVarCompensator add();
}
