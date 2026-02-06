/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
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
public interface VoltageRegulationAdderOrBuilder<T extends VoltageRegulationAdderOrBuilder<T>> {

    T withTargetValue(double targetValue);

    T withTargetDeadband(double targetDeadband);

    T withSlope(double slope);

    T withTerminal(Terminal terminal);

    T withMode(RegulationMode mode);

    T withRegulating(boolean regulating);
}
