/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * VSC converter station builder and adder.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface VscConverterStationAdder extends HvdcConverterStationAdder<VscConverterStationAdder> {

    VscConverterStationAdder setVoltageRegulatorOn(boolean voltageRegulatorOn);

    /**
     * To express local regulation. When the adder.add() method was invoked, the
     * adder must set the regulating terminal to the terminal of the resulting vsc converter added to the Network.
     */
    default VscConverterStationAdder useLocalRegulation(boolean use) {
        return this; // does nothing
    }

    VscConverterStationAdder setVoltageSetpoint(double voltageSetpoint);

    VscConverterStationAdder setReactivePowerSetpoint(double reactivePowerSetpoint);

    default VscConverterStationAdder setRegulatingTerminal(Terminal regulatingTerminal) {
        return this;
    }

    VscConverterStation add();
}
