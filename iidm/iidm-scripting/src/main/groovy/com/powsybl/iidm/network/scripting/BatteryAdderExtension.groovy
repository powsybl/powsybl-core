/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.iidm.network.BatteryAdder

/**
 * @author Miora VEDELAGO {@literal <miora.ralambotiana at rte-france.com>}
 */
class BatteryAdderExtension {
    static BatteryAdder setP0(BatteryAdder self, double p0) {
        self.setTargetP(p0)
    }

    static BatteryAdder setQ0(BatteryAdder self, double q0) {
        self.setTargetQ(q0)
    }
}
