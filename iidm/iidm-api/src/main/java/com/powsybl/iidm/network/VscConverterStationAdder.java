/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;

/**
 * VSC converter station builder and adder.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public interface VscConverterStationAdder extends HvdcConverterStationAdder<VscConverterStation, VscConverterStationAdder> {

    @Deprecated(forRemoval = true, since = "7.2.0")
    VscConverterStationAdder setVoltageRegulatorOn(boolean voltageRegulatorOn);

    @Deprecated(forRemoval = true, since = "7.2.0")
    VscConverterStationAdder setVoltageSetpoint(double voltageSetpoint);

    @Deprecated(forRemoval = true, since = "7.2.0")
    VscConverterStationAdder setReactivePowerSetpoint(double reactivePowerSetpoint);

    @Deprecated(forRemoval = true, since = "7.2.0")
    default VscConverterStationAdder setRegulatingTerminal(Terminal regulatingTerminal) {
        return this;
    }

    VoltageRegulationAdder<VscConverterStationAdder> newVoltageRegulation();

    VscConverterStationAdder setTargetQ(double targetQ);

    VscConverterStationAdder setTargetV(double targetV);

    @Override
    VscConverterStation add();
}
