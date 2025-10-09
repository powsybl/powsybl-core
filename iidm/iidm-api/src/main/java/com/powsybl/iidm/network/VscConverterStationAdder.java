/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * VSC converter station builder and adder.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public interface VscConverterStationAdder extends HvdcConverterStationAdder<VscConverterStation, VscConverterStationAdder> {

    VscConverterStationAdder setVoltageRegulatorOn(boolean voltageRegulatorOn);

    VscConverterStationAdder setVoltageSetpoint(double voltageSetpoint);

    VscConverterStationAdder setReactivePowerSetpoint(double reactivePowerSetpoint);

    default VscConverterStationAdder setRegulatingTerminal(Terminal regulatingTerminal) {
        return this;
    }

    @Override
    VscConverterStation add();
}
