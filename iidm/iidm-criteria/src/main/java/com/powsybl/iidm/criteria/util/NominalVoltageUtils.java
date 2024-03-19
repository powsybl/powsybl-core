/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.util;

import com.powsybl.iidm.network.Terminal;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class NominalVoltageUtils {
    private NominalVoltageUtils() {
    }

    public static Double getNominalVoltage(Terminal terminal) {
        return terminal != null && terminal.getVoltageLevel() != null ? terminal.getVoltageLevel().getNominalV() : null;
    }
}
