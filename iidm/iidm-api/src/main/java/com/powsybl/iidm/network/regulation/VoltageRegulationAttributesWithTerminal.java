/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.iidm.network.Terminal;

public record VoltageRegulationAttributesWithTerminal(
    VoltageRegulationAttributes attributes,
    Terminal terminal
) {
    public double targetValue() {
        return attributes.targetValue();
    }

    public double targetDeadband() {
        return attributes.targetDeadband();
    }

    public double slope() {
        return attributes.slope();
    }

    public RegulationMode mode() {
        return attributes.mode();
    }

    public boolean isRegulating() {
        return attributes.isRegulating();
    }
}
