/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface VoltageSourceConverterAdder extends AcDcConverterAdder<VoltageSourceConverter, VoltageSourceConverterAdder> {

    VoltageRegulationAdder<VoltageSourceConverterAdder> newVoltageRegulation();

    VoltageSourceConverterAdder setTargetQ(double targetQ);

    VoltageSourceConverterAdder setTargetV(double targetV);

    /**
     * @deprecated use {@link #newVoltageRegulation()} instead
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    VoltageSourceConverterAdder setVoltageRegulatorOn(boolean voltageRegulatorOn);

    /**
     * @deprecated use {@link #newVoltageRegulation()} instead
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    VoltageSourceConverterAdder setVoltageSetpoint(double voltageSetpoint);

    /**
     * @deprecated use {@link #newVoltageRegulation()} instead
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    VoltageSourceConverterAdder setReactivePowerSetpoint(double reactivePowerSetpoint);

}
