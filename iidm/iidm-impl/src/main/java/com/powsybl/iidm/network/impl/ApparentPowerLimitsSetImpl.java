/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ApparentPowerLimits;
import com.powsybl.iidm.network.ApparentPowerLimitsSet;
import com.powsybl.iidm.network.LimitType;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class ApparentPowerLimitsSetImpl extends AbstractOperationalLimitsSet<ApparentPowerLimits> implements ApparentPowerLimitsSet {

    ApparentPowerLimitsSetImpl(OperationalLimitsOwner owner) {
        super(owner);
    }

    @Override
    LimitType getType() {
        return LimitType.APPARENT_POWER;
    }
}
