/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.systemconstraints;

import java.util.Objects;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
final class NcSystemConstraintsImporter {
    private final NcSystemConstraintsModel model;

    NcSystemConstraintsImporter(NcSystemConstraintsModel model) {
        this.model = Objects.requireNonNull(model);
    }

    NcSystemConstraints importData() {
        return new NcSystemConstraints(
            new NcVoltageAngleLimitImporter(model).importData(),
            new NcPowerTransferCorridorImporter(model).importData());
    }
}
