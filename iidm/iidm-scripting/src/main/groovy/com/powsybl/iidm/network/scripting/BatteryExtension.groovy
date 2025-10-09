/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.iidm.network.Battery

/**
 * @author Miora VEDELAGO {@literal <miora.ralambotiana at rte-france.com>}
 */
class BatteryExtension {
    static double getP0(Battery self) {
        self.getTargetP()
    }

    static Battery setP0(Battery self, double p0) {
        self.setTargetP(p0)
    }

    static double getQ0(Battery self) {
        self.getTargetQ()
    }

    static Battery setQ0(Battery self, double q0) {
        self.setTargetQ(q0)
    }
}
