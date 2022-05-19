/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.iidm.network.ActivePowerLimits
import com.powsybl.iidm.network.ApparentPowerLimits
import com.powsybl.iidm.network.Branch
import com.powsybl.iidm.network.CurrentLimits

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class BranchExtension {

    static ActivePowerLimits getActivePowerLimits1(Branch self) {
        self.getActiveActivePowerLimits1().orElse(null)
    }

    static ApparentPowerLimits getApparentPowerLimits1(Branch self) {
        self.getActiveApparentPowerLimits1().orElse(null)
    }

    static CurrentLimits getCurrentLimits1(Branch self) {
        self.getActiveCurrentLimits1().orElse(null)
    }

    static ActivePowerLimits getActivePowerLimits2(Branch self) {
        self.getActiveActivePowerLimits2().orElse(null)
    }

    static ApparentPowerLimits getApparentPowerLimits2(Branch self) {
        self.getActiveApparentPowerLimits2().orElse(null)
    }

    static CurrentLimits getCurrentLimits2(Branch self) {
        self.getActiveCurrentLimits2().orElse(null)
    }
}
