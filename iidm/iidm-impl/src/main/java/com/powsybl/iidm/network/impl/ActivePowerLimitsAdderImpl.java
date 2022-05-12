/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ActivePowerLimits;
import com.powsybl.iidm.network.ActivePowerLimitsAdder;
import com.powsybl.iidm.network.LimitType;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class ActivePowerLimitsAdderImpl extends AbstractLoadingLimitsAdder<ActivePowerLimits, ActivePowerLimitsAdder> implements ActivePowerLimitsAdder {

    ActivePowerLimitsAdderImpl(OperationalLimitsOwner owner) {
        super(owner);
    }

    @Override
    public ActivePowerLimits add() {
        checkLoadingLimits();
        ActivePowerLimits limits = new ActivePowerLimitsImpl(owner, id, permanentLimit, temporaryLimits);
        owner.setOperationalLimits(LimitType.ACTIVE_POWER, limits);
        return limits;
    }
}
