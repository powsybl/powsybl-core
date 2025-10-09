/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.iidm.network.ActivePowerLimits
import com.powsybl.iidm.network.ApparentPowerLimits
import com.powsybl.iidm.network.CurrentLimits
import com.powsybl.iidm.network.FlowsLimitsHolder

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class FlowLimitsHolderExtension {

    static ActivePowerLimits getActivePowerLimits(FlowsLimitsHolder self) {
        self.getNullableActivePowerLimits()
    }

    static ApparentPowerLimits getApparentPowerLimits(FlowsLimitsHolder self) {
        self.getNullableApparentPowerLimits()
    }

    static CurrentLimits getCurrentLimits(FlowsLimitsHolder self) {
        self.getNullableCurrentLimits()
    }
}
