/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.iidm.network.Terminal;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public interface VoltageRegulationBuilder<T extends VoltageRegulationAdder<T>> {

    VoltageRegulationAdder<T> setTargetValue(Double targetValue);

    VoltageRegulationAdder<T> setTargetDeadband(Double targetDeadband);

    VoltageRegulationAdder<T> setSlope(Double slope);

    VoltageRegulationAdder<T> setTerminal(Terminal terminal);

    VoltageRegulationAdder<T> setMode(RegulationMode mode);

    VoltageRegulationAdder<T> setRegulating(boolean regulating);

    T addVoltageRegulation();
}
