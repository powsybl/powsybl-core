/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation;

import com.powsybl.iidm.network.regulation.RegulationMode;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
record DataVoltageRegulationHolderCreator(
    String id,
    RegulationMode mode,
    boolean remoteTerminal,
    double targetValue,
    double localTargetV,
    double localTargetQ,
    double targetDeadband,
    double slope,
    boolean regulating
) {

    DataVoltageRegulationHolderCreator(
        String id,
        RegulationMode mode,
        boolean remoteTerminal,
        double targetValue,
        double localTargetV,
        double localTargetQ,
        boolean regulating
    ) {
        this(id, mode, remoteTerminal, targetValue, localTargetV, localTargetQ, Double.NaN, Double.NaN, regulating);
    }

    DataVoltageRegulationHolderCreator(
        String id,
        RegulationMode mode,
        boolean remoteTerminal,
        double targetValue,
        double targetDeadband,
        boolean regulating
    ) {
        this(id, mode, remoteTerminal, targetValue, Double.NaN, Double.NaN, targetDeadband, Double.NaN, regulating);
    }
}
