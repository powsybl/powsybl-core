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
import com.powsybl.iidm.network.Branch
import com.powsybl.iidm.network.CurrentLimits
import com.powsybl.iidm.network.LimitType
import com.powsybl.iidm.network.LoadingLimits
import com.powsybl.iidm.network.TwoSides

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class BranchExtension {

    static ActivePowerLimits getActivePowerLimits1(Branch self) {
        self.getNullableActivePowerLimits1()
    }

    static ApparentPowerLimits getApparentPowerLimits1(Branch self) {
        self.getNullableApparentPowerLimits1()
    }

    static CurrentLimits getCurrentLimits1(Branch self) {
        self.getNullableCurrentLimits1()
    }

    static ActivePowerLimits getActivePowerLimits2(Branch self) {
        self.getNullableActivePowerLimits2()
    }

    static ApparentPowerLimits getApparentPowerLimits2(Branch self) {
        self.getNullableApparentPowerLimits2()
    }

    static CurrentLimits getCurrentLimits2(Branch self) {
        self.getNullableCurrentLimits2()
    }

    static CurrentLimits getCurrentLimits(Branch self, TwoSides side) {
        self.getNullableCurrentLimits(side)
    }

    static ActivePowerLimits getActivePowerLimits(Branch self, TwoSides side) {
        self.getNullableActivePowerLimits(side)
    }

    static ApparentPowerLimits getApparentPowerLimits(Branch self, TwoSides side) {
        self.getNullableApparentPowerLimits(side)
    }

    static LoadingLimits getLimits(Branch self, LimitType type, TwoSides side) {
        self.getNullableLimits(type, side)
    }
}
